package org.qh.DDBMS.LDBMS.ms_sys;

import org.qh.DDBMS.common.Constant;
import org.qh.DDBMS.common.config.GDBMSConfig;
import org.qh.DDBMS.common.input.ServerConfig;
import org.qh.DDBMS.common.output.DDBMSSender;
import org.qh.DDBMS.common.protocol.ACKProtocol;
import org.qh.tools.exception.ExceptionUtils;
import org.qh.tools.thread.ThreadUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/25
 * @Version: 0.0.0
 * @Description: 用于自动地维护主从状态信息
 * @Specification:
 *   1. 当没有主站点时，且当前站点能够成为主站点，每10秒向GDBMS发送一次站点信息协议
 *   2. 当没有主站点时，且当前站点不能够成为主站点，每20秒向GDBMS发送一次请求主站的协议
 */
public class MasterSlaveMaintain implements Runnable {

    /**
     * <pre>
     * 说明：用于获取主从状态
     * </pre>
     */
    @Resource
    private MasterSlaveManager manager;

    /**
     * <pre>
     * 说明：用于发送协议内容
     * </pre>
     */
    @Resource
    private DDBMSSender<String> sender;

    /**
     * <pre>
     * 说明：用于获取主从模块的配置信息
     * </pre>
     */
    @Resource
    private MasterSlaveConfig msConfig;

    /**
     * <pre>
     * 说明：用于获取本LDBMS站点信息
     * </pre>
     */
    @Resource
    private ServerConfig serverConfig;

    /**
     * <pre>
     * 说明：用于获取GDBMS有关信息
     * </pre>
     */
    @Resource
    private GDBMSConfig gdbmsConfig;

    private ScheduledFuture<?> future; // 用于终止任务

    private boolean requestMaster = false;

    @PostConstruct
    public void start() {
        future = ThreadUtils.scheduleWithFixedDelay(this, 1000, 1000 * 10, TimeUnit.MILLISECONDS);
    }

    /**
     * <pre>
     * 说明：自动地维护主站点
     * 实现步骤：
     *   1) 判定当前站点已经知道哪个是主站点，则返回
     *   2) 判定当前站点可以成为主站点，则向GDBMS发送本站点信息协议。
     *   3) 判定当前站点不可以成为主站点，则向GDBMS发送请求站点信息协议。
     * </pre>
     * @since 0.0.0
     */
    @Override
    public void run() {
        try {
            if (manager.master() != null) return;
            ACKProtocol protocol = null;
            if (msConfig.canBeMaster()) {
                protocol = new ACKProtocol(Constant.ACKType.SITE_INFO, manager.siteInfo());
            } else if (requestMaster = !requestMaster){
                protocol = new ACKProtocol(Constant.ACKType.REQUEST_SITE_INFO, serverConfig.dbName(), serverConfig.siteName());
            }
            if (protocol != null) {
                sender.send(gdbmsConfig.siteName(), protocol, null, false);
            }
        } catch (Exception e) {
            ExceptionUtils.printStackTrace(e);
        }
    }

    @PreDestroy
    public void destroy() {
        future.cancel(false);
    }
}

