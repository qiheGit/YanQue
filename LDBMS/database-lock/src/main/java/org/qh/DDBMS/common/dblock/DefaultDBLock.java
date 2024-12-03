package org.qh.DDBMS.common.dblock;

import com.qh.exception.MethodParameterException;
import lombok.var;
import org.qh.DDBMS.common.Constant;
import org.qh.DDBMS.common.db.DBResource;
import org.qh.DDBMS.common.db.DBTransaction;
import org.qh.tools.thread.LockUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/19
 * @Version: 0.0.0
 * @Description: DBLock的默认实现类
 */
public class DefaultDBLock implements DBLock {

    // 该属性是一个事务与其需要获得资源的映射关系
    private Map<DBTransaction, List<DBResource>> transactionResourceMap;

    // 该属性是资源和加锁事务之间的映射关系
    private Map<DBResource, DBTransaction> resourceMap;

    // 该属性是资源和其等待者之间的映射关系
    private Map<DBResource, DBTransaction> waiter;

    // 该属性是等待表资源与等待表中行资源事务之间的映射关系
    private Map<Integer, TreeSet<DBTransaction>> tableWaiterMap;

    // 该属性是表资源与已封锁表中资源事务之间的映射关系
    private Map<Integer, TreeSet<DBTransaction>> tableOwnerMap;

    // 该属性是一个等待事务和其执行线程的映射关系
    private Map<DBTransaction, Thread> transactionThreadMap;

    /**
     * <pre>
     * 说明：初始化字段的方法
     * 实现步骤：
     *   1) 为所有属性赋值
     * <pre/>
     * @since 0.0.0
     */
    public void init() {
        transactionResourceMap = new ConcurrentHashMap<>();
        resourceMap = new ConcurrentHashMap<>();
        waiter = new ConcurrentHashMap<>();
        tableWaiterMap = new ConcurrentHashMap<>();
        tableOwnerMap = new ConcurrentHashMap<>();
        transactionThreadMap = new ConcurrentHashMap<>();
    }

    /**
     * <pre>
     * 说明：该方法用于一个事务对所要加锁的资源进行注册
     * 注意：需要对事务加同步锁
     * 实现步骤：
     *   1) 判定事务存在于transactionResourceMap中
     *     1. 获取事务对应的List<DBResource>,
     *     2. 将当前传入的DBResource加入list
     *   2) 事务不存在于transactionResourceMap中
     *     1. 构建一个List<DBResource>
     *     2. 将当前传入的DBResource加入list
     *     3. 线程同步地将事务和list的映射关系加入到transactionResourceMap
     * <pre/>
     * @param transaction 需要注册的事务
     * @param resource 要加锁的资源
     * @since 0.0.0
     */
    @Override
    public void register(DBTransaction transaction, DBResource resource) {
        try {
            lockTransaction(transaction);
            List<DBResource> resourceList = transactionResourceMap.get(transaction);
            if (resourceList == null) {
                transactionResourceMap.put(transaction, resourceList = new ArrayList<>());
            }
            resourceList.add(resource);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            unlockTransaction(transaction);
        }

    }


    /**
     * <pre>
     * 说明：该方法对于事务所需要加锁的资源真正的进行加锁
     * 实现步骤：
     *   1) 判定transactionResourceMap中不存在传入的事务，
     *     则抛出参数异常"The transaction need not lock any DBResource."
     *   2) 对资源进行封锁
     *   3) 返回封锁结果。
     * </pre>
     * @param transaction 需要加锁的事务
     * @return boolean 加锁是否成功
     * @since 0.0.0
     */
    public boolean lock(DBTransaction transaction) throws Exception {
        boolean result = false;
        try {
            lockTransaction(transaction);
            List<DBResource> resourceList = transactionResourceMap.get(transaction); // 获取事务对应的List<DBResource>
            if (resourceList == null)
                throw new MethodParameterException("The transaction need not lock any DBResource."); // 抛出参数异常
            result = doLock(transaction, resourceList);
        } finally {
            unlockTransaction(transaction);
        }
        if (!result) unlock(transaction);
        return result;
    }


