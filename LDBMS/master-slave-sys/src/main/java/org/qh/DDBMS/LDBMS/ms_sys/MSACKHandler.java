package org.qh.DDBMS.LDBMS.ms_sys;

import com.qh.protocol.exception.ProtocolResolveException;
import org.qh.DDBMS.common.Constant;
import org.qh.DDBMS.common.entity.SiteInfo;
import org.qh.DDBMS.common.input.ServerConfig;
import org.qh.DDBMS.common.input.SocketManager;
import org.qh.DDBMS.common.output.Connector;
import org.qh.DDBMS.common.output.DDBMSSender;
import org.qh.DDBMS.common.ack.AckHandler;
import org.qh.DDBMS.common.protocol.ACKProtocol;

import javax.annotation.Resource;


/**
 *
 * @Author: qihe
 * @Date: 2024/11/25
 * @Version: 0.0.0
 * @Description: 用于处理接受到有关主从模块的ACK信息
 */
@AckHandler
public class MSACKHandler {

    /**
     * <pre>
     * 说明：用于获取当前站点的配置信息
     * </pre>
     */
    @Resource
    private ServerConfig serverConfig;

    /**
     * <pre>
     * 说明：主从模块顶级接口
     * </pre>
     */
    @Resource
    private MasterSlaveManager manager;

    /**
     * <pre>
     * 说明：用于发送协议给到其他站点
     * </pre>
     */
    @Resource
    private DDBMSSender<String> sender;

    /**
     * <pre>
     * 说明：用于建立与主站点的连接
     * </pre>
     */
    @Resource
    private Connector connector;

    /**
     * <pre>
     * 说明：用于删除旧的主站点信息
     * </pre>
     */
    @Resource
    private SocketManager socketManager;


    /**
     * <pre>
     * 说明：处理接收到的主站点信息
     * 注解：@AckHandler(ASSIGN_MASTER_INFO)
     * 实现步骤：
     *   1) 调用manager的setMaster方法
     *   2) 建立与新主站点的连接
     *   3) 断开与旧主站的连接
     * </pre>
     * @param siteInfo 主站点信息
     * @since 0.0.0
     */
    @AckHandler(Constant.ACKType.ASSIGN_MASTER_INFO)
    public void handleMasterInfo(SiteInfo siteInfo) throws Exception {
        SiteInfo oldMaster = manager.master();
        manager.setMaster(siteInfo);
        if (!manager.isMaster()) connector.connect(siteInfo.getIp(), siteInfo.getPort());
        if (oldMaster != null) disconnect(oldMaster);
    }

    /**
     * <pre>
     * 说明：处理接收到的请求站点信息
     * 注解：@AckHandler(REQUEST_SITE_INFO)
     * 实现步骤：
     *   1) 将本站点的主站点信息设置为null
     *     1. 当前站点之前存在主站点，则断开与主站点的连接
     *   2) 从manager获取本站点信息
     *   3) 将本站点信息封装为一个ACK协议
     *   4) 将协议发送给请求站点
     * </pre>
     * @param requestSite 请求当前站点信息的站点
     * @since 0.0.0
     */
    @AckHandler(Constant.ACKType.REQUEST_SITE_INFO)
    public void handleRequestSiteInfo(String requestSite) throws Exception {
        SiteInfo master = manager.master();
        manager.setMaster(null);
        SiteInfo siteInfo = manager.siteInfo();
        sender.send(requestSite, new ACKProtocol(Constant.ACKType.SITE_INFO, siteInfo), null, false);

        if (master != null) disconnect(master);
    }

    /**
     * <pre>
     * 说明；用于断开于旧主站点之间的连接
     * </pre>
     * @param oldMaster 旧的主站点
     * @since 0.0.0
     */
    private void disconnect(SiteInfo oldMaster) throws Exception {
        socketManager.removeSite(oldMaster.getName());
    }

    /**
     * <pre>
     * 说明：处理接受到的从站点信息
     * 注解：@AckHandler(SITE_INFO)
     * 实现步骤：
     *   1) 判断该站点负责的数据库就是当前站点负责的数据库，则将该站点信息加入到msManager中
     *   2) 否则，直接返回
     * </pre>
     * @param siteInfo 从站点信息
     * @since 0.0.0
     */
    @AckHandler(Constant.ACKType.SITE_INFO)
    public void handleSlaveInfo(SiteInfo siteInfo) throws Exception {
        if (siteInfo == null) throw new ProtocolResolveException("The siteInfo is null");
        if (!serverConfig.dbName().equals(siteInfo.getDbName())) return;
        manager.addSite(siteInfo);
    }


    /**
     * <pre>
     * 说明：处理收到的Ready协议
     * 注解：@AckHandler(READY)
     * 实现步骤：
     *   1) 向该站点发送站点信息，表示告知对方身份。
     * </pre>
     * @param site 准备好的站点名
     * @since 0.0.0
     */
    @AckHandler(Constant.ACKType.READY)
    public void handleSlaveInfo(String site) throws Exception {
        if (site == null) throw new ProtocolResolveException("The site is null");
        sender.send(site, new ACKProtocol(Constant.ACKType.SITE_INFO,
                manager.siteInfo()), null, false);

    }
}

