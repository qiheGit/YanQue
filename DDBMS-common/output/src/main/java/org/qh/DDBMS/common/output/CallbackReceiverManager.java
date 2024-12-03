package org.qh.DDBMS.common.output;

import org.qh.DDBMS.common.input.DDBMSReceiver;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/17
 * @Version: 0.0.0
 * @Description: 回调协议的回调Receiver管理器
 */
public interface CallbackReceiverManager {

    /**
     * <pre>
     * 说明：为回调协议注册一个长期有效的Receiver
     * </pre>
     * @param key long 回调协议的唯一标识
     * @param receiver Receiver 注册的Receiver实例
     * @since 0.0.0
     */
    void register(long key, DDBMSReceiver receiver);

    /**
     * <pre>
     * 说明：为一个回调协议实例注册一个临时的Receiver
     * </pre>
     * @param key long 回调协议的唯一标识
     * @param receiver Receiver 注册的Receiver实例
     * @since 0.0.0
     */
    void temporary(long key, DDBMSReceiver receiver);

    /**
     * <pre>
     * 说明：获取回调协议的回调key对应的Receiver
     * </pre>
     * @param key long 回调协议的唯一标识
     * @return Receiver 对应的Receiver实例
     * @since 0.0.0
     */
    DDBMSReceiver get(long key);

    /**
     * <pre>
     * 说明：注销一个长期有效回调key的Receiver
     * </pre>
     * @param key long 回调协议的唯一标识
     * @return Receiver 被注销的Receiver实例
     * @since 0.0.0
     */
    DDBMSReceiver unregister(long key);
}