    /**
     * <pre>
     * 说明；封锁一个事务需要的资源
     * 实现步骤：
     *   1) 获取事务对应的List<DBResource>
     *   2) 按先后顺序循环取出DBResource
     *     1. 判定当前DBResource是表资源则调用lockTable方法。
     *       - 判定条件：rowID == null
     *     2. 判断当前DBResource是行资源则调用lockRow方法
     *       - 判定条件：rowId和tableId均不为null
     *     3. 其他情况抛出异常
     *   3) 只要有一个资源封锁失败，则释放当前事务获取的所有资源，并返回false
     * </pre>
     * @param transaction 一个数据库事务
     * @param resourceList 资源列表
     * @return boolean 加锁是否成功
     * @since 0.0.0
     */
    private boolean doLock(DBTransaction transaction, List<DBResource> resourceList) throws InterruptedException {
        boolean lockSuccessful = true;
        lockTransaction(transaction);
        for (DBResource resource : resourceList) {
            if (resource.isTable()) { // 判定当前DBResource是表资源
                lockSuccessful = lockTable(transaction, resource);
            } else if (resource.isRow()) { // 判断当前DBResource是行资源
                lockSuccessful = lockRow(transaction, resource);
            } else {
                throw new IllegalArgumentException("Invalid resource type."); // 其他情况抛出异常
            }
            if (!lockSuccessful) break;
        }
        return lockSuccessful; // 返回封锁结果
    }


    /**
     * <pre>
     * 说明：该方法用于释放一个事务所持有资源的锁
     * 实现步骤：
     *   1) 判定当前事务不存在于transactionResourceMap中，则抛出参数异常，
     *     "The transaction does not need lock any DBResource."
     *   2) 获取该事务对应的资源列表
     *   3) 释放资源
     *   4) 返回list
     * </pre>
     * @param transaction 需要释放锁的事务
     * @return List<DBResource> 释放的资源列表
     * @since 0.0.0
     */
    public List<DBResource> unlock(DBTransaction transaction) {
        try {
            lockTransaction(transaction);
            // 1) 判定当前事务不存在于transactionResourceMap中
            List<DBResource> resourceList = transactionResourceMap.remove(transaction);
            if (resourceList == null)
                throw new IllegalArgumentException("The transaction need not lock any DBResource."); // 抛出参数异常

            doUnlock(resourceList, transaction);
            return resourceList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            unlockTransaction(transaction);
        }
    }

    /**
     * <pre>
     * 说明：获取一个事务需要的资源列表
     * 实现步骤：
     *   1. 从transactionResourceMap获取事务的资源列表list
     *   2. 将list封装为不可更改的list，并返回
     * </pre>
     * @param transaction 需要解锁的事务
     * @return transaction对应的资源列表
     * @since 0.0.0
     */
    @Override
    public List<DBResource> resources(DBTransaction transaction) {
        return Collections.unmodifiableList(transactionResourceMap.get(transaction));
    }

    /**
     * <pre>
     * 说明：释放资源
     * 实现步骤：
     *   1) 按依次循环取出每个DBResource
     *     1. 判定当前资源没有被当前事务所获取则跳过该资源
     *     2. 判定当前DBResource是表资源，则调用unlockTable方法
     *     3. 判断当DBResource是行资源，则调用unlockRow方法
     *   2) 将事务对表资源或表中行资源的拥有关系删除
     * </pre>
     * @param resourceList 需要释放的资源列表
     * @since 0.0.0
     */
    private synchronized void doUnlock(List<DBResource> resourceList, DBTransaction transaction) {
        HashMap<Integer, DBResource> tableMap = new HashMap<>();
        for (DBResource resource : resourceList) {
            if (!transaction.equals(resourceMap.get(resource))) continue;
            if (resource.isTable()) unlockTable(resource);
            else unlockRow(resource);

            resourceMap.remove(resource);
            removeOwnerTransaction(
                    tableMap.getOrDefault(resource.getTableId(), new DBResource(resource.getTableId(), 0L)),
                    transaction);
        }
    }


