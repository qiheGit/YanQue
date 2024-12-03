package org.qh.DDBMS.common.input.impl;

import com.qh.net.Receiver;
import com.qh.obj.MethodHandler;
import com.qh.protocol.net.BaseTransportProtocol;
import com.qh.protocol.net.TransportProtocol;
import io.netty.channel.ChannelHandlerContext;
import org.qh.DDBMS.common.ack.AckHandler;
import org.qh.DDBMS.common.ack.AckMethod;
import org.qh.DDBMS.common.input.DDBMSProtocolDispatcher;
import org.qh.DDBMS.common.input.DDBMSReceiver;
import org.qh.DDBMS.common.protocol.ACKProtocol;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/17
 * @Version: 0.0.0
 * @Description: Ack协议的分发器
 */
public class AckDispatcher implements DDBMSProtocolDispatcher, ApplicationContextAware {

    /**
     * <pre>
     * 说明：各个类型的ack和其处理方法
     * </pre>
     */
    private Map<Integer, MethodHandler> ackMethod;

    /**
     * <pre>
     * 说明：用于初始化ackHandler
     * 实现步骤：
     *   1) 构建ackMethod实例
     *   2) 获取所有被AckHandler注解注释类的实例
     *   3) 遍历实例的所有方法
     *      1. 将被AckHandler注解的方法封装为AckMethod实例method
     *      2. 将method实例装入ackMethod中
     * </pre>
     *
     * @param ioc 应用上下文
     * @since 0.0.0
     */
    public void init(ApplicationContext ioc) {
        ackMethod = new ConcurrentHashMap<>();
        Map<String, Object> beans = ioc.getBeansWithAnnotation(AckHandler.class);
        for (Object value : beans.values()) {
            for (Method method : value.getClass().getDeclaredMethods()) {
                if (!method.isAnnotationPresent(AckHandler.class)) continue;
                AckHandler annotation = method.getAnnotation(AckHandler.class);
                ackMethod.put(annotation.value(), new AckMethod(annotation.value(), value, method));
            }
        }
    }

    /**
     * <pre>
     * 说明：分发任务给到真正的处理器
     * 规范：
     *   1. ack协议的处理方法不能返回协议对象，如果需要发送信息则需要使用sender进行发送。
     * 实现步骤：
     *   1) BaseTransportProtocol转为AckProtocol实例
     *   2) 获取ackType对应的ackMethod
     *   3) 执行该方法
     *   4) 返回执行结果
     * </pre>
     *
     * @param protocol 基础传输协议
     * @param ctx 通道处理上下文
     * @return 处理后的TransportProtocol实例
     * @since 0.0.0
     */
    public TransportProtocol doDispatch(BaseTransportProtocol protocol, ChannelHandlerContext ctx) throws InvocationTargetException, IllegalAccessException {
        ACKProtocol ackProtocol = new ACKProtocol(protocol.data());
        ackMethod.get(Integer.valueOf(ackProtocol.getAckType()))
                .invoke(ackProtocol.getData());
        return null;
    }

    @Override
    public void register(byte type, DDBMSReceiver<BaseTransportProtocol, ChannelHandlerContext> receiver) {

    }

    @Override
    public void unregister(byte type) {

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        init(applicationContext);
    }
}

