package org.qh.DDBMS.LDBMS.ms_sync.impl;

import com.qh.protocol.exception.ProtocolException;
import com.qh.protocol.net.BaseTransportProtocol;
import com.qh.protocol.net.TransportProtocol;
import org.qh.DDBMS.LDBMS.ms_sync.AbstractSync;
import org.qh.DDBMS.LDBMS.ms_sync.dao.SyncDao;
import org.qh.DDBMS.common.Constant;
import org.qh.DDBMS.common.db.DBTransaction;
import org.qh.DDBMS.common.entity.SyncInfoEntity;
import org.qh.DDBMS.common.input.ServerConfig;
import org.qh.DDBMS.common.output.DDBMSSender;
import org.qh.DDBMS.common.protocol.RequestSyncInfoProtocol;
import org.qh.DDBMS.common.protocol.SyncInfoProtocol;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @Author: qihe
 * @Date: 2024/10/29
 * @Version: 0.0.0
 * @Description: 用于执行从站点请求同步数据的类
 */
public class SyncTransaction extends AbstractSync<Object[]> {

    /**
     * <pre>
     * 说明：获取同步信息的dao
     * </pre>
     */
    @Resource
    private SyncDao syncDao;

    /**
     * <pre>
     * 说明：用于获取当前站点负责的数据库名
     * </pre>
     */
    @Resource
    private ServerConfig serverConfig;

    /**
     * <pre>
     * 说明：用于将协议信息发送给到请求者
     * </pre>
     */
    @Resource
    private DDBMSSender<String> sender;

    public SyncTransaction() {
        super(Constant.ObjectProtocolDataUse.REQUEST_SYNC_INFO_PROTOCOL);
    }

    /**
     * <pre>
     * 说明：用于验证传入参数是否合规。
     * 规范：
     *   1) 协议内容
     *     1. dataUse：1
     *     2. data[]:
     *       - dbName
     *       - last
     *       - siteName
     * 实现步骤：
     *   1) 判定protocol.dataUse!=本类dataUse,则抛出异常
     *   2) 将传入协议转化为RequestSyncInfoProtocol
     *   3) 判断传入数据库不是当前站点负责的数据库，则返回null
     *   4) 将协议data值返回
     * </pre>
     *
     * @param protocol 接受到的请求同步信息协议实例
     * @return List<DBTransaction> 两个DBTransaction实例
     * @since 0.0.0
     */
    @Override
    public Object[] validate(BaseTransportProtocol protocol) {
        if (getDataUse() != protocol.data()[9]) throw new ProtocolException("Bad protocol content!");
        RequestSyncInfoProtocol p = new RequestSyncInfoProtocol(protocol.data());
        if (!serverConfig.dbName().equals(p.getData()[0])) return null;
        return p.getData();
    }

    /**
     * <pre>
     * 说明：真正执行数据同步操作。
     * 规范：
     *   1) 协议内容
     *     1. dataUse：1
     *     2. data[]:
     *       - dbName
     *       - last
     *       - siteName
     * 实现步骤：
     *   1) 从数据库中查出id值大于args[1]的协议
     *   2) 将查询到的同步信息封装为SyncInfoProtocol
     *   3) 将协议信息发送给请求者
     * </pre>
     *
     * @param args 请求同步信息需要的参数
     * @return TransportProtocol 封装的同步结果
     * @since 0.0.0
     */
    @Override
    public TransportProtocol doSync(Object[] args) throws Exception {
        List<SyncInfoEntity> res = syncDao.selectSyncInfo((String) args[0], (Long) args[1]);
        sender.send((String) args[2], new SyncInfoProtocol(res), null, false);
        return null;
    }
}
