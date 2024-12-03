package org.qh.DDBMS.LDBMS.sql.impl;

import org.qh.DDBMS.LDBMS.sql.SQL;
import org.qh.DDBMS.LDBMS.sql.SQLBank;
import org.qh.DDBMS.LDBMS.sql.config.SQLBankConfig;
import org.qh.DDBMS.common.db.DBTransaction;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/26
 * @Version: 0.0.0
 * @Description: 该类是SQLBank的实现类
 */
public class SQLBankImpl implements SQLBank {


    /**
     * <pre>
     * 说明：SQL被存入DB事务将被保存到该属性中
     * </pre>
     */
    private Set<DBTransaction> inDB;

    /**
     * <pre>
     * 说明：保存事务和SQL关系的map
     * </pre>
     */
    private Map<DBTransaction, List<SQL>> sqlMap;

    /**
     * <pre>
     * 说明：获取配置的信息
     *   1) bufferSize：正常情况下sqlMap中缓存事务个数
     *   2) maxSize：最大情况下，sqlMap中缓存事务个数
     * </pre>
     */
    @Resource
    private SQLBankConfig config;
    /**
     * <pre>
     * 说明：获取数据库连接的数据源
     * </pre>
     */
    @Resource
    private DataSource dataSource;

    public SQLBankImpl() {
        inDB = Collections.synchronizedSet(new HashSet<>());
        sqlMap = new ConcurrentHashMap<>();
    }

    /**
     * <pre>
     * 说明：存入一个sql
     * 注意事项：
     *   1. 不允许同一个事务并发的存入sql
     * 实现步骤：
     *   1) 获取sql对应的事务在sqlMap的list
     *   2) 判定list不为null将sql加入list
     *   3) 判定list为null
     *     1. 将当前sql的信息写入db
     *     2. 将事务加入inDB中
     * </pre>
     *
     * @param sql 要存入的SQL实例
     * @since 0.0.0
     */
    @Override
    public void put(SQL sql) throws SQLException {
        DBTransaction transaction = sql.transaction();
        List<SQL> list = sqlList(transaction);

        if (list != null) list.add(sql);
        else {
            writeToDB(transaction, sql);
            inDB.add(transaction);
        }
    }

    /**
     * <pre>
     * 说明：获取一个事务对应sqlList
     * 实现步骤：
     *   1. 从sqlMap中获取，存在直接返回
     *   2. 判定当前sql已经存在于db中返回null
     *   3. 判定sqlMap中元素个数小于bufferSize
     *     2.1. 构建一个list装入sqlMap
     *     2.2. 返回该list
     *   4. 判定事务的order为1且sqlMap中元素个数小于maxBufferSize
     *     2.1. 构建一个list装入sqlMap
     *     2.2. 返回该list
     *   4. 返回null
     * </pre>
     * @param transaction 要存入sql的事务
     * @return transaction对应的sqlList
     * @since 0.0.0
     */
    private List<SQL> sqlList(DBTransaction transaction) {
        List<SQL> sqlList = sqlMap.get(transaction);
        if (sqlList != null) return sqlList;
        if (inDB.contains(transaction)) return null;
        int size = sqlMap.size();
        if (!(size < config.bufferSize() ||
                (transaction.getOrder() == 1 && size < config.maxBufferSize()))) {
            return null;
        }
        ArrayList<SQL> list = new ArrayList<>();
        sqlMap.put(transaction, list);
        return list;
    }

    @Override
    public void putAll(List<SQL> sqlList) throws SQLException {
        for (SQL sql : sqlList) {
            put(sql);
        }
    }

    /**
     * <pre>
     * 说明：获取一个事务存入的所有sql
     * 实现步骤：
     *   1) 获取事务在sqlMap中对应的list
     *   2) list不等于null，则返回list
     *   3) 判定当前事务的sql被保存到数据库中，则从数据库中读出对应的sql数据
     *   4) 返回list
     * </pre>
     *
     * @param transaction 要获取SQL的事务
     * @return 该事务存入的所有SQL列表
     * @since 0.0.0
     */
    @Override
    public List<SQL> get(DBTransaction transaction) throws SQLException {
        List<SQL> list = sqlMap.get(transaction);
        if (list != null) return list;
        if (inDB.contains(transaction)) {
            list = readFromDB(transaction);
        }
        return list;
    }

    /**
     * <pre>
     * 说明：删除一个事务存入的所有SQL，并返回
     * 实现步骤：
     *   1) 判定该事务对应的sql存入DB，则从数据库中删除对应的元组
     *   2) 判定该事务对应的sql在sqlMap中，则将sqlMap中的该事务删除
     * </pre>
     *
     * @param transaction 要删除SQL的事务
     * @since 0.0.0
     */
    @Override
    public void delete(DBTransaction transaction) throws SQLException {
        if (inDB.contains(transaction)) {
            deleteFromDB(transaction);
            inDB.remove(transaction);
        }
        sqlMap.remove(transaction);
    }

