package org.qh.DDBMS.GDBMS.idm.impl;

import com.qh.protocol.net.AbstractCallbackProtocol;
import org.qh.DDBMS.GDBMS.idm.IDDBMethod;
import org.qh.DDBMS.GDBMS.msm.MasterSlaveManager;
import org.qh.DDBMS.common.input.DDBMSReceiver;
import org.qh.DDBMS.common.output.DDBMSSender;
import org.qh.DDBMS.common.protocol.CallMethodProtocol;

import javax.annotation.Resource;


/**
 *
 * @Author: qihe
 * @Date: 2024/11/17
 * @Version: 0.0.0
 * @Description: IDDBMethod接口的默认实现类
 */
public class DefaultIDDBMethod implements IDDBMethod {

    /**
     * <pre>
     * 说明：将信息发送到目标站点的发送器
     * </pre>
     */
    @Resource
    private DDBMSSender<String> sender;

    /**
     * <pre>
     * 说明：获取数据库对应主站点的管理器
     * </pre>
     */
    @Resource
    private MasterSlaveManager manager;

    /**
     * <pre>
     * 说明：执行参数指定数据库上的方法
     * 实现步骤：
     *   1) 解析得到数据库的主站点
     *   2) 将回调key和uri和目标方法所需要的参数封装为一个CallMethodProtocol实例protocol
     *   3) 通过sender将protocol发送出去
     * </pre>
     *
     * @param dbName 数据库
     * @param callbackKey 本次调用的回调key
     * @param uri 方法uri
     * @param receiver 接收者
     * @param params 方法参数
     * @since 0.0.0
     */
    @Override
    public void invoke(String dbName, long callbackKey, String uri, DDBMSReceiver receiver, Object... params) throws Exception {
        send(manager.masterSite(dbName), new CallMethodProtocol(callbackKey, uri, params), receiver);
    }


    /**
     * <pre>
     * 说明：执行参数指定数据库上的方法
     * 实现步骤：
     *   1) 解析得到数据库的辅站点
     *   2) 将回调key和uri和目标方法所需要的参数封装为一个CallMethodProtocol实例protocol
     *   3) 通过sender将protocol发送出去
     * </pre>
     *
     * @param dbName 数据库
     * @param callbackKey 本次调用的回调key
     * @param uri 方法uri
     * @param receiver 接收者
     * @param params 方法参数
     * @since 0.0.0
     */
    @Override
    public void readOnly(String dbName, long callbackKey, String uri, DDBMSReceiver receiver, Object... params) throws Exception {
        send(manager.slaveSite(dbName), new CallMethodProtocol(callbackKey, uri, params), receiver);
    }

    private void send(String site, CallMethodProtocol protocol, DDBMSReceiver receiver) {
        sender.send(site, protocol, receiver, false);
    }
}

