package org.qh.DDBMS.LDBMS.tx.impl;

import org.qh.DDBMS.LDBMS.tx.DoCommit;
import org.qh.DDBMS.LDBMS.tx.LTransactionCenter;
import org.qh.DDBMS.LDBMS.tx.LTransactionConfig;
import org.qh.DDBMS.common.Constant;
import org.qh.DDBMS.common.config.GDBMSConfig;
import org.qh.DDBMS.common.db.DBTransaction;
import org.qh.DDBMS.common.input.ServerConfig;
import org.qh.DDBMS.common.output.DDBMSSender;
import org.qh.DDBMS.common.protocol.ACKProtocol;
import org.qh.DDBMS.common.tx.TransactionExec;
import org.qh.tools.thread.ThreadUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


/**
 *
 * @Author: qihe
 * @Date: 2024/11/25
 * @Version: 0.0.0
 * @Description: LTransactionCenter接口的默认实现类
 */

public class DefaultLTransactionCenter implements LTransactionCenter {

    /**
     * <pre>
     * 说明：当前数据库提交的事务数
     * </pre>
     */
    private AtomicLong transactionCount;

    /**
     * <pre>
     * 说明：局部事务默认的优先级
     * </pre>
     */
    private static final byte DEFAULT_ORDER = 0;

    /**
     * <pre>
     * 说明：局部事务的ID生成器
     * </pre>
     */
    private AtomicLong transactionId = new AtomicLong(0);

    /**
     * <pre>
     * 说明：用于获取当前数据库编号，也是本数据库生成事务的type
     * </pre>
     */
    @Resource
    private LTransactionConfig config;

    /**
     * <pre>
     * 说明：局部事务和client的对应关系
     * </pre>
     */
    private Map<DBTransaction, String> client = new ConcurrentHashMap<>();

    /**
     * <pre>
     * 说明：等待GDBMS确认的事务
     * </pre>
     */
    private Map<DBTransaction, DoCommit> waitConfirmation = new ConcurrentHashMap<>();

    /**
     * <pre>
     * 说明：事务和执行器的映射关系
     * </pre>
     */
    private Map<DBTransaction, TransactionExec> executor = new ConcurrentHashMap<>();

    /**
     * <pre>
     * 说明：发送协议的发送器
     * </pre>
     */
    @Resource
    private DDBMSSender<String> sender;

    /**
     * <pre>
     * 说明：获取全局数据库管理系统的站点名
     * </pre>
     */
    @Resource
    private GDBMSConfig gdbmsConfig;

    /**
     * <pre>
     * 说明：获取本站点名
     * </pre>
     */
    @Resource
    private ServerConfig serverConfig;

    /**
     * <pre>
     * 说明：数据库数据源
     * </pre>
     */
    @Resource
    private DataSource dataSource;

    @PostConstruct
    public void init() throws Exception {
        initTransactionCount();
    }

