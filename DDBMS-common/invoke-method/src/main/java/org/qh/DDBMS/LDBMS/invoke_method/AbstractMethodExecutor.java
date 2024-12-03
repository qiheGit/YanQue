package org.qh.DDBMS.LDBMS.invoke_method;


import com.qh.protocol.net.BaseTransportProtocol;
import com.qh.protocol.net.TransportProtocol;
import io.netty.channel.ChannelHandlerContext;
import org.qh.DDBMS.common.Validator;
import org.qh.DDBMS.common.input.DDBMSProtocolDispatcher;
import org.qh.DDBMS.common.input.DDBMSReceiver;
import org.qh.DDBMS.common.protocol.CallMethodProtocol;

import java.lang.reflect.InvocationTargetException;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/24
 * @Version: 0.0.0
 * @Description: 此类是一个抽象类，类中定义了一个MethodExecutor必须要执行的动作，以及业务流程。
 */
public abstract class AbstractMethodExecutor<F> implements DDBMSReceiver<BaseTransportProtocol, F>, Validator<BaseTransportProtocol, CallMethodProtocol> {

    /**
     * <pre>
     * 说明：该方法用于校验，传入参数是否合规。
     * 该方法是一个抽象方法由子类实现。
     * </pre>
     * @param protocol 要校验的BaseTransportProtocol实例
     * @return 校验后的CallMethodProtocol实例
     * @since 0.0.0
     */
    @Override
    public abstract CallMethodProtocol validate(BaseTransportProtocol protocol);

    /**
     * <pre>
     * 说明：模板方法
     * 实现步骤：
     *   1) 执行validate()
     *      1. 判定validate()返回null，则抛出参数异常
     *   2) 执行invoke0()，并返回执行结果
     * </pre>
     * @param protocol BaseTransportProtocol 要分发的协议
     * @param f  处理协议的辅助参数
     * @return TransportProtocol&lt;ByteBuffer&gt; 分发后的协议
     * @since 0.0.0
     */
    @Override
    public TransportProtocol receive(BaseTransportProtocol protocol, F f) throws Exception {
        CallMethodProtocol callMethodProtocol = validate(protocol);
        if (callMethodProtocol == null) {
            throw new IllegalArgumentException("Invalid parameters provided");
        }
        return invoke0(callMethodProtocol, f);
    }
    /**
     * <pre>
     * 说明：真正执行目标方法的方法。
     * </pre>
     * @param callMethodProtocol 要执行的CallMethodProtocol实例
     * @param f 处理协议的辅助参数
     * @return 执行结果的TransportProtocol实例
     * @since 0.0.0
     */
    protected abstract TransportProtocol invoke0(CallMethodProtocol callMethodProtocol, F f) throws Exception;
}

