package org.qh.DDBMS.LDBMS.ms_sync.dao;

import org.qh.DDBMS.common.Constant;
import org.qh.DDBMS.common.db.DBTransaction;
import org.qh.DDBMS.common.entity.SyncInfoEntity;
import org.qh.DDBMS.common.input.ServerConfig;
import org.qh.tools.clz.LauncherUtils;
import org.qh.tools.exception.ExceptionUtils;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    /**
     * <pre>
     * 说明：获取数据库连接的数据源
     * </pre>
     */
    @Resource
    private DataSource dataSource;

    /**
     * <pre>
     * 说明：用于获取当前站点负责的数据库名
     * </pre>
     */
    @Resource
    private ServerConfig serverConfig;


    /**
     * <pre>
     * 说明：提交同步信息到数据库
     * 实现步骤：
     *   1. 构建写入同步性信息的sql语句
     *   2. 构建更新数据库的sql语句
     *   3. 获取连接，执行sql
     *   4. 发生异常则，打印异常，并打印无法进行信息，关闭服务
     * </pre>
     * @param info 同步信息
     * @since 0.0.0
     */
    public void commitSyncInfo(SyncInfoEntity info) {
        String insertSyncInfo = "INSERT INTO `" + Constant.Sync.SYNC_INFO_TABLE_PREFIX +
                serverConfig.dbName() +
                "` VALUES(?, ?, ?, ?);";
        String[] updateDB = info.getSqlStatement().split(Constant.Sync.SQL_STATEMENT_SEPARATOR);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSyncInfo)) {
            conn.setAutoCommit(false);
            ps.setLong(1, info.getId());
            ps.setLong(2, info.getTransaction().getId());
            ps.setInt(3, info.getTransaction().getType());
            ps.setString(4, info.getSqlStatement());
            ps.addBatch();
            for (String sqlStatement : updateDB){
                ps.addBatch(sqlStatement);
            }
            ps.executeBatch();
            conn.commit();
        } catch (Exception e) {
            ExceptionUtils.printStackTrace(e);
            LauncherUtils.destroy(); // 关闭服务
        }

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
    public List<SyncInfoEntity> selectSyncInfo(String dbName, long id) throws SQLException {
        List<SyncInfoEntity> res = null;
        String stat = "select `id` from `" + Constant.Sync.SYNC_INFO_TABLE_PREFIX + dbName + "` where `id` = " + (id + 1) + ";";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(stat);
             ResultSet resultSet = ps.executeQuery()) {
            if (!resultSet.next()) return res;

            try (ResultSet rSet = ps.executeQuery("select * from `" + dbName + "` where `id` > " + id + ";")) {
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
                    new DBTransaction(rSet.getLong(2), rSet.getByte(3), (byte) 0),
                    rSet.getString(4)));
        }
    }
}












