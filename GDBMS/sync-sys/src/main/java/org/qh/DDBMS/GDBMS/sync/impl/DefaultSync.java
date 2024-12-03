package org.qh.DDBMS.GDBMS.sync.impl;

import org.qh.DDBMS.GDBMS.sync.Sync;
import org.qh.DDBMS.GDBMS.sync.dao.SyncDao;
import org.qh.DDBMS.common.entity.SyncInfoEntity;
import org.qh.tools.common.RandomUtils;
import org.qh.tools.thread.LockUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/18
 * @Version: 0.0.0
 * @Description: 默认的Sync接口实现实现
 * @Specification:
 *   1. 同步信息表名：sync_info_dbName
 *   2. 同步信息表中最大值id，就是对应数据库的执行事务数
 */
public class DefaultSync implements Sync {

    /**
     * <pre>
     * 说明：用于存储每个数据库的自增同步信息id
     * </pre>
     */
    private Map<String, AtomicLong> syncId;

    @Resource
    private SyncDao syncDao;

    private static final String LOCK_PREFIX = "DDBMS_GDBMS_sync_";

    /**
     * <pre>
     * 说明：初始化各个数据库的自增同步信息id
     * 实现步骤：
     *   1) 从数据库中获取所有表的名字
     *   2) 根据同步信息表的命名规范，取得所有数据库名
     *   3) 获取各个同步信息表最大id值
     *   4) 初始化syncId
     * </pre>
     */
    @PostConstruct
    public void init() throws SQLException {
        syncId = new ConcurrentHashMap<>();
        Map<String, Long> map = syncDao.selectSyncTableIds();
        for (Map.Entry<String, Long> entry : map.entrySet()) {
            syncId.put(entry.getKey(), new AtomicLong(entry.getValue()));
        }
    }

    /**
     * <pre>
     * 说明：保存一次提交交事务的同步信息
     * 实现步骤：
     *   1) 取得所有涉及的数据库同步锁
     *   2) 将所有同步信息转化为insert sql
     *   3) 执行sql
     *      1. 执行失败,则回退自增的id值，并直接返回false
     *   4) 释放取得的所有锁
     *   5) 返回true
     * </pre>
     *
     * @param syncInfoMap 数据库与同步信息的映射
     * @return 是否保存成功
     */
    public boolean save(Map<String, SyncInfoEntity> syncInfoMap) {
        List<String> dbLocks = new ArrayList<>(syncInfoMap.size());
        for (String dbName : syncInfoMap.keySet()) dbLocks.add(LOCK_PREFIX + dbName);
        try {
            lockDatabases(dbLocks);
            for (Map.Entry<String, SyncInfoEntity> entry : syncInfoMap.entrySet()){
                entry.getValue().setId(syncId.get(entry.getKey()).incrementAndGet());
            }
            syncDao.insertSyncInfo(syncInfoMap);

        } catch (SQLException e) {
            for (Map.Entry<String, SyncInfoEntity> entry : syncInfoMap.entrySet()) syncId.get(entry.getKey()).getAndDecrement();
        } finally {
            unlockDatabases(dbLocks);
        }

        return false;
    }

    /**
     * <pre>
     * 说明：取得传入的数据库名形式的db锁资源
     * 实现步骤：
     *   1. 对数据库名进行排序
     *   2. 进行加锁
     *   3. 循环地对所有的数据库进行加锁
     *     3.1. 只要有一个次加锁失败则释放所有锁
     *     3.2. 休息50ms-147ms后重新开始加锁
     *   4. 持有所有锁后才可以
     * </pre>
     * @param dbLocks 需要上的锁
     * @since 0.0.0
     */
    private void lockDatabases(List<String> dbLocks) {

        Collections.sort(dbLocks);
        int i = 0;
        for (i = 0; i < dbLocks.size(); i++) {
            if (!LockUtils.lock(dbLocks.get(i))) {
                i--;
                while (i >= 0) {
                    LockUtils.unlock(dbLocks.get(i));
                    i--;
                }
                try {
                    Thread.sleep(Math.max(50, (RandomUtils.instance().nextInt() % 50) * 3L));
                } catch (Exception e) {

                }
            }
        }
    }

    /**
     * 说明：释放持有的资源
     * @param dbLocks 数据库锁资源
     * @since 0.0.0
     */
    private void unlockDatabases(List<String> dbLocks) {
        for (String dbLock : dbLocks) {
            LockUtils.unlock(dbLock);
        }
    }

    /**
     * <pre>
     * 说明：检索database中last后所有已经提交的事务
     * 实现步骤：
     *   1) 检索所有大于传入提交事务同步信息id的同步数据
     *   2) 将得到的数据封装为SyncInfoEntity
     *   3) 返回结果
     * </pre>
     *
     * @param database 数据库名
     * @param lastSyncId 上次提交的同步信息id
     * @return 已提交的事务列表
     */
    public List<SyncInfoEntity> retrieve(String database, Long lastSyncId) throws SQLException {
        return syncDao.selectSyncInfoGT(database, lastSyncId);
    }


    /**
     * <pre>
     * 说明：得到一个数据库中执行事务数
     * 实现步骤：
     *   1) 返回syncId中对应数据库的id值sql
     * </pre>
     *
     * @param database 数据库名
     * @return 执行事务数
     */
    public long transactionCount(String database) {
        return syncId.get(database).get();
    }
}

