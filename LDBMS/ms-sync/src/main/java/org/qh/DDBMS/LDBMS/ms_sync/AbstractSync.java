package org.qh.DDBMS.LDBMS.ms_sync;

import com.qh.protocol.net.BaseTransportProtocol;
import com.qh.protocol.net.TransportProtocol;
import org.qh.DDBMS.common.Validator;
import org.qh.DDBMS.common.protocol.ObjectTransportProtocol;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/28
 * @Version: 0.0.0
 * @Description: 该类是Sync接口的抽象实现类
 */

public abstract class AbstractSync<Q> implements Sync, Validator<BaseTransportProtocol, Q> {

    /**
     * <pre>
     * 说明：确定当前实例对什么样的用途的协议数据进行解析
     * </pre>
     */
    protected byte dataUse;

    public AbstractSync(byte dataUse) {
        this.dataUse = dataUse;
    }

    /**
     * <pre>
     * 说明：
     *   1) 抽象方法
     *   2) 用于验证传入参数是否合规
     * </pre>
     *
     * @param protocol 当前协议实例
     * @return 验证结果
     * @since 0.0.0
     */
    public abstract Q validate(BaseTransportProtocol protocol);

    /**
     * <pre>
     * 说明：
     *   1) 抽象方法
     *   2) 真正执行数据同步操作。
     * </pre>
     *
     * @param data 数据
     * @return TransportProtocol
     * @since 0.0.0
     */
    protected abstract TransportProtocol doSync(Q data) throws Exception;

    /**
     * <pre>
     * 说明：该方法是一个模板方法，规定的子类执行同步操作所有的步骤
     * 实现步骤：
     *   1) 执行validate()方法，
     *      1. 返回null，则直接返回。
     *   2) 将validate()方法的返回值,作为参数执行doSync()
     *   3) 返回doSync()执行结果
     * </pre>
     *
     * @param protocol 基础传输协议
     * @return TransportProtocol
     * @since 0.0.0
     */
    public TransportProtocol sync(BaseTransportProtocol protocol) throws Exception {
        Q validate = validate(protocol);
        if (validate == null) return null;
        return doSync(validate);
    }

    public byte getDataUse() {
        return this.dataUse;
    }

}

