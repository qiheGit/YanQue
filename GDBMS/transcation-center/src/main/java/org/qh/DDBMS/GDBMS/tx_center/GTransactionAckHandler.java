package org.qh.DDBMS.GDBMS.tx_center;

import org.qh.DDBMS.common.Constant;
import org.qh.DDBMS.common.ack.AckHandler;
import org.qh.DDBMS.common.db.DBTransaction;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/17
 * @Version: 0.0.0
 * @Description: 处理收到的有关Transaction的AckProtocol
 */
public class GTransactionAckHandler {

    /**
     * <pre>
     * 说明：全局事务中心
     * </pre>
     */
    private GTransactionCenter center;

    /**
     * <pre>
     * 说明：用于处理撤销事务的Ack协议
     * 规范：
     *   1) 注解：@AckHandler(REVOCATE_DBTRANSACTION)
     * 实现步骤：
     *   1) 执行center的revocate方法，返回执行结果
     * </pre>
     *
     * @param dbName 撤销事务的数据库名
     * @param transaction 数据库事务
     * @param reason 失败原因
     * @since 0.0.0
     */
    @AckHandler(Constant.ACKType.CANCEL_DBTRANSACTION)
    public void cancel(String dbName, DBTransaction transaction, int reason) {
        center.cancel(dbName, transaction, reason);
    }

    /**
     * <pre>
     * 说明：处理请求提交事务的ACK协议
     * 规范：
     *   1) 注解：@AckHandler(COMMIT_DBTRANSACTION)
     * 实现步骤：
     *   1) 调用center的commit方法
     * </pre>
     *
     * @param dbName 数据库名
     * @param transaction 数据库事务
     * @param sqlStatement 执行的sql语句
     * @since 0.0.0
     */
    @AckHandler(Constant.ACKType.COMMIT_TYPE)
    public void commit(String dbName, DBTransaction transaction, String sqlStatement) {
        center.commit(dbName, transaction, sqlStatement);
    }
}













