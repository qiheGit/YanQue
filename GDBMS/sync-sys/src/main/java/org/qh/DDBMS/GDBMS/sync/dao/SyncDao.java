package org.qh.DDBMS.GDBMS.sync.dao;

import org.qh.DDBMS.common.Constant;
import org.qh.DDBMS.common.db.DBTransaction;
import org.qh.DDBMS.common.entity.SyncInfoEntity;

import javax.annotation.Resource;
import javax.sql.DataSource;
import javax.swing.text.Style;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/18
 * @Version: 0.0.0
 * @Description:
 *   1. 生产所有Sync模块需要的sql语句
 *   2. 为Sync模块的查询结果进行数据封装
 */
public class SyncDao {
    @Resource
    private DataSource dataSource;

    /**
     * <pre>
     * 说明：取得所有数据库的同步信息表名
     * 实现步骤：
     *   1. 编写查询语句
     *   2. 执行sql
     *   3. 将所有同步信息表的名称加入到结果集
     *   4. 返回结果集
     * </pre>
     * @return 所有数据库的同步信息表名
     * @since 0.0.0
     */
    private List<String> selectSyncInfoTables() throws SQLException {
        String sql = "shows tables;";
        ArrayList<String> res = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet resultSet = ps.executeQuery()) {

            while (resultSet.next()) {
                String tableName = resultSet.getString(1);
                if (tableName.startsWith(Constant.Sync.SYNC_INFO_TABLE_PREFIX)) res.add(tableName);
            }
        }
        return res;
    }


    /**
     * 说明：查询得到所有同步信息的最大id
     * 实现步骤：
     *   1. 得到所有的同步信息表的数据库名
     *   2. 构建查询同步信息表最大id的sql语句
     *   3. 查询所有表中的最大id并封进结果集
     *   4. 将结果作为map返回
     * @return Map<String, Long>
     * @since 0.0.0
     */
    public Map<String, Long> selectSyncTableIds() throws SQLException {
        List<String> tableNames = selectSyncInfoTables();
        List<String> dbName = new ArrayList<>(tableNames.size());
        Map<String, Long> res = new HashMap<>();
        if (tableNames.isEmpty()) return res;

        for (String table : tableNames) dbName.add(table.substring(Constant.Sync.SYNC_INFO_TABLE_PREFIX.length()));
        String sql = selectSyncTableIdsSql(tableNames, dbName);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet resultSet = ps.executeQuery()) {
            while (resultSet.next()) {
                res.put(resultSet.getString(1), resultSet.getLong(2));
            }
        }
        return res;
    }

    /**
     * 说明：得到查询传入表中最大id和数据库名的sql语句
     * 模板：
     *   SELECT 't2', IFNULL(MAX(`id`), 0) id FROM `t2`
     *   UNION
     *   SELECT 't1', IFNULL(MAX(`id`), 0) id FROM `t1`;
     * @param tableNames 表明
     * @param dbName 表名对应的数据库名
     * @return 查询所有表中最大id的sql语句
     * @since 0.0.0
     */
    private String selectSyncTableIdsSql(List<String> tableNames, List<String> dbName) {
        StringBuilder sb = new StringBuilder();
        String selectTemplate = "SELECT '%s', IFNULL(MAX(`id`), 0) id FROM `" +
                Constant.Sync.SYNC_INFO_TABLE_PREFIX + "%s` UNION ";
        for (int i = 0; i < tableNames.size(); i++) {
            sb.append(String.format(selectTemplate, tableNames.get(i), dbName.get(i)));
        }
        sb.setLength(sb.length() - " UNION ".length());
        sb.append(";");
        return sb.toString();
    }

    /**
     * 说明：将传入的同步信息插入到数据库中
     * 实现步骤：
     *   1. 构建insert sql
     *   2. 执行sql
     * @param syncInfoMap 需要插入的同步信息
     * @throws SQLException
     * @since 0.0.0
     */
    public void insertSyncInfo(Map<String, SyncInfoEntity> syncInfoMap) throws SQLException {

        try (Connection conn = dataSource.getConnection();
             Statement ps = prepareInsertSyncInfoStatement(syncInfoMap, conn)) {
            conn.setAutoCommit(false);
            ps.executeBatch();
            conn.commit();
        }
    }

    /**
     * 说明：将传入的同步信息插入到数据库中
     * 实现步骤：
     *   1. 构建执行sql语句的PreparedStatement
     *   2. 返回ps
     * @param syncInfoMap 需要插入的同步信息
     * @param conn 数据库连接
     * @throws SQLException
     * @since 0.0.0
     */
    private Statement prepareInsertSyncInfoStatement(
            Map<String, SyncInfoEntity> syncInfoMap, Connection conn) throws SQLException {
        String insertSql = "INSERT INTO `%s` VALUES(%d, %d, %d, '%s');";
        Statement statement = conn.createStatement();

        for (Map.Entry<String, SyncInfoEntity> entry : syncInfoMap.entrySet()) {
            SyncInfoEntity value = entry.getValue();
            statement.addBatch(String.format(insertSql,
                    entry.getKey(),
                    value.getId(),
                    value.getTransaction().getId(),
                    value.getTransaction().getType(),
                    value.getSqlStatement()));
        }
        return statement;
    }

    /**
     * 说明：查询所有id大于传入id的row
     * 实现步骤：
     *   1. 判定当前id+1不存在则返回null
     *   2. 查询所有大于当前id的row
     *   3. 将结果封装为SyncInfoEntity，加入结果集返回
     *   4. 返回结果
     * @param dbName 数据库名
     * @param id 同步信息id
     * @return 所有id大于传入id的row
     * @throws SQLException
     * @since 0.0.0
     */
    public List<SyncInfoEntity> selectSyncInfoGT(String dbName, long id) throws SQLException {
        String syncInfoTable = Constant.Sync.SYNC_INFO_TABLE_PREFIX + dbName;
        List<SyncInfoEntity> res = null;
        String stat = "select `id` from `" + syncInfoTable  + "` where `id` = " + (id + 1) + ";";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(stat);
             ResultSet resultSet = ps.executeQuery()) {
            if (!resultSet.next()) return res;

            try (ResultSet rSet = ps.executeQuery("select * from `" + syncInfoTable + "` where `id` > " + id + ";")) {
                resolveSyncInfo(rSet, res = new ArrayList<>());
            }
        }

        return res;
    }

    /**
     * 说明：从结果集中解析出所有的syncInfo实例
     * @param rSet 数据库查询结果集
     * @param res 实例结果集
     * @since 0.0.0
     */
    private void resolveSyncInfo(ResultSet rSet, List<SyncInfoEntity> res) throws SQLException {
        while (rSet.next()) {
            res.add(new SyncInfoEntity(rSet.getLong(1),
                    new DBTransaction(rSet.getLong(2), rSet.getInt(3), (byte) 0),
                    rSet.getString(4)));
        }
    }
}