    /**
     * <pre>
     * 说明：将当前SQL写入数据库
     * 实现步骤：
     *   1. 获取数据库连接
     *   2. 获取sql语句
     *   3. 执行sql语句
     *   4. 关闭数据库连接
     * </pre>
     *
     * @param transaction 要存入SQL的事务
     * @param sql 要写入的SQL
     * @since 0.0.0
     */
    private void writeToDB(DBTransaction transaction, SQL sql) throws SQLException {

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.
                     prepareStatement(SQLBankTable.INSERT_SQL_TEMPLATE)) {
            ps.setLong(1, transaction.getId());
            ps.setInt(2, transaction.getType());
            ps.setByte(3, sql.type());
            ps.setString(4, sql.statement());
            ps.execute();
        }
    }


    /**
     * <pre>
     * 说明：从数据库读取对应事务的SQL数据
     * 实现步骤：
     *   1. 获取select语句
     *   2. 获取数据库连接
     *   3. 执行sql语句
     *   4. 将查询结果封装为SQLList
     *   5. 返回查询结果
     * </pre>
     *
     * @param transaction 要读取SQL的事务
     * @return 读取到的SQL列表
     * @since 0.0.0
     */
    private List<SQL> readFromDB(DBTransaction transaction) throws SQLException {
        String statement = selectSqlStatement(transaction);
        List<SQL> sqlList = null;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(statement);
             ResultSet resultSet = ps.executeQuery()) {

            sqlList = resultSetToSQLList(resultSet, transaction);
        }
        return sqlList;
    }

    /**
     * <pre>
     * 说明：将从数据库中查询到resultSet转化为List<SQL>
     * 实现步骤：
     *   1. 构建list实例
     *   2. 循环地取出一行数据封装为SQL实例
     *     2.1. 从resultSet中取出SQL_TYPE
     *     2.2. 从resultSet中取出SQL
     *     2.3. 构建SQL实例并加入到list
     *   3. 返回list
     * </pre>
     * @param resultSet 执行查询语句得到的结果集
     * @param transaction 需要查询SQL的事务
     * @return 查询到的SQL实例集合
     * @throws SQLException
     * @since 0.0.0
     */
    private List<SQL> resultSetToSQLList(ResultSet resultSet, DBTransaction transaction) throws SQLException {
        ArrayList<SQL> list = new ArrayList<>();
        Byte sqlType = null;
        String statement = null;
        while (resultSet.next()) {
            sqlType = resultSet.getByte(SQLBankTable.SQL_TYPE);
            statement = resultSet.getString(SQLBankTable.STATEMENT);
            list.add(new SQLImpl(transaction, sqlType, statement));
        }
        return list;
    }

    /**
     * 说明：根据transaction获取查询sql实例的语句
     * @param transaction 进行查询的事务
     * @return select语句
     * @since 0.0.0
     */
    private String selectSqlStatement(DBTransaction transaction) {
        return String.format(SQLBankTable.SELECT_SQL_TEMPLATE, transaction.getId(), transaction.getType());
    }

    /**
     * <pre>
     * 说明：从数据库删除对应事务的SQL数据
     * 实现步骤：
     *   1. 得到删除语句
     *   2. 得到数据库连接
     *   3. 执行删除
     *   4. 释放连接
     * </pre>
     *
     * @param transaction 要删除SQL的事务
     * @since 0.0.0
     */
    private void deleteFromDB(DBTransaction transaction) throws SQLException {
        String statement = deleteSqlStatement(transaction);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(statement)) {
            ps.execute();
        }
    }

    /**
     * 说明：得到删除sql的delete语句
     * @param transaction 要删除sql的事务
     * @return delete语句
     * @since 0.0.0
     */
    private String deleteSqlStatement(DBTransaction transaction) {
        return String.format(SQLBankTable.DELETE_SQL_TEMPLATE, transaction.getId(), transaction.getType());
    }

    /**
     * 说明：该接口描述了数据库中sql_bank具有的字段信息
     */
    private static interface SQLBankTable {
        String TABLE_NAME = "sql_bank";
        String ID = "id";
        String TRANSACTION_ID = "transaction_id";
        String TRANSACTION_TYPE = "transaction_type";
        String SQL_TYPE = "sql_type";
        String STATEMENT = "statement";

        // sql templates
        String INSERT_SQL_TEMPLATE = "insert into `" + TABLE_NAME + "` values(null, ?, ?, ?, ?);";
        String SELECT_SQL_TEMPLATE = "select `" + SQL_TYPE + "`, `" + STATEMENT + "` from `" + TABLE_NAME + "` " +
                "where `" + TRANSACTION_ID +"` = %d and `" + TRANSACTION_TYPE +"` = %d " +
                "order by `" + ID + "` asc;";

        String DELETE_SQL_TEMPLATE = "delete from `" + TABLE_NAME + "` " +
                "where `" + TRANSACTION_ID +"` = %d and `" + TRANSACTION_TYPE +"` = %d;";
    }
}

