package org.qh.DDBMS.common.ack;

import com.qh.obj.MethodHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/17
 * @Version: 0.0.0
 * @Description: 处理ack协议的方法处理器
 */
public class AckMethod implements MethodHandler {
    /**
     * <pre>
     * 说明：方法处理哪一类型的ack协议
     * </pre>
     */
    private int type;

    /**
     * <pre>
     * 说明：方法的执行者
     * </pre>
     */
    private Object obj;

    /**
     * <pre>
     * 说明：处理ack协议的方法
     * </pre>
     */
    private Method method;

    /**
     * <pre>
     * 说明：构造函数，用于初始化AckMethod实例
     * 实现步骤：
     *   1) 设置ack协议类型
     *   2) 设置方法执行者
     *   3) 设置处理ack协议的方法
     * </pre>
     *
     * @param type ack协议类型
     * @param obj 方法的执行者
     * @param method 处理ack协议的方法
     * @since 0.0.0
     */
    public AckMethod(int type, Object obj, Method method) {
        this.type = type;
        this.obj = obj;
        this.method = method;
    }

    @Override
    public Object invoke(Object... args) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(obj, args);
    }

    @Override
    public Object handler() {
        return obj;
    }

    @Override
    public Method method() {
        return method;
    }

}
