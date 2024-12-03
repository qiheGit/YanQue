package org.qh.DDBMS.common.output.impl;

import org.qh.DDBMS.common.input.DDBMSReceiver;
import org.qh.DDBMS.common.output.CallbackReceiverManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/17
 * @Version: 0.0.0
 * @Description: CallbackDDBMSReceiverManager的默认实现类,参考NettyClientUtils的实现进行实现
 */
public class DefaultCallbackReceiverManager implements CallbackReceiverManager {

    /**
     * <pre>
     * 说明：保存用户注册的回调key和DDBMSReceiver之间的映射关系。
     * </pre>
     */
    private Map<Long, DDBMSReceiver> register = new ConcurrentHashMap<>();

    /**
     * <pre>
     * 说明：保存用户注册的临时回调key和DDBMSReceiver之间的映射关系。
     * </pre>
     */
    private Map<Long, DDBMSReceiver> temporary = new ConcurrentHashMap<>();

    /**
     * <pre>
     * 说明：为回调协议注册一个长期有效的DDBMSReceiver
     * 实现步骤：
     *   1) 将key和DDBMSReceiver之间的映射关系装入到register中
     * </pre>
     *
     * @param key 回调协议的唯一标识
     * @param DDBMSReceiver 要注册的DDBMSReceiver实例
     * @since 0.0.0
     */
    public void register(long key, DDBMSReceiver DDBMSReceiver) {
        register.put(key, DDBMSReceiver);
    }

    /**
     * <pre>
     * 说明：为一个回调协议实例注册一个临时的DDBMSReceiver
     * 实现步骤：
     *   1) 将key和DDBMSReceiver之间的映射关系装入到temporary中
     * </pre>
     *
     * @param key 临时回调协议的唯一标识
     * @param DDBMSReceiver 要注册的临时DDBMSReceiver实例
     * @since 0.0.0
     */
    public void temporary(long key, DDBMSReceiver DDBMSReceiver) {
        temporary.put(key, DDBMSReceiver);
    }


    /**
     * <pre>
     * 说明：获取回调协议的回调key对应的DDBMSReceiver
     * 实现步骤：
     *   1) 调用temporary的remove()得到DDBMSReceiver
     *   2) DDBMSReceiver存在，则返回DDBMSReceiver
     *   3) 返回register中的DDBMSReceiver。
     * </pre>
     *
     * @param key 回调协议的唯一标识
     * @return 对应的DDBMSReceiver实例或null
     * @since 0.0.0
     */
    public DDBMSReceiver get(long key) {
        DDBMSReceiver DDBMSReceiver = temporary.remove(key);
        if (DDBMSReceiver != null) {
            return DDBMSReceiver;
        }
        return register.get(key);
    }

    /**
     * <pre>
     * 说明：注销一个长期有效回调key的DDBMSReceiver
     * 实现步骤：
     *   1) 从register中删除回调key与DDBMSReceiver之间的映射关系。
     * </pre>
     *
     * @param key 回调协议的唯一标识
     * @return 被注销的DDBMSReceiver实例或null
     * @since 0.0.0
     */
    public DDBMSReceiver unregister(long key) {
        return register.remove(key);
    }
}

