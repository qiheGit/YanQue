package org.qh.DDBMS.GDBMS.idm;


import com.qh.protocol.exception.ProtocolResolveException;
import org.qh.DDBMS.common.Constant;
import org.qh.DDBMS.common.ack.AckHandler;
import org.qh.DDBMS.common.input.DDBMSReceiver;
import org.qh.DDBMS.common.output.CallbackReceiverManager;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/17
 * @Version: 0.0.0
 * @Description: 用于分发LDBMS对CallMethodProtocol响应的处理
 */
@AckHandler
public class CallbackDispatcher {

    /**
     * <pre>
     * 说明：用于获取回调key对应的receiver
     * </pre>
     */
    @Resource
    private CallbackReceiverManager manager;

    /**
     * <pre>
     * 说明：分发回调处理的方法
     * 规范：
     *   @AckHandler(METHOD_CALLBACK)方法
     *   第一个参数必须是回调key
     * 实现步骤：
     *   1) 获取回调key对应receiver
     *   2) 将回调key抹去，将剩下的参数作为receiver的receive()的第二个参数传入
     * </pre>
     *
     * @param params 参数列表，其中第一个参数为回调key
     * @since 0.0.0
     */
    @AckHandler(Constant.ACKType.METHOD_CALLBACK)
    public void dispatch(Object... params) throws Exception {
        if (!(params[0] instanceof Long)) throw new ProtocolResolveException(Arrays.toString(params));
        DDBMSReceiver ddbmsReceiver = manager.get(Long.valueOf((long) params[0]));
        ddbmsReceiver.receive(null, (Object) Arrays.copyOfRange(params, 1, params.length));

    }
}

