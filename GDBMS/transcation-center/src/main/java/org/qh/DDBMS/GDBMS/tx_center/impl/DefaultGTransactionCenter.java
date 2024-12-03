package org.qh.DDBMS.GDBMS.tx_center.impl;

import org.qh.DDBMS.GDBMS.msm.MasterSlaveManager;
import org.qh.DDBMS.GDBMS.sync.Sync;
import org.qh.DDBMS.GDBMS.tx_center.GTransactionCenter;
import org.qh.DDBMS.common.tx.TransactionExec;
import org.qh.DDBMS.common.Constant;
import org.qh.DDBMS.common.db.DBTransaction;
import org.qh.DDBMS.common.entity.SyncInfoEntity;
import org.qh.DDBMS.common.output.DDBMSSender;
import org.qh.DDBMS.common.protocol.ACKProtocol;
import org.qh.tools.exception.ExceptionUtils;
import org.qh.tools.thread.ThreadUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/19
 * @Version: 0.0.0
 * @Description: GTransactionCenter的默认实现类
 */
public class DefaultGTransactionCenter implements GTransactionCenter {
    /**
     * <pre>
     * 说明： 全局事务id生成器
     * </pre>
     */
    private static AtomicLong TRANSACTION_ID = new AtomicLong(0);

    /**
     * <pre>
     * 说明： 全局事务类型，也是具体的局部数据库编号
     * </pre>
     */
    private static int TRANSACTION_TYPE = 0;
    /**
     * <pre>
     * 说明： 全局事务类型，的默认优先级
     * </pre>
     */
    private static byte TRANSACTION_ORDER = 1;
    /**
     * <pre>
     * 说明：事务和客户端站点的的映射关系
     * </pre>
     */
    private Map<DBTransaction, String> client = new ConcurrentHashMap<>();

    /**
     * <pre>
     * 说明：事务与需要修改数据库的映射关系
     * </pre>
     */
    private Map<DBTransaction, Set<String>> updatedDB = new ConcurrentHashMap<>();

    /**
     * <pre>
     * 说明：等待GDBMS确认是否提交的事务。
     * </pre>
     */
    private Map<DBTransaction, Map<String, SyncInfoEntity>> waitTransaction = new ConcurrentHashMap<>();

    /**
     * <pre>
     * 说明：等待GDBMS确认撤销的事务。
     * </pre>
     */
    private Map<DBTransaction, Map<String, Integer>> failedTransaction = new ConcurrentHashMap<>();

    /**
     * <pre>
     * 说明：事务和其执行器之间的映射关系
     * </pre>
     */
    private Map<DBTransaction, TransactionExec> executor = new ConcurrentHashMap<>();

    /**
     * <pre>
     * 说明：用于发送协议
     * </pre>
     */
    @Resource
    private DDBMSSender<String> sender;

    /**
     * <pre>
     * 说明：同步系统，用于保存提交事务
     * </pre>
     */
    @Resource
    private Sync sync;

    /**
     * <pre>
     * 说明：用于获取数据库主站点
     * </pre>
     */
    @Resource
    private MasterSlaveManager ms;

    /**
     * <pre>
     * 说明：事务失败时的同步信息占位对象
     * </pre>
     */
    private static final SyncInfoEntity FAILURE = new SyncInfoEntity(null, null, null);

    @PostConstruct
    public void init() {
        if (TRANSACTION_ID != null) return;
    }

    /**
     * <pre>
     * 说明：创建一个事务
     * 实现步骤：
     *   1) 获取事务id，并构建事务实例
     *   2) 保存事务和client站点的映射关系
     *   3) 保存事务和执行器之间的关系
     *   4) 在新线程启动事务
     *   5) 返回创建的事务
     * </pre>
     *
     * @param clientName 客户端名称
     * @param transactionExec 事务执行器
     * @return 创建的事务实例
     */
    public DBTransaction createTransaction(String clientName, TransactionExec transactionExec) {
        DBTransaction dbTransaction = new DBTransaction(TRANSACTION_ID.getAndIncrement(), TRANSACTION_TYPE, TRANSACTION_ORDER);
        client.put(dbTransaction, clientName);
        executor.put(dbTransaction, transactionExec);
        ThreadUtils.execute(() -> transactionExec.exec(dbTransaction));
        return dbTransaction;
    }