    /**
     * 说明：初始化当前数据库提交事务数
     * @throws SQLException
     * @since 0.0.0
     */
    private void initTransactionCount() throws SQLException {
        String sql = "select ifnull(max(`id`), 0) from `" + Constant.Sync.SYNC_INFO_TABLE_PREFIX + serverConfig.dbName() + "`;";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                transactionCount = new AtomicLong(resultSet.getLong(1));
            } else throw new RuntimeException("No transaction count found");
        }
    }

    /**
     * <pre>
     * 说明：获取当前数据库提交的事务数
     * 实现步骤：
     *   1) 返回当前提交事务数
     * </pre>
     *
     * @return 当前数据库提交的事务数
     * @since 0.0.0
     */
    public long transactionCount() {
        return transactionCount.get();
    }

    /**
     * <pre>
     * 说明: 该方法用于创建一个事务
     * 实现步骤：
     *   1) 获取事务id，并构建事务实例
     *   2) 保存事务和client站点的映射关系
     *   3) 保存事务和执行器之间的关系
     *   4) 在新线程启动事务
     *   5) 返回创建的事务
     * </pre>
     *
     * @param clientId 客户端ID
     * @param exec 执行器实例
     * @return 创建的事务实例
     * @since 0.0.0
     */
    public DBTransaction createTransaction(String clientId, TransactionExec exec) {
        DBTransaction transaction = new DBTransaction(transactionId.getAndIncrement(),
                config.transactionType(), DEFAULT_ORDER);
        client.put(transaction, clientId);
        executor.put(transaction, exec);
        ThreadUtils.execute(() -> exec.exec(transaction));
        return transaction;
    }

    /**
     * <pre>
     * 说明: 该方法用于注册一个事务提交接口实例
     * 实现步骤：
     *   1) 将事务和DoCommit的关系保存到waitConfirmation中。
     * </pre>
     * @param transaction 等待提交的事务
     * @param doCommit 事务提交逻辑
     * @since 0.0.0
     */
    @Override
    public void registerCommit(DBTransaction transaction, DoCommit doCommit) {
        waitConfirmation.put(transaction, doCommit);
    }

    /**
     * <pre>
     * 说明：根据事务撤销原因执行撤销事务操作。
     * 规范：
     *   1) reason=1：表示LOCK_FAILURE
     *   2) reason=2：表示FAILURE
     *   3) reason=3：表示来自全局数据库告知撤销事务
     * 实现步骤：
     *   1) 取出事务对应的DoCommit实例
     *     1. 实例不为null则，执行该实例，参数为null
     *   2) 判定reason是LOCK_FAILURE
     *     1. 判定是本地事务，在500ms后重新启动事务
     *     2. 判定是全局事务，告知GDBMS事务封锁失败
     *   3) 判定reason是FAILURE
     *     1. 判定是局部事务：告知client当前事务执行失败
     *     2. 判定是全局事务：告知GDBMS当前事务执行失败
     *   4) 判定reason是CANCEL_DBTRANSACTION
     * </pre>
     *
     * @param transaction 需要撤销的事务
     * @param reason 撤销原因
     * @since 0.0.0
     */
    public void cancel(DBTransaction transaction, int reason) {
        DoCommit commit = waitConfirmation.remove(transaction);
        ACKProtocol p = null;
        if (commit != null) commit.commit(null);
        boolean localTransaction = isLocalTransaction(transaction);
        if (reason != Constant.TransactionCenter.CANCEL_FAILURE && !localTransaction) {
            p = new ACKProtocol(Constant.ACKType.CANCEL_DBTRANSACTION,
                    serverConfig.dbName(), transaction, reason);
        }
        if (localTransaction) {
            if (reason == Constant.TransactionCenter.LOCK_FAILURE) {
                ThreadUtils.schedule(() -> executor.get(transaction).exec(transaction),
                        Constant.TransactionCenter.LOCK_FAILURE_REBOOT_INTERVAL, TimeUnit.MILLISECONDS);
            } else {
                fail(transaction);
            }
        }
        if (p == null) return;
        sender.send(gdbmsConfig.siteName(), p, null, false);
    }

    /**
     * 说明：判定一个事务是不是本地事务
     * @param transaction 一个数据库事务
     * @return true 表示本地事务， false表示不是。
     * @since 0.0.0
     */
    private boolean isLocalTransaction(DBTransaction transaction) {
        return transaction.getType() == config.transactionType();
    }

    /**
     * <pre>
     * 说明：该方法由GDBMS进行调用，真正地提交事务
     * 实现步骤：
     *   1) 获取事务对应的doCommit接口实例
     *   2) 提交事务数加1
     *   3) 执行该实例
     * </pre>
     *
     * @param transaction 需要提交的事务
     * @param commitId 提交ID
     * @since 0.0.0
     */
    public void commit(DBTransaction transaction, Long commitId) {
        if (commitId == null) throw new NullPointerException("A CommitId is null");
        DoCommit commit = waitConfirmation.remove(transaction);
        if (commit == null) throw new NullPointerException("A DoCommit is null");
        transactionCount.incrementAndGet();
        commit.commit(commitId);
        success(transaction);

    }

    /**
     * <pre>
     * 说明：client事务执行失败
     * 实现步骤：
     *   1) 判定事务不是局部事务直接返回
     *   2) 从client中获取站点信息
     *   3) 构建事务执行失败的ACK协议
     *   4) 将协议发送给client
     *   5) 删除事务缓存信息
     * </pre>
     *
     * @param transaction 执行失败的事务
     * @since 0.0.0
     */
    private void fail(DBTransaction transaction) {
        if (transaction.getType() != config.transactionType()) {
            return;
        }
        if (waitConfirmation.containsKey(transaction)) throw new RuntimeException("Unexpected execution!");
        sender.send(client.remove(transaction),
                new ACKProtocol(Constant.ACKType.DBTRANSACTION_FAILURE, transaction),
                null, false);
        executor.remove(transaction);
    }

    /**
     * <pre>
     * 说明：通知client事务执行成功
     * 实现步骤：
     *   1) 判定事务不是局部事务直接返回
     *   2) 从client中获取站点信息
     *   3) 构建事务执行成功的协议
     *   4) 将协议发送给client
     *   5) 删除事务缓存信息
     * </pre>
     *
     * @param transaction 执行失败的事务
     * @since 0.0.0
     */
    private void success(DBTransaction transaction) {
        if (isLocalTransaction(transaction)) {
            return;
        }
        if (waitConfirmation.containsKey(transaction)) throw new RuntimeException("Unexpected execution!");
        sender.send(client.remove(transaction),
                new ACKProtocol(Constant.ACKType.DBTRANSACTION_SUCCESS, transaction),
                null, false);
        executor.remove(transaction);
    }
}
