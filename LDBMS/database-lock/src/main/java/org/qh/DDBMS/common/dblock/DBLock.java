package org.qh.DDBMS.common.dblock;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/18
 * @Version:
 * @Description:
 */

import org.qh.DDBMS.common.db.DBResource;
import org.qh.DDBMS.common.db.DBTransaction;

import java.util.List;

/**
 * 说明：提供封锁和解锁API的接口
 */
public interface DBLock {

    /**
     * 说明：用于记录一个事务需要对哪些资源进行封锁。
     * @param transaction 需要资源的事务
     * @param resource 需要封锁的资源
     */
    void register(DBTransaction transaction, DBResource resource);

    /**
     * 说明：对一个事务需要的资源进行封锁。
     * @param transaction 需要封锁的事务
     */
    boolean lock(DBTransaction transaction) throws Exception;

    /**
     * 说明：释放传入事务持有的所有的锁资源，并返回所有加锁的资源项。
     * @param transaction 需要解锁的事务
     * @return List<DBResource> 释放的资源列表
     */
    List<DBResource> unlock(DBTransaction transaction);

    /**
     * 说明：获取一个事务需要的资源列表
     * @param transaction 需要解锁的事务
     * @return List<DBResource> 释放的资源列表
     */
    List<DBResource> resources(DBTransaction transaction);


}