    /**
     * <pre>
     * 说明：该方法用于一个事务对表资源进行加锁
     * 注意事项：该方法为同步方法
     * 实现步骤：
     *   1) 判定当前表资源或表中行资源，已经被事务锁定
     *     1. 判定当前事务比锁定资源的最年老事务年轻
     *       - 返回false
     *     2. 等待其他事务释放资源。
     *     3. 判定当前事务不允许等待该资源，返回false。
     *     4. waited=true,表示当前事务已经等待过该资源。
     *   2) 当前事务获取表锁
     * </pre>
     * @param transaction 需要加锁的事务
     * @param table 表资源
     * @return boolean 加锁是否成功
     * @since 0.0.0
     */
    private synchronized boolean lockTable(DBTransaction transaction, DBResource table) {
        boolean waited = false;
        while (true) {
            DBTransaction olderOwnerTransaction = getOlderOwnerTransaction(table);
            if (olderOwnerTransaction == null) break;
            if (olderOwnerTransaction.older(transaction)) return false; // 当前事务更加年轻
            // 等待其他事务释放资源
            if (!waitTable(transaction, table, waited)) return false;
            waited = true;
        }
        // 对表资源进行封锁
        lockTable0(transaction, table, waited);
        return true;
    }

    /**
     * <pre>
     * 说明：真正封锁表资源
     * 实现步骤：
     *   1) 将该表资源和当前事务的关系映射到resourceMap中。
     *   2) 判定waited==true
     *     1. 从waiter中删除该表和该事务的等待关系。
     *     2. 从tableWaiterMap中删除表资源和当前事务的最年老等待关系
     *     3. 从transactionThreadMap删除当前事务和线程的关系
     *   3) 将当前事务和当前表资源的最年老资源封锁关系，push到tableOldOwnerMap中
     * </pre>
     * @param transaction 需要加锁的事务
     * @param table 表资源
     * @param waited 当前事务是否已经等待过当前资源
     * @since 0.0.0
     */
    private void lockTable0(DBTransaction transaction, DBResource table, boolean waited) {
        resourceMap.put(table, transaction);
        putTITOMap(transaction, table);

        if (waited) {
            waiter.remove(table);
            removeWaitTransaction(table, transaction);
            transactionThreadMap.remove(transaction);
        }

    }

    /**
     * <pre>
     * 说明：等待其他事务释放该表资源
     * 实现步骤：
     *   1) 判定存在事务正在等待该表资源或表中的行资源
     *     1. 从tableWaiterMap中获取表和行资源等待的最年老的事务oldTransaction
     *     2. 判定oldTransaction == null || oldTransaction就是当前事务
     *       - 什么也不做
     *     3. 判定当前事务比oldTransaction年轻
     *       - 判定waited==true,
     *         > 从transactionThreadMap删除当前事务和线程的关系
     *       - 返回false
     *     4. 判定当前事务比oldTransaction更年老
     *       - 唤醒所有等待该表和表中行资源的事务
     *       - 清空tableWaiterMap中对于该表资源和事务的映射关系。
     *   2) 判定不存在事务正在等待该表资源或表中的行资源,则不作为
     *   3) 阻塞当前线程等待其他事务释放资源
     * </pre>
     * @param transaction 需要加锁的事务
     * @param table 表资源
     * @param waited 当前事务是否已经等待过当前资源
     * @return boolean 是否允许等待该资源
     * @return 不允许 false， 允许true
     * @since 0.0.0
     */
    private boolean waitTable(DBTransaction transaction, DBResource table, boolean waited) {
        DBTransaction olderWaitTransaction = getOlderWaitTransaction(table);
        if (olderWaitTransaction == null || transaction
                .equals(olderWaitTransaction)) ;
        else if (olderWaitTransaction.older(transaction)) {
            if (waited) {
                transactionThreadMap.remove(transaction);
                return false;
            }
        } else {
            wakeUpWaitingThread(table);
            tableWaiterMap.get(table.getTableId()).clear();
        }
        waitTable0(transaction, table);
        return true;
    }

