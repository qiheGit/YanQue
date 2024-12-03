package org.qh.DDBMS.LDBMS.sql.impl;

import org.qh.DDBMS.LDBMS.ms_sync.SyncSender;
import org.qh.DDBMS.LDBMS.ms_sys.MasterSlaveManager;
import org.qh.DDBMS.LDBMS.sql.Commit;
import org.qh.DDBMS.LDBMS.sql.SQL;
import org.qh.DDBMS.LDBMS.sql.SQLBank;
import org.qh.DDBMS.LDBMS.tx.DoCommit;
import org.qh.DDBMS.LDBMS.tx.LTransactionCenter;
import org.qh.DDBMS.common.Constant;
import org.qh.DDBMS.common.config.GDBMSConfig;
import org.qh.DDBMS.common.db.DBTransaction;
import org.qh.DDBMS.common.dblock.DBLock;
import org.qh.DDBMS.common.entity.SyncInfoEntity;
import org.qh.DDBMS.common.exception.FailedTransactionException;
import org.qh.DDBMS.common.input.ServerConfig;
import org.qh.DDBMS.common.output.DDBMSSender;
import org.qh.DDBMS.common.protocol.ACKProtocol;
import org.qh.tools.clz.LauncherUtils;
import org.qh.tools.exception.ExceptionUtils;
import org.qh.tools.str.StringUtils;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @Author: qihe
 * @Date: 2024/10/26
 * @Version: 0.0.0
 * @Description: Commit的实现类
 */
public class CommitImpl implements Commit {

    /**
     * <pre>
     * 说明：获取数据库连接的数据源
     * </pre>
     */
    @Resource
    private DataSource dataSource;

    /**
     * <pre>
     * 说明：该管理器管理一个事务对数据进行修改的语句
     * </pre>
     */
    @Resource
    private SQLBank bank;

    /**
     * <pre>
     * 说明：对数据库资源进行加锁的接口
     * </pre>
     */
    @Resource
    private DBLock dbLock;

    /**
     * <pre>
     * 说明：保存同步信息
     * </pre>
     */
    @Resource
    private SyncSender syncSender;


    /**
     * <pre>
     * 说明：协议发送器
     * </pre>
     */
    @Resource
    private DDBMSSender<String> sender;


    /**
     * <pre>
     * 说明：用于获取GDBMS的站点信息
     * </pre>
     */
    @Resource
    private GDBMSConfig gdbmsConfig;

    /**
     * <pre>
     * 说明：局部事务中心
     * </pre>
     */
    @Resource
    private LTransactionCenter txCenter;

    /**
     * <pre>
     * 说明：主从管理中心
     * </pre>
     */
    @Resource
    private MasterSlaveManager msManager;

    /**
     * <pre>
     * 说明：主从管理中心
     * </pre>
     */
    @Resource
    private ServerConfig serverConfig;

    /**
     * <pre>
     * 说明：该方法获取一个事务的修改语句
     * 实现步骤：
     *   1) 从bank中获取该事务所有对数据库进行修改的SQL实例
     *   2) 删除bank中该事务的sql
     *   3) 如果传入sql实例中也存在sql语句，将该sql语句加入到结果集的尾部
     *   4) 返回res
     * </pre>
     *
     * @return 最终的SQL语句
     * @since 0.0.0
     */
    private List<SQL> statement(SQL sql) throws SQLException {
        List<SQL> res = null;
        try {
            res = bank.get(sql.transaction());
        } finally {
            bank.delete(sql.transaction());
        }
        if (res == null) res = new ArrayList<SQL>();


        if (!StringUtils.isEmpty(sql.statement())) {
            res.add(sql);
        }
        return res;
    }

    /**
     * <pre>
     * 说明：确认当前事务对数据库的修改是否提交
     * 规范：
     *   1. ACKType: COMMIT_TYPE
     *   2. data[]:
     *     - dbName
     *     - transaction
     *     - sqlStatement // 所有语句组成的总的字符串
     * 实现步骤：
     *   1) 将doCommit注册到事务中心
     *   2) 向全局DB管理系统发送确认信息
     * </pre>
     *
     * @param transaction 当前事务
     * @param sqlStatement 当前事务执行的SQL语句
     * @param doCommit 处理全局数据库的确认结果，最终确定是否提交事务
     * @since 0.0.0
     */
    private void confirm(DBTransaction transaction, String sqlStatement, DoCommit doCommit) {
        txCenter.registerCommit(transaction, doCommit);
        sender.send(gdbmsConfig.siteName(), new ACKProtocol(Constant.ACKType.COMMIT_TYPE,
                serverConfig.dbName(), transaction, sqlStatement), null, false);

    }