    /**
     * <pre>
     * 说明：注册执行一个事务所需要修改哪些数据库
     * 实现步骤：
     *   1) 获取事务在site集合中的set
     *   2) 将数据库名加入到set中
     * </pre>
     *
     * @param transaction 事务实例
     * @param dbName 数据库名称
     */
    @Override
    public void registerUpdatedDB(DBTransaction transaction, String dbName) {
        if (!updatedDB.containsKey(transaction)) {
            updatedDB.put(transaction, ConcurrentHashMap.newKeySet());
        }
        updatedDB.get(transaction).add(dbName);
    }

    /**
     * <pre>
     * 说明：根据事务撤销原因执行撤销事务操作。
     * 实现步骤：
     *   1) 验证事务的合法性；
     *   2) 判断该事务不修改对应数据库则报错
     *   3) 判定事务已经在等待提交集合中或失败集合则忽略
     *   4) 将事务加入到failedTransaction中
     * </pre>
     * @param dbName 撤销事务的数据库名
     * @param transaction 事务实例
     * @param reason 撤销原因
     */
    @Override
    public void cancel(String dbName, DBTransaction transaction, int reason) {
        verifyGTransaction(transaction);
        if (!updatedDB.get(transaction).contains(dbName)) {
            throw new RuntimeException("The " + transaction + " do not update the " + dbName + " database.");
        }
        Map<String, Integer> failMap = failedTransaction.get(transaction);
        if (failMap == null) {
            failedTransaction.put(transaction, failMap = new ConcurrentHashMap<>());
        }
        Map<String, SyncInfoEntity> waitMap = waitTransaction.get(transaction);
        if ((waitMap != null && waitMap.containsKey(dbName)) ||
                failMap.containsKey(dbName)) return;

        failMap.put(dbName, reason);
    }

    /**
     * <pre>
     * 说明：验证事务是否合法
     * </pre>
     * @param transaction 待验证事务
     * @since 0.0.0
     */
    private void verifyGTransaction(DBTransaction transaction) {
        if (transaction.getType() != TRANSACTION_TYPE ||
                !executor.containsKey(transaction)) throw new RuntimeException("Invalid transaction.");
    }

    /**
     * <pre>
     * 说明：LDBMS同意提交事务。
     * 实现步骤：
     *   1) 判定提交的事务是一个局部事务，执行提交局部事务的业务逻辑
     *   2) 判定提交的事务是一个全局事务，执行提交全局事务的业务逻辑
     * </pre>
     *
     * @param dbName 数据库名称
     * @param transaction 事务实例
     * @param sqlState 数据库执行的sql语句
     */
    public void commit(String dbName, DBTransaction transaction, String sqlState) {
        if (transaction.getType() != TRANSACTION_TYPE) {
            commitLTransaction(dbName, transaction, sqlState);
        } else commitGTransaction(dbName, transaction, sqlState);
    }

    /**
     * 说明：尝试提交一个局部事务
     * 实现步骤：
     *   1. 将事务的提交信息交给sync模块保存
     *   2. 保存成功执行success
     *   3. 保存失败执行fail
     * @param dbName 数据库名
     * @param transaction 需要提交的事务
     * @param sqlState 事务在该数据库中执行的所有修改数据库的sql语句
     * @since 0.0.0
     */
    private void commitLTransaction(String dbName, DBTransaction transaction, String sqlState) {
        HashMap<String, SyncInfoEntity> map = new HashMap<>();
        map.put(dbName, new SyncInfoEntity(null, transaction, sqlState));
        if (sync.save(map)) success(transaction, map);
        else fail(transaction, Constant.TransactionCenter.FAILURE);
    }

