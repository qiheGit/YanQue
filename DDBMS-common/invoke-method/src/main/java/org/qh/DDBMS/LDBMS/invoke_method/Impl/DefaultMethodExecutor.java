package org.qh.DDBMS.LDBMS.invoke_method.Impl;

import com.qh.protocol.annotation.ProtocolReceiver;
import com.qh.protocol.net.BaseTransportProtocol;
import com.qh.protocol.net.TransportProtocol;
import io.netty.channel.ChannelHandlerContext;
import org.qh.DDBMS.LDBMS.invoke_method.AbstractMethodExecutor;
import org.qh.DDBMS.common.Constant;
import org.qh.DDBMS.common.module.DBMethod;
import org.qh.DDBMS.common.module.DBModuleManager;
import org.qh.DDBMS.common.protocol.ACKProtocol;
import org.qh.DDBMS.common.protocol.CallMethodProtocol;
import org.qh.sys.exception.MethodNotFoundException;

import javax.annotation.Resource;
import java.lang.reflect.Parameter;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/24
 * @Version: 0.0.0
 * @Description: AbstractMethodExecutor抽象类的默认实现类
 */
public class DefaultMethodExecutor extends AbstractMethodExecutor<ChannelHandlerContext> {

    /**
     * 说明：该属性用于获取要执行的目标方法
     */
    @Resource
    private DBModuleManager moduleManager;

    /**
     * <pre>
     * 说明：该方法用于校验，传入参数是否合规。
     * 实现步骤：
     *   1) 根据入参构建一个CallMethodProtocol实例
     *   2) 判定uri对应的目标方法不存在，返回null
     *   3) 判定参数和目标方法参数不匹配，返回null
     *   4) 返回CallMethodProtocol实例
     * </pre>
     * @param protocol 要校验的BaseTransportProtocol实例
     * @return 校验后的CallMethodProtocol实例
     * @since 0.0.0
     */
    @Override
    public CallMethodProtocol validate(BaseTransportProtocol protocol) {
        try {
            CallMethodProtocol callMethodProtocol = new CallMethodProtocol(protocol.data());
            DBMethod method = moduleManager.getMethod(callMethodProtocol.uri());
            if (method == null) return null;

            Parameter[] parameters = method.method().getParameters();
            if (parameterCheck(callMethodProtocol.args(), parameters)) {
                return null;
            }
            return callMethodProtocol;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * <pre>
     * 说明：检验入参是否和目标方法相匹配
     * 注意事项：
     *   1. 如果入参是null，则默认匹配
     * 实现步骤：
     *   1. 遍历匹配args和parameter的类型
     * </pre>
     * @param args 执行方法需要的参数值
     * @param parameters 方法声明所需要的参数
     * @return true 当前参数初步符合要求， false 非法参数
     * @since 0.0.0
     */
    private boolean parameterCheck(Object[] args, Parameter[] parameters) {
        if (args.length != parameters.length) return false;
        for (int i = 0; i < parameters.length; i++) {
            if (args[i] == null) continue;
            if (!args[i].getClass().equals(parameters[i].getType())) return false;
        }
        return true;
    }

    /**
     * <pre>
     * 说明：正真执行目标方法的方法。
     * 实现步骤：
     *   1) 获取目标方法
     *   2) 将当前线程的上下文类加载器换为目标模块程序的上下文类加载器  // 待定
     *   3) 执行目标方法
     *   4) 将当前线程的上下文类加载器还原为原来的类加载器            // 待定
     *   5) 将目标方法的返回值封装为ACK协议进行返回。
     *   6) 异常处理：直接抛出
     * </pre>
     * @param callMethodProtocol 要执行的CallMethodProtocol实例
     * @param ctx 通道的上下文对象
     * @return 执行结果的TransportProtocol实例
     * @since 0.0.0
     */
    @Override
    protected ACKProtocol invoke0(CallMethodProtocol callMethodProtocol, ChannelHandlerContext ctx) throws Exception {
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        // try {
            DBMethod dbMethod = moduleManager.getMethod(callMethodProtocol.uri());
            if (dbMethod == null) throw new MethodNotFoundException(callMethodProtocol.uri());

            Thread.currentThread().setContextClassLoader(dbMethod.obj().getClass().getClassLoader());

            Object res = dbMethod.method().invoke(dbMethod.obj(), callMethodProtocol.args());
            return new ACKProtocol(Constant.ACKType.METHOD_CALLBACK, callMethodProtocol.key(), res);
        // } finally {
        //     Thread.currentThread().setContextClassLoader(originalClassLoader);
        // }
    }
}