    /**
     * <pre>
     * 说明：唤醒等待有关此表资源的所有事务
     * </pre>
     * @param table 表资源
     * @since 0.0.0
     */
    private void wakeUpWaitingThread(DBResource table) {
        TreeSet<DBTransaction> dbTransactions = tableWaiterMap.get(table.getTableId());
        if (dbTransactions == null) return;
        for (DBTransaction transaction : dbTransactions) {
            transactionThreadMap.get(transaction).interrupt();
        }
    }

    /**
     * <pre>
     * 说明：阻塞当前线程等待其他事务释放资源
     * 实现步骤：
     *   1) 将该表资源和该事务的等待关系，映射到waiter中
     *   2) 将当前事务加入tableWaiterMap中，表示该事务正在等待与该表资源有关的资源
     *   3) 将当前事务和线程的关系映射到transactionThreadMap中。
     *   4) 阻塞当前线程
     * </pre>
     * @param transaction 需要加锁的事务
     * @param table 表资源
     * @since 0.0.0
     */
    private void waitTable0(DBTransaction transaction, DBResource table) {
        waiter.put(table, transaction);
        putTITWMap(transaction, table);
        transactionThreadMap.put(transaction, Thread.currentThread());
        try {
            wait();
        } catch (InterruptedException e) {
        }
    }

    /**
     * <pre>
     * 说明：获取等待当前表资源或当前表中行资源的最年老事务
     * 实现步骤：
     *   1) 获取表对应所有等待事务
     *   2) 返回所有事务中最年老的事务
     * </pre>
     * @param table 表资源
     * @return 等待有关该表资源的最年老事务
     * @since 0.0.0
     */
    private DBTransaction getOlderWaitTransaction(DBResource table) {
        TreeSet<DBTransaction> tree = tableWaiterMap.get(table.getTableId());
        return tree.first();
    }

    /**
     * <pre>
     * 说明：获取封锁当前表资源或当前表中行资源的最年老事务
     * 实现步骤：
     *   1) 获取所有封锁有关该表资源的事务
     *   2) 返回所有事务中最年老的事务
     * </pre>
     * @param table table 表资源
     * @return 封锁有关该表资源的最年老事务
     * @since 0.0.0
     */
    private DBTransaction getOlderOwnerTransaction(DBResource table) {
        TreeSet<DBTransaction> tree = tableOwnerMap.get(table.getTableId());
        return tree.first();
    }

    /**
     * <pre>
     * 说明：移除等待当前表资源或当前表中行资源的指定事务
     * 实现步骤：
     *   1) 获取表对应所有等待事务
     *   2) 移除指定事务
     * </pre>
     * @param table 表资源
     * @param transaction 需要被移除的事务
     * @since 0.0.0
     */
    private void removeWaitTransaction(DBResource table, DBTransaction transaction) {
        TreeSet<DBTransaction> tree = tableWaiterMap.get(table.getTableId());
        tree.remove(transaction);
    }

    /**
     * <pre>
     * 说明：移除封锁当前表资源或当前表中行资源的指定事务
     * 实现步骤：
     *   1) 获取表对应所有等待事务
     *   2) 移除指定事务
     * </pre>
     * @param table 表资源
     * @param transaction 需要被移除的事务
     * @since 0.0.0
     */
    private void removeOwnerTransaction(DBResource table, DBTransaction transaction) {
        TreeSet<DBTransaction> tree = tableOwnerMap.get(table.getTableId());
        tree.remove(transaction);
    }

    /**
     * <pre>
     * 说明：
     *   1) 存入事务与tableOwnerMap的关系
     *   2) 方法全名putTITOMap(put Transaction Into Table Owner Map)
     * </pre>
     * @param transaction 需要与表有关资源的事务
     * @param table 表资源
     * @since 0.0.0
     */
    private void putTITOMap(DBTransaction transaction, DBResource table) {
        putTransactionIntoTableMap(transaction, table, tableOwnerMap);
    }

