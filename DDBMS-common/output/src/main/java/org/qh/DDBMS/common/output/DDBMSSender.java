package org.qh.DDBMS.common.output;

import com.qh.protocol.net.TransportProtocol;
import org.qh.DDBMS.common.input.DDBMSReceiver;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/17
 * @Version: 0.0.0
 * @Description: 将协议对象发送到目标站点的发送者接口
 * @Genericity:
 *   1. Q: 能够得到socket的参数
 */
public interface DDBMSSender<Q> {

    /**
     * <pre>
     * 说明：发送协议实例的目标方法
     * </pre>
     * @param params Q 能够得到socket的参数
     * @param protocol TransportProtocol 发送的协议实例
     * @param receiver DDBMSReceiver&lt;Q, ?&gt; 接收者
     * @param sync 发送时是否阻塞
     * @since 0.0.0
     */
    void send(Q params, TransportProtocol protocol, DDBMSReceiver receiver, boolean sync) ;
}

