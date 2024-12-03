package org.qh.DDBMS.common.tx;

import org.qh.DDBMS.common.db.DBTransaction;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/17
 * @Version: 0.0.0
 * @Description: 事务执行器接口
 */
public interface TransactionExec {

    /**
     * <pre>
     * 说明：接收一个事务对象，并执行事务有关业务
     * </pre>
     * @param transaction DBTransaction 需要执行的事务对象
     * @since 0.0.0
     */
    void exec(DBTransaction transaction);
}






