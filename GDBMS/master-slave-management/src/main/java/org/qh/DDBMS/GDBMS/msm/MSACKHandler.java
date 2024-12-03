package org.qh.DDBMS.GDBMS.msm;

import org.qh.DDBMS.common.Constant;
import org.qh.DDBMS.common.ack.AckHandler;
import org.qh.DDBMS.common.entity.SiteInfo;
import org.qh.DDBMS.common.output.DDBMSSender;
import org.qh.DDBMS.common.protocol.ACKProtocol;


import javax.annotation.Resource;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/19
 * @Version: 0.0.0
 * @Description: 处理接受到的关于主从管理的ACK协议
 */
public class MSACKHandler {

    /**
     * <pre>
     * 说明：主从管理器
     * </pre>
     */
    @Resource
    private MasterSlaveManager msManager;
    /**
     * <pre>
     * 说明：发送协议内容的发送器
     * </pre>
     */
    @Resource
    private DDBMSSender<String> sender;

    /**
     * <pre>
     * 规范：
     *   1) 注解：@AckHandler(SITE_INFO)
     *   2) data[]: SiteInfo
     * 实现步骤：
     *   1) 调用msManager.addSite()
     * </pre>
     */
    @AckHandler(Constant.ACKType.SITE_INFO)
    public void handlerSiteInfo(SiteInfo info) throws Exception {
        msManager.addSite(info);
    }

    /**
     * <pre>
     * 说明：处理辅站点请求主站点信息协议
     * 实现步骤：
     *   1) 调用msManager.getMasterInfo()获取主站点信息
     *   2) 将得到的实例封装为一个Ack协议实例并返回
     * </pre>
     *
     * @param dbName 数据库名称
     * @return AckProtocol 返回的ACK协议实例
     */
    @AckHandler(Constant.ACKType.REQUEST_SITE_INFO)
    public void getMasterInfo(String dbName, String siteName) throws Exception {
        SiteInfo masterInfo = msManager.getMasterInfo(dbName);
        sender.send(siteName, new ACKProtocol(Constant.ACKType.ASSIGN_MASTER_INFO, masterInfo), null, false);
    }
}



















