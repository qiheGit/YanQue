package org.qh.DDBMS.LDBMS.ms_sync;

import org.qh.DDBMS.LDBMS.tx.LTransactionCenter;
import org.qh.DDBMS.common.Constant;
import org.qh.DDBMS.common.ack.AckHandler;
import org.qh.DDBMS.common.input.ServerConfig;
import org.qh.DDBMS.common.output.DDBMSSender;
import org.qh.DDBMS.common.protocol.RequestSyncInfoProtocol;

import javax.annotation.Resource;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/27
 * @Version: 0.0.0
 * @Description: 处理该模块接收到的Ack协议
 */
@AckHandler
public class MSSyncAckHandler {

    /**
     * <pre>
     * 说明：事务中心，用于获取提交事务数。
     * </pre>
     */
    @Resource
    private LTransactionCenter txCenter;

    /**
     * <pre>
     * 说明：用于发送将协议信息
     * </pre>
     */
    @Resource
    private DDBMSSender<String> sender;

    /**
     * <pre>
     * 说明：用于获取当前站点信息
     * </pre>
     */

    @Resource
    private ServerConfig serverConfig;

    /**
     * <pre>
     * 说明：处理收到缺失事务协议
     * 注解：AckHandler(LACK_COMMITED_TRANSACTION)
     * 实现步骤：
     *   1) 向发送站点发出请求同步信息的协议，携带如下参数
     *     1. dbName
     *     2. last：最后提交事务的同步信息id
     *     3. siteName
     * </pre>
     * @param site 告知当前站点缺失事务的站点名
     * @since 0.0.0
     */
    @AckHandler(Constant.ACKType.LACK_COMMITED_TRANSACTION)
    public void handleLackTransaction(String site) {
        RequestSyncInfoProtocol protocol = new RequestSyncInfoProtocol(serverConfig.dbName(),
                txCenter.transactionCount(), serverConfig.siteName());

        sender.send(site, protocol, null, false);
    }
}


















