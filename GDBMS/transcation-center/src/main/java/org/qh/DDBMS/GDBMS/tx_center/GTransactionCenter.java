package org.qh.DDBMS.GDBMS.tx_center;
import org.qh.DDBMS.common.tx.TransactionExec;
import org.qh.DDBMS.common.db.DBTransaction;


/**
 *
 * @Author: qihe
 * @Date: 2024/11/17
 * @Version: 0.0.0
 * @Description: 全局事务中心
 */
public interface GTransactionCenter {

    /**
     * <pre>
     * 说明：创建一个事务
     * </pre>
     * @param name String 事务名称
     * @param exec TransactionExec 事务执行器
     * @return DBTransaction 创建的事务对象
     * @since 0.0.0
     */
    DBTransaction createTransaction(String name, TransactionExec exec);

    /**
     * <pre>
     * 说明：注册执行一个事务所需要修改哪些数据库
     * </pre>
     * @param transaction DBTransaction 需要修改的事务
     * @param dbName String 需要修改的数据库名称
     * @since 0.0.0
     */
    void registerUpdatedDB(DBTransaction transaction, String dbName);

    /**
     * <pre>
     * 说明：根据事务撤销原因执行撤销事务操作
     * </pre>
     *
     * @param dbName 提出撤销事务的数据库
     * @param transaction DBTransaction 需要撤销的事务
     * @param reason int 撤销原因
     * @since 0.0.0
     */
    void cancel(String dbName, DBTransaction transaction, int reason);

    /**
     * <pre>
     * 说明：LDBMS同意提交事务
     * </pre>
     * @param dbName String 数据库名称
     * @param transaction DBTransaction 第一个事务
     * @param sqlStatement String 数据库上执行的sql
     * @since 0.0.0
     */
    void commit(String dbName, DBTransaction transaction, String sqlStatement);

}

