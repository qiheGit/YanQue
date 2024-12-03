package org.qh.DDBMS.LDBMS.ms_sys.impl;

import org.qh.DDBMS.LDBMS.ms_sys.MasterSlaveConfig;
import org.qh.DDBMS.LDBMS.ms_sys.MasterSlaveManager;
import org.qh.DDBMS.LDBMS.tx.LTransactionCenter;
import org.qh.DDBMS.common.entity.SiteInfo;
import org.qh.DDBMS.common.input.ServerConfig;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/25
 * @Version: 0.0.0
 * @Description: MasterSlaveManager接口的默认实现类
 */
public class DefaultMasterSlaveManager implements MasterSlaveManager {

    /**
     * <pre>
     * 说明：用于获取当前站点提交事务数
     * </pre>
     */
    @Resource
    private LTransactionCenter center;

    /**
     * <pre>
     * 说明：获取主从模块的配置信息
     * </pre>
     */
    @Resource
    private MasterSlaveConfig msConfig;

    /**
     * <pre>
     * 说明：获取站点服务配置信息
     * </pre>
     */
    @Resource
    private ServerConfig serverConfig;

    /**
     * <pre>
     * 说明：主站点信息
     * </pre>
     */
    private SiteInfo master;

    /**
     * <pre>
     * 说明：当前站点是不是主站点
     * </pre>
     */
    private boolean isMaster;

    /**
     * <pre>
     * 说明：从站点集合
     * </pre>
     */
    private Map<String, SiteInfo> slaves = new ConcurrentHashMap<>();

    /**
     * <pre>
     * 说明：获取当前站点是不是主站点
     * 实现步骤：
     *   1) 返回isMaster属性
     * </pre>
     * @since 0.0.0
     */
    public boolean isMaster() {
        return isMaster;
    }

    /**
     * <pre>
     * 说明：获取主站点信息
     * 实现步骤：
     *   1) 返回master
     * </pre>
     * @since 0.0.0
     */
    public SiteInfo master() {
        return master;
    }

    /**
     * <pre>
     * 说明：获取本站点信息
     * 实现步骤：
     *   1) 从serverConfig获取本站点的站点名
     *   2) 从serverConfig获取本站点维护的数据库名
     *   3) 从serverConfig获取本站点外部ip地址
     *   4) 从serverConfig获取本站点服务监听端口
     *   5) 从msConfig获取本站点是否可以成为主站点
     *   6) 从center获取当前站点提交事务数
     * </pre>
     * @since 0.0.0
     */
    public SiteInfo siteInfo() {
        return new SiteInfo(serverConfig.siteName(), serverConfig.dbName(), serverConfig.ip(),
                serverConfig.port(), msConfig.canBeMaster(), center.transactionCount());
    }

    /**
     * <pre>
     * 说明：设置主站点信息
     * 注意：该方法是一个同步方法
     * 实现步骤：
     *   1) 将传入参数设置为master
     *   2) 将isMaster设置为本站点名equals主站点名
     * </pre>
     * @param master 主站点信息
     * @since 0.0.0
     */
    public synchronized void setMaster(SiteInfo master) {
        this.master = master;
        this.isMaster = serverConfig.siteName().equals(master.getName());
    }

    /**
     * <pre>
     * 说明：判断当前站点是否存在从站点
     * 实现步骤：
     *   1) slaves为空返回false，否则返回true
     * </pre>
     * @since 0.0.0
     */
    @Override
    public boolean hasSlave() {
        return !slaves.isEmpty();
    }

    /**
     * <pre>
     * 说明：获取当前站点所有的从站点
     * 实现步骤：
     *   1) 遍历slaves，将所有站点名封装为一个String[]
     *   2) 返回结果
     * </pre>
     * @since 0.0.0
     */
    @Override
    public String[] slaves() {
        String[] res = slaves.keySet().toArray(new String[0]);
        return res;
    }

    /**
     * <pre>
     * 说明：添加一个从站点信息
     * 实现步骤：
     *   1) 将站点信息加入到slaves中
     * </pre>
     * @param slave 从站点信息
     * @since 0.0.0
     */
    @Override
    public void addSite(SiteInfo slave) throws Exception {
        slaves.put(slave.getName(), slave);
    }
    /**
     * <pre>
     * 说明：删除一个从站点信息
     * 实现步骤：
     *   1) 将站点信息从slaves中删除
     * </pre>
     * @param site 从站点名信息
     * @since 0.0.0
     */
    @Override
    public void delSite(String site) throws Exception {
        slaves.remove(site);
    }
}