    /**
     * <pre>
     * 说明：尝试提交全局事务
     * 实现步骤：
     *   1. 验证事务的有效性
     *   2. 判定当前事务不修改当前数据库，则返回false
     *   3. 获取当前事务对应的waitTransaction，或failedTransaction中已经保存当前
     *   数据库的提交信息，忽略本次提交请求
     *   4. 将本次提交信息封装为一个SyncInfo并以数据库名
     *   为key装入到waitMap中
     *   5. 尝试提交事务
     * </pre>
     *
     * @param dbName 数据库名称
     * @param transaction 事务实例
     * @param sqlState 数据库执行的sql语句
     * @since 0.0.0
     */
    private void commitGTransaction(String dbName, DBTransaction transaction, String sqlState) {
        verifyGTransaction(transaction);
        if (!updatedDB.containsKey(transaction)) return;
        if (!updatedDB.get(transaction).contains(dbName)) return;
        if (!waitTransaction.containsKey(transaction)) waitTransaction.put(transaction, new ConcurrentHashMap<>());
        Map<String, SyncInfoEntity> map = waitTransaction.get(transaction);
        if (map.containsKey(dbName) ||
                (failedTransaction.containsKey(transaction) &&
                        failedTransaction.get(transaction).containsKey(dbName))) {
            return;
        }

        map.put(dbName, new SyncInfoEntity(null, transaction, sqlState));

        doCommitGTransaction(transaction);
    }

    /**
     * <pre>
     * 说明：提交全局事务
     * 实现步骤：
     *   1. 判定当前waitMap+failMap的size等于当前事务需要修改数据库的数量，则进行提交
     *     - 判定事务对应的failMap不为null，
     *       > 获取失败原因值中较大的失败原因reason
     *       > 告知所有数据库本次事务执行失败了、
     *     - 判定判定事务对应的failMap为null，
     *       > 将所有的提交信息均交给Sync进行保存
     *       > 保存成功：执行success
     *       > 保存失败：执行fail
     * </pre>
     *
     * @param transaction 事务实例
     * @since 0.0.0
     */
    private void doCommitGTransaction(DBTransaction transaction) {
        Map<String, SyncInfoEntity> waitMap = waitTransaction.get(transaction);
        Map<String, Integer> failMap = failedTransaction.get(transaction);
        int failSize = failMap == null ? 0 : failMap.size();
        if (!(waitMap.size() + failSize == updatedDB.get(transaction).size())) return;
        if (waitMap.size() + failSize > updatedDB.get(transaction).size()) {
            fail(transaction, Constant.TransactionCenter.FAILURE);
            throw new RuntimeException("The transaction is living a bad state.");
        }

        int failReason = failOrCommit(failMap);
        if (failReason == Constant.TransactionCenter.COMMIT && sync.save(waitMap)) {
            success(transaction, waitMap);
        } else {
            fail(transaction,
                    failReason == Constant.TransactionCenter.COMMIT ?
                            Constant.TransactionCenter.FAILURE : failReason);
        }

    }

    /**
     * 说明：判定一个事务是失败还是允许提交
     * 实现步骤：
     *   1. 判定failMap为null，则返回COMMIT
     *   2. 获取failMap中失败原因的最大值返回
     * @param failMap 一个事务的失败记录map
     * @return 0表示允许提交，其他表示失败
     * @since 0.0.0
     */
    private int failOrCommit(Map<String, Integer> failMap) {
        if (failMap == null) return Constant.TransactionCenter.COMMIT;
        int max = 0;
        for (int reason : failMap.values()) {
            max = Math.max(max, reason);
        }
        return max;
    }