    /**
     * <pre>
     * 说明：提交SQL指定的事务
     * 实现步骤：
     *   1) 判定当前站点不是主站点
     *     1. 抛出异常
     *   2) 执行statement() 获取SQL语句
     *   3) 通过DBLock对该事务应该持有的锁进行加锁
     *     1. 加锁失败，则执行撤销事务的操作
     *   4) 获取连接，执行SQL语句
     *   5) 将所有执行的sql语句连成一条语句
     *   6) 将handleCommit() 封装为一个doCommit
     *   7) 执行confirm()，等待Global确认是否提交更新
     * </pre>
     *
     * @param sql 要提交的SQL
     * @since 0.0.0
     */
    @Override
    public void commit(SQL sql) throws SQLException {
        // 1) 判定当前站点不是主站点
        if (!msManager.isMaster()) throw new RuntimeException("This site is not a master!");

        DBTransaction transaction = sql.transaction();
        try {
            List<SQL> list = statement(sql); // 2)获取SQL语句
            if (list.isEmpty()) throw new RuntimeException("Nothing to commit!");

            if (!dbLock.lock(transaction)) { // 3) 对该事务应该持有的锁进行加锁
                txCenter.cancel(transaction, Constant.TransactionCenter.LOCK_FAILURE);
            }

            Connection connection = dataSource.getConnection(); // 4) 获取连接，执行SQL语句
            executeSQL(connection, list);

            String sqlStatement = combineSql(list); // 5) 将所有执行的sql语句连成一条语句

            // 6) 将handleCommit() 封装为一个doCommit
            DoCommit doCommit = (id) -> handleCommit(id, connection, transaction, sqlStatement);

            // 7) 执行confirm()，向Global确认是否提交更新
            confirm(transaction, sqlStatement, doCommit);
        } catch (Exception e) {
            throw new FailedTransactionException(transaction);
        }

    }

    /**
     * <pre>
     * 说明：将list中的sql语句联合成一个字符串
     * </pre>
     * @param list sql集合
     * @return list中sql语句组成的字符串
     * @since 0.0.0
     */
    private String combineSql(List<SQL> list) {
        StringBuilder sb = new StringBuilder();
        for (SQL sql : list) {
            sb.append(sql.statement()).append(Constant.Sync.SQL_STATEMENT_SEPARATOR);
        }
        sb.setLength(sb.length() - Constant.Sync.SQL_STATEMENT_SEPARATOR.length());
        return sb.toString();
    }

    /**
     * 说明：执行sql语句
     * @param connection 数据库连接
     * @param list sql集合
     * @throws SQLException
     * @since 0.0.0
     */
    private void executeSQL(Connection connection, List<SQL> list) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(list.get(0).statement())) {
            for (SQL sql : list) {
                ps.addBatch(sql.statement());
            }
            ps.executeBatch();
        }
    }

    /**
     * <pre>
     * 说明：处理全局服务器是否确认提交当前事务
     * 实现步骤：
     *   1) 判定id不为Null，则提交该事务：
     *     1. 将同步信息保存到数据库
     *     2. 提交事务
     *     3. 释放锁和其他事务持有的资源
     *     4. 将同步信息发送给从站点
     *   2) 判定不提交该事务
     *     1. 释放锁和其他事务持有的资源
     * </pre>
     *
     * @param connection 数据库连接
     * @param transaction 当前事务
     * @param sqlStatement 当前事务执行的SQL语句
     * @since 0.0.0
     */
    private void handleCommit(Long id, Connection connection,
                              DBTransaction transaction, String sqlStatement) {
        try {
            if (id == null) { // 不提交
                dbLock.unlock(transaction);
                connection.rollback();
                return;
            }
            SyncInfoEntity info = new SyncInfoEntity(id, transaction, sqlStatement);
            insertSyncInfo(info, connection);
            connection.commit();
            dbLock.unlock(transaction);
            syncSender.send(info);
        } catch (Exception e) {
            ExceptionUtils.printStackTrace(e);
            LauncherUtils.destroy(); // 销毁程序
        }finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException e) {
                ExceptionUtils.printStackTrace(e);
            }
        }
    }

    /**
     * <pre>
     * 说明：保存一个事务相关的同步信息
     * 实现步骤：
     *   1) 构建insert语句
     *   2) 执行sql语句
     * </pre>
     * @param info 一个事务有关的同步信息
     * @param conn 数据库连接
     * @throws SQLException
     * @since 0.0.0
     */
    private void insertSyncInfo(SyncInfoEntity info, Connection conn) throws SQLException {
        String insertSql = "INSERT INTO `" + Constant.Sync.SYNC_INFO_TABLE_PREFIX +
                serverConfig.dbName() +
                "` VALUES(?, ?, ?, ?);";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)){
            ps.setLong(1, info.getId());
            ps.setLong(2, info.getTransaction().getId());
            ps.setInt(3, info.getTransaction().getType());
            ps.setString(4, info.getSqlStatement());
            ps.executeUpdate();
        }
    }
}