    /**
     * <pre>
     * 说明：
     *   1) 存入事务与tableWaiterMap的关系
     *   2) 方法全名putTITWTMap(put Transaction Into Table Waiter Map)
     * </pre>
     * @param transaction 需要与表有关资源的事务
     * @param table 表资源
     * @since 0.0.0
     */
    private void putTITWMap(DBTransaction transaction, DBResource table) {
        putTransactionIntoTableMap(transaction, table, tableWaiterMap);
    }


    /**
     * <pre>
     * 说明：将事务与表的关系装入指定Map中
     * 实现步骤：
     *   1) 获取map中表对应的set
     *   2) 将当前事务装入set 
     * </pre>
     * @param transaction 与表发生关系的事务
     * @param table 表资源
     * @param map 关系
     * @since 0.0.0
     */
    private void putTransactionIntoTableMap(DBTransaction transaction, DBResource table,
                                            Map<Integer, TreeSet<DBTransaction>> map) {
        Set<DBTransaction> set = map.computeIfAbsent(table.getTableId(), k -> new TreeSet<>());
        set.add(transaction);
    }


    /**
     * <pre>
     * 说明：该方法使一个事务锁定一个表中的行资源
     * 注意事项：该方法为同步方法。
     * 实现步骤：
     *   1) 判定存在事务已经锁定表资源或当前行资源
     *     1. 获取封锁资源的事务lockedTransaction
     *     2. 当前事务比lockedTransaction年轻
     *       - 返回false
     *     3. 等待行资源被其他事务释放
     *   2) waited设置为true，表示当前事务已经等待过该资源。
     *   3) 锁定行资源
     * </pre>
     * @param transaction 需要加锁的事务
     * @param row 行资源
     * @return false 该事务封锁失败， true 该事务封锁成功
     * @since 0.0.0
     */
    private synchronized boolean lockRow(DBTransaction transaction, DBResource row) {
        boolean waited = false;
        DBResource table = new DBResource(row.getTableId(), null);
        DBTransaction lockedTransaction = null;
        while (true) {
            lockedTransaction = resourceMap.getOrDefault(row, resourceMap.get(table));
            if (lockedTransaction == null) break;
            if (lockedTransaction.older(transaction)) return false;
            if (!waitRow(transaction, row, table, waited)) return false;
            waited = true;
        }
        lockRow0(transaction, row, waited);
        return true;
    }

    /**
     * <pre>
     * 说明：锁定行资源。
     * 实现步骤：
     *   1) 判定waited==true
     *     1. 从waiter中移除当前事务和行资源的等待关系。
     *     2. 从tableWaiterMap中移除当前事务和表资源的关系。
     *     3. 从transactionThreadMap中移除当前事务和线程的关系。
     *   2) 将行资源和当前事务的关系映射到resourceMap中。
     *   3) 将当前事务和表资源的关系加入到tableOwnerMap。
     *   4) 返回true。
     * </pre>
     * @param transaction 需要加锁的事务
     * @param row 行资源
     * @since 0.0.0
     */
    private void lockRow0(DBTransaction transaction, DBResource row, boolean waited) {
        if (waited) {
            waiter.remove(row);
            removeWaitTransaction(new DBResource(row.getTableId(), null), transaction);
            transactionThreadMap.remove(transaction);
        }
        resourceMap.put(row, transaction);
        putTITOMap(transaction, row);
    }