    /**
     * <pre>
     * 说明：事务执行失败进行必要的操作
     * 实现步骤：
     *   1) 告知所有的LDBMS撤销事务
     *   2) 判定封锁失败，执行封锁失败操作(不可能是局部事务)
     *     2.1. 清空事务部分缓存
     *     2.2. 重新执行当前事务
     *   3) 判定事务无法正常执行，执行异常失败操作
     *     3.1. 判定该事务是一个全局事务
     *       3.1.1. 告知client事务执行失败
     *       3.1.2. 删除事务的所有缓存
     * </pre>
     *
     * @param transaction 事务实例
     * @param reason      撤销原因
     */
    private void fail(DBTransaction transaction, int reason) {
        notifyLDBMS(transaction,
                updatedDB.get(transaction).stream()
                        .collect(Collectors.toMap(db -> db, db -> FAILURE)),
                Constant.ACKType.CANCEL_DBTRANSACTION, true);

        if (reason == Constant.TransactionCenter.LOCK_FAILURE) {
            updatedDB.remove(transaction);
            waitTransaction.remove(transaction);
            failedTransaction.remove(transaction);
            ThreadUtils.schedule(() -> executor.get(transaction).exec(transaction),
                    Constant.TransactionCenter.LOCK_FAILURE_REBOOT_INTERVAL,
                    TimeUnit.MILLISECONDS);
        } else if (reason == Constant.TransactionCenter.FAILURE) {
            if (transaction.getType() != TRANSACTION_TYPE) return;
            notifyClient(client.get(transaction), transaction, Constant.ACKType.DBTRANSACTION_FAILURE, true);
            removeTransaction(transaction);
        }
    }

    /**
     * <pre>
     * 说明：事务成功执行
     * 实现步骤：
     *   1) 获取事务对应的client
     *   2) 告知事务执行成功
     *   3) 告知LDBMS，事务允许提交
     *   4) 清除事务有关缓存信息
     * </pre>
     *
     * @param transaction 事务实例
     */
    private void success(DBTransaction transaction, Map<String, SyncInfoEntity> map) {
        notifyClient(client.get(transaction), transaction, Constant.ACKType.DBTRANSACTION_SUCCESS, false);
        notifyLDBMS(transaction, map, Constant.ACKType.RESPONSE_COMMIT_TYPE, false);
        removeTransaction(transaction);
    }

    /**
     * 说明：将事务是否可以提交通知到client
     * 实现步骤：
     *   1. client为null，则返回（不是全局事务）
     *   2. 构建通知事务提交成功的ACK协议，并将协议内容发送出去
     * @param transaction 需要提交的事务
     *
     * @since 0.0.0
     */
    private void notifyClient(String clientName, DBTransaction transaction, short ackType, boolean sync) {
        if (clientName == null) return;
        try {
            sender.send(clientName, new ACKProtocol(ackType, transaction), null, sync);
        } catch (Exception e) {
            ExceptionUtils.printStackTrace(e);
        }
    }

    /**
     * 说明：将事务是否可以提交通知到各个LDBMS
     * 实现步骤：
     *   1. 为每个LDBMS准备ACK协议
     *   2. 将协议内容发送出去
     * @param transaction 需要提交的事务
     * @param map 数据库和同步信息的映射关系
     * @since 0.0.0
     */
    private void notifyLDBMS(DBTransaction transaction, Map<String, SyncInfoEntity> map, short ackType, boolean sync) {
        for (Map.Entry<String, SyncInfoEntity> entry : map.entrySet()) {
            ACKProtocol p = new ACKProtocol(ackType, transaction, entry.getValue().getId());
            try {
                sender.send(ms.masterSite(entry.getKey()), p, null, sync);
            } catch (Exception e) {
                ExceptionUtils.printStackTrace(e);
            }
        }
    }

    /**
     * 说明：移除一个事务
     * @param transaction 需要移除的事务
     * @since 0.0.0
     */
    private void removeTransaction(DBTransaction transaction) {
        client.remove(transaction);
        updatedDB.remove(transaction);
        waitTransaction.remove(transaction);
        failedTransaction.remove(transaction);
        executor.remove(transaction);
    }
}

