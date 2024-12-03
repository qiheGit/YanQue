package org.qh.DDBMS.common.exception;

import org.qh.DDBMS.common.db.DBTransaction;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/26
 * @Version: 0.0.0
 * @Description: 一个事务执行失败应该抛出的异常
 */
public class FailedTransactionException extends RuntimeException {
    private DBTransaction transaction; // 执行失败的事务

    public FailedTransactionException(DBTransaction transaction) {
        this.transaction = transaction;
    }

    public DBTransaction getTransaction() {
        return transaction;
    }
}