    /**
     * <pre>
     * 说明：等待行资源被其他事务释放
     * 实现步骤：
     *   1) 获取等待该表资源的事务wt(Wait Transaction)
     *   2) 判定wt==null
     *     1. wt赋值为等待该行资源的事务。
     *   3) wt就是当前事务或wt是null，则什么也不干。
     *   4) wt!=null
     *     1. 判定当前事务比wt年轻
     *       - 判定waited==true
     *         > 从transactionThreadMap中删除当前事务和线程的关系
     *         > 判定当前行资源的waiter是当前事务，则从waiter中删除当前事务
     *       - 返回false
     *     2. 判定当前事务比wt年老
     *       - 唤醒wt对应线程
     *       - 从tableWaitMap将相关表资源和wt的关系删除
     *   5) 阻塞当前线程等待其他事务释放资源
     * <pre/>
     * @param transaction 需要加锁的事务
     * @param row 行资源
     * @param table row所在的表资源
     * @param waited 是否等待过行资源
     * @return false 当前事务不允许等待该资源， true 允许等待该资源
     * @since 0.0.0
     */
    private boolean waitRow(DBTransaction transaction, DBResource row,
                            DBResource table, boolean waited) {
        DBTransaction wt = waiter.getOrDefault(row, waiter.get(table));
        if (wt == null || wt.equals(transaction)) ;
        else if (wt.older(transaction)) {
            if (waited) {
                transactionThreadMap.remove(transaction);
                if (transaction.equals(waiter.get(row))) waiter.remove(row);
            }
            return false;
        } else {
            transactionThreadMap.get(wt).interrupt();
            removeWaitTransaction(table, wt);
        }
        waitRow0(transaction, row, table);
        return true;
    }

    /**
     * <pre>
     * 说明：真正阻塞当前线程等待其他事务释放资源
     * 实现步骤：
     *   1) 将当前事务和资源的等待关系注册到waiter中
     *   2) 将当前事务和表资源的等待关系加入tableWaiterMap中
     *   3) 将当前事务和执行线程的关系映射到transactionThreadMap中。
     *   4) 阻塞当前线程。
     * <pre/>
     * @param transaction 需要加锁的事务
     * @param row 行资源
     * @param table 表资源
     * @since 0.0.0
     */
    private void waitRow0(DBTransaction transaction, DBResource row, DBResource table) {
        waiter.put(row, transaction);
        putTITWMap(transaction, table);
        transactionThreadMap.put(transaction, Thread.currentThread());
        try {
            wait();
        } catch (InterruptedException e) {
        }
    }


    /**
     *<pre>
     * 说明：该方法用于释放一个表资源的锁
     * 注意实现：方法内要求部分代码同步
     * 实现步骤：
     *   1) 将该表资源和事务的关系从resourceMap中删除。
     *   2) 同步
     *   3) 判定当前有事务在等待该表有关资源，则唤醒等待的事务。
     *   4) 取消同步
     *
     * @param table 表资源
     * @return void
     * @since 0.0.0
     */
    private void unlockTable(DBResource table) {
        synchronized (this) {
            wakeUpWaitingThread(table);
        }
    }


    /**
     * <pre>
     * 说明：该方法用于释放一个行资源的锁
     * 注意事项：方法内要求部分代码同步
     * 实现步骤：
     *   1) 将行资源和事务的映射关系从resourceMap中移除。
     *   2) 同步
     *   3) 判定当前有事务在等待表资源或行资源，则唤醒等待事务。
     *   4) 取消同步
     * </pre>
     * @param row 行资源
     * @return void
     * @since 0.0.0
     */
    private void unlockRow(DBResource row) {
        DBResource table = new DBResource(row.getTableId(), null);
        synchronized (this) {
            DBTransaction wt = waiter.getOrDefault(row, waiter.get(table));
            if (wt != null) {
                transactionThreadMap.get(wt).interrupt();
            }
        }
    }

    /**
     * <pre>
     * 说明：对事务进行加锁
     * 实现步骤：
     *   1. 得到本模块事务锁
     *   2. 进行上锁
     * </pre>
     * @param transaction 要操作的事务
     * @since 0.0.0
     */
    private void lockTransaction(DBTransaction transaction) throws InterruptedException {
        String tranDBLock = Constant.DBLock.DBLOCK_PREFIX + transaction.getId() + "_" + transaction.getType();
        LockUtils.waitLock(tranDBLock);
    }

    /**
     * <pre>
     * 说明: 释放得到的事务锁
     * 实现步骤：
     *   1. 得到本模块事务锁
     *   2. 进行解锁
     * </pre>
     * @param transaction 要操作的事务
     * @since 0.0.0
     */
    private void unlockTransaction(DBTransaction transaction) {
        String tranDBLock = Constant.DBLock.DBLOCK_PREFIX + transaction.getId() + "_" + transaction.getType();
        LockUtils.unlock(tranDBLock);
    }
}
