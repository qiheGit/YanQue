package org.qh.DDBMS.GDBMS.sync;

import org.qh.DDBMS.common.entity.SyncInfoEntity;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/18
 * @Version: 0.0.0
 * @Description: 同步系统对外接口
 */
public interface Sync {

    /**
     * <pre>
     * 说明：保存一次提及的同步信息
     * </pre>
     * @param syncInfoMap Map&lt;String, SyncInfo&gt; 同步信息的映射
     * @return boolean 保存操作是否成功
     * @since 0.0.0
     */
    boolean save(Map<String, SyncInfoEntity> syncInfoMap);

    /**
     * <pre>
     * 说明：检索database中last后所有已经提交的事务
     * </pre>
     * @param last 数据库中最后提交事务的序号
     * @param database 数据库
     * @return 检索到的同步信息列表
     * @since 0.0.0
     */
    List<SyncInfoEntity> retrieve(String database, Long last) throws SQLException;

    /**
     * <pre>
     * 说明：得到一个数据库中执行事务数
     * </pre>
     * @param dbName String 数据库名称
     * @return long 执行的事务数量
     * @since 0.0.0
     */
    long transactionCount(String dbName);



}
