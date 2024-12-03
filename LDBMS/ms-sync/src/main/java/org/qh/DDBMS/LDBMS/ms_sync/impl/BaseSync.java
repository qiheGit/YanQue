package org.qh.DDBMS.LDBMS.ms_sync.impl;

import com.qh.protocol.exception.ProtocolResolveException;
import com.qh.protocol.net.BaseTransportProtocol;
import com.qh.protocol.net.TransportProtocol;
import org.qh.DDBMS.LDBMS.ms_sync.AbstractSync;
import org.qh.DDBMS.LDBMS.ms_sync.Sync;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/28
 * @Version: 0.0.0
 * @Description: 基础的同步实现类，用于分发同步任务到具体的实现类中
 */
public class BaseSync implements Sync, ApplicationContextAware {

    /**
     * <pre>
     * 说明：持有dataUse和sync实例之间的映射关系
     * </pre>
     */
    private Map<Byte, AbstractSync<?>> syncObj = new  ConcurrentHashMap<>();;

    /**
     * <pre>
     * 说明：初始化syncObj
     * 实现步骤：
     *   1) 从ioc中获取所有AbstractSync的子类实例
     *   2) 循环地遍历beans
     *      1. 将beans[i]的dataUse作为key，和beans[i]作为value装入syncObj
     * </pre>
     *
     * @param applicationContext Spring应用上下文
     * @since 0.0.0
     */
    public void init(ApplicationContext applicationContext) {
        Map<String, AbstractSync> beans = applicationContext.getBeansOfType(AbstractSync.class);
        for (AbstractSync<?> value : beans.values()) {
            syncObj.put(value.getDataUse(), value);
        }
    }

    /**
     * <pre>
     * 说明：重写父类实现，分发同步任务。
     * 实现步骤：
     *   1) 获取协议的dataUse
     *   2) 根据dataUse从syncObj中获取sync实例
     *   3) 判定sync不存在则抛出异常
     *   4) 执行sync实例的sync(), 并返回执行结果
     * </pre>
     *
     * @param protocol 基础传输协议
     * @return TransportProtocol
     * @since 0.0.0
     */
    @Override
    public TransportProtocol sync(BaseTransportProtocol protocol) throws Exception {
        byte dataUse = protocol.data()[9];
        AbstractSync<?> syncInstance = syncObj.get(dataUse);
        if (syncInstance == null) {
            throw new ProtocolResolveException("Not found any syncInstance for the data use: " + dataUse);
        }
        return syncInstance.sync(protocol);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        init(applicationContext);
    }
}

