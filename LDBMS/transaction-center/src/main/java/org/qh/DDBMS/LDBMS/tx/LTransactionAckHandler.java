package org.qh.DDBMS.LDBMS.tx;


import org.qh.DDBMS.common.Constant;
import org.qh.DDBMS.common.ack.AckHandler;
import org.qh.DDBMS.common.db.DBTransaction;

import javax.annotation.Resource;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/25
 * @Version: 0.0.0
 * @Description: 处理局部事务有关的ACK协议
 */
@AckHandler
public class LTransactionAckHandler {

    /**
     * <pre>
     * 说明：局部事务中心
     * </pre>
     */
    @Resource
    private LTransactionCenter center;

    /**
     * <pre>
     * 说明：处理撤销事务的ack
     * 注解：@AckHandler(CANCEL_DBTRANSACTION)
     * 实现步骤：
     *   1) 调用center的cancel()
     * </pre>
     *
     * @param transaction 需要撤销的事务
     * @param commitId 提交ID
     * @since 0.0.0
     */
    @AckHandler(Constant.ACKType.CANCEL_DBTRANSACTION)
    public void cancel(DBTransaction transaction, Long commitId) {
        center.cancel(transaction, Constant.TransactionCenter.CANCEL_FAILURE);
    }

    /**
     * <pre>
     * 说明：处理提交事务的ack
     * 注解：@AckHandler(RESPONSE_COMMIT_TYPE)
     * 实现步骤：
     *   1) 调用center的commit()
     * </pre>
     *
     * @param transaction 需要提交的事务
     * @param commitId 提交ID
     * @since 0.0.0
     */
    @AckHandler(Constant.ACKType.RESPONSE_COMMIT_TYPE)
    public void commit(DBTransaction transaction, Long commitId) {
        center.commit(transaction, commitId);
    }
}
