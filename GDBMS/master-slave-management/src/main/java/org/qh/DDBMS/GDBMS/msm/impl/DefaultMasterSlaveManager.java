package org.qh.DDBMS.GDBMS.msm.impl;

import org.apache.commons.pool2.ObjectPool;
import org.qh.DDBMS.GDBMS.msm.MasterSlaveManager;
import org.qh.DDBMS.GDBMS.sync.Sync;
import org.qh.DDBMS.common.Constant;
import org.qh.DDBMS.common.entity.SiteInfo;
import org.qh.DDBMS.common.input.ServerConfig;
import org.qh.DDBMS.common.output.DDBMSSender;
import org.qh.DDBMS.common.protocol.ACKProtocol;
import org.qh.tools.pool.DefinedElementPool;
import org.qh.tools.pool.DefinedElementPoolImpl;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/19
 * @Version: 0.0.0
 * @Description: MasterSlaveManager的默认实现类
 */
public class DefaultMasterSlaveManager implements MasterSlaveManager {

    /**
     * <pre>
     * 说明：数据库和其主站点的映射关系
     * </pre>
     */
    private Map<String, SiteInfo> masterSite = new ConcurrentHashMap<>();

    /**
     * <pre>
     * 说明：数据库和其辅站点的映射关系
     * </pre>
     */
    private Map<String, Map<String, SiteInfo>> slaveSite = new ConcurrentHashMap<>();

    /**
     * <pre>
     * 说明：数据库和辅站点池的映射关系
     * </pre>
     */
    private Map<String, DefinedElementPool<String>> slavePool = new ConcurrentHashMap<>();

    /**
     * <pre>
     * 说明：将协议信息发送给站点的发送器
     * </pre>
     */
    @Resource
    private DDBMSSender<String> sender;

    /**
     * <pre>
     * 说明：获取数据库提交事务数
     * </pre>
     */
    @Resource
    private Sync sync;

    /**
     * <pre>
     * 说明：获取本站点ming
     * </pre>
     */
    @Resource
    private ServerConfig serverConfig;

    /**
     * <pre>
     * 说明：获取传输数据库的主站点
     * 实现步骤：
     *   1) 从masterSite中获取主站点并返回
     * </pre>
     *
     * @param databaseName 数据库名称
     * @return 主站点名称
     */
    public String masterSite(String databaseName) {
        return masterSite.get(databaseName).getName();
    }


    /**
     * <pre>
     * 说明：获取传入数据库的从站点
     * 实现步骤：
     *   1) 从slavePool借出一个辅站点，siteName
     *   2) 将siteName还回去pool
     *   3) 返回siteName
     * </pre>
     *
     * @param databaseName 数据库名称
     * @return 从站点名称
     */
    public String slaveSite(String databaseName) throws Exception {
        ObjectPool<String> pool = slavePool.get(databaseName);
        if (pool == null) throw new RuntimeException("The slave site of the database '" + databaseName + "' does not exist.");
        String site = null;
        synchronized (pool) {
            try {
                site = pool.borrowObject();
            } finally {
                pool.returnObject(site);
            }
        }

        return site;
    }

    /**
     * <pre>
     * 说明：添加站点信息
     * 实现步骤：
     *   1) 判定该数据库有主站点
     *     1. 将该站点加入到数据库中对应辅站点集合
     *     2. 将主站点信息分发给该站点
     *     3. 退出
     *   2) 判定该数据库没有主站点
     *     1. 判定当前站点不可以作为主站点
     *       - 将该站点加入到数据库中对应辅站点集合
     *     2. 判定当前站点可以作为主站点
     *       - 获取数据库提交事务数
     *       - 判定站点提交事务数等于数据库提交事务数
     *         > 将该站点设置为主站点
     *         > 将主站点信息分发给所有站点(包括主站点)
     *       - 判定站点提交事务数小于数据库的提交事务数
     *         > 将站点加入数据库辅站点集合
     *         > 告知站点缺少提交事务
     * </pre>
     *
     * @param siteInfo 站点信息
     * @return 结果消息
     */
    public synchronized void addSite(SiteInfo siteInfo) throws Exception {
        if (masterSite.get(siteInfo.getDbName()).getName().equals(siteInfo.getName())) return;
        if (masterSite.containsKey(siteInfo.getDbName()) || !siteInfo.isCanBeMaster()) {
            addSlave(siteInfo);
            return;
        }
        long count = sync.transactionCount(siteInfo.getDbName());
        if (count == siteInfo.getTransactionCount()) {
            setMasterSite(siteInfo);
            return;
        }
        addSlave(siteInfo);
        sender.send(siteInfo.getName(), new ACKProtocol(Constant.ACKType.LACK_COMMITED_TRANSACTION, serverConfig.siteName()), null, false);
    }

    /**
     * <pre>
     * 说明：将站点信息加入到辅站点集合
     * 实现步骤：
     *   1. 判定该站点已经存在于当前数据库的辅站点集合，则忽略添加该站点信息
     *   2. 判定该站点不存在于当前数据库的辅站点集合中
     *     2.1. 将该站点信息加入到slaveSite中
     *     2.1. 将该站点名加入到slavePool中
     *   3. 判定当前站点存在主站点
     *     3.1. 得到主站点信息，并构建分发主站点信息的ACK协议
     *     3.2. 将协议分发给该站点，
     * </pre>
     * @param siteInfo 站点信息
     * @since 0.0.0
     */
    private void addSlave(SiteInfo siteInfo) throws Exception {
        String dbName = siteInfo.getDbName();
        if (slaveSite.get(dbName) == null) {
            slaveSite.put(dbName, new ConcurrentHashMap<>());
            slavePool.put(dbName, new DefinedElementPoolImpl<>());
        }

        String siteName = siteInfo.getName();
        if (!slaveSite.get(dbName).containsKey(siteName)) {
            slaveSite.get(dbName).put(siteName, siteInfo);
            slavePool.get(dbName).addObject(siteName);
        }

        if (!masterSite.containsKey(dbName)) return;

        sender.send(siteName, new ACKProtocol(Constant.ACKType.ASSIGN_MASTER_INFO, getMasterInfo(dbName)), null, false);
    }

    /**
     * <pre>
     * 说明：将站点信息设置为主站点
     * 实现步骤：
     *   1. 判定该站点已经存在于当前数据库的辅站点集合中
     *     1.1. 删除该站点信息
     *   2. 将该数据库的主站设置为该站点
     *   3. 将该站点信息分发给对应数据库所有的站点
     * </pre>
     * @param siteInfo 站点信息
     * @since 0.0.0
     */
    private void setMasterSite(SiteInfo siteInfo) throws Exception {
        String dbName = siteInfo.getDbName();
        String siteName = siteInfo.getName();
        if (slaveSite.get(dbName) != null && slaveSite.get(dbName).containsKey(siteName)) {
            slaveSite.get(dbName).remove(siteName);
            slavePool.get(dbName).removeObject(siteName);
        }
        masterSite.put(dbName, siteInfo);

        ACKProtocol protocol = new ACKProtocol(Constant.ACKType.ASSIGN_MASTER_INFO, siteInfo);
        for (String slaves : slaveSite.get(dbName).keySet()) sender.send(slaves, protocol, null, false);
        sender.send(siteName, protocol, null, false);
    }

    /**
     * <pre>
     * 说明：删除站点信息
     * 实现步骤:
     *   1) 将站点信息删除
     *   2) 判定删除的是一个数据库的主站点
     *     1. 向该数据库所有的辅站点发送请求站点信息协议
     * </pre>
     *
     * @param siteName 站点名称
     */
    public synchronized void delSite(String siteName) throws Exception {
        String dbName = delSite0(siteName);
        if (dbName == null) return;

        ACKProtocol ackProtocol = new ACKProtocol(Constant.ACKType.REQUEST_SITE_INFO, serverConfig.siteName());
        for (String slave : slaveSite.get(dbName).keySet()) {
            sender.send(slave, ackProtocol, null, false);
        }
    }

    /**
     * 说明：删除站点信息
     * 规范：
     *   1. 删除的是主站点返回数据库名，否则null
     * 实现步骤：
     *   1. 判定该站点是不是主站点
     *   2. 是主站点：
     *     2.1. 从masterSite中删除该数据库
     *     2.2. 返回数据库名
     *   3. 不是主站点
     *     3.1. 从slavePool中删除该站点返回null
     * @param site 需要删除的站点名
     * @return null表示删除的是一个辅站点，非null，表示删除主站点的数据库名
     * @since 0.0.0
     */
    private String delSite0(String site) throws Exception {
        String dbName = siteIsMaster(site);
        if (dbName != null) {
            masterSite.remove(dbName);
            return dbName;
        } else if ((dbName = siteIsSlave(site)) != null) {
            slaveSite.get(dbName).remove(site);
            slavePool.get(dbName).removeObject(site);
        }
        return null;

    }

    /**
     * <pre>
     * 说明：判定一个站点是不是一个主站点
     * 规范：传入站点是一个主站点返回
     * 实现步骤：
     *   1. 遍历所有的数据主站点信息，如果主站点名得到匹配则立即返回该数据名
     *   2. 没有数据库主站点名匹配传入站点名，则返回null
     * </pre>
     * @param site 站点名
     * @return 站点对应的数据库名
     * @since 0.0.0
     */
    private String siteIsMaster(String site) {
        String dbName = null;
        for (Map.Entry<String, SiteInfo> entry : masterSite.entrySet()) {
            if (entry.getValue().getName().equals(site)) {
                dbName = entry.getKey();
                break;
            }
        }
        return dbName;
    }

    /**
     * <pre>
     * 说明：判定一个站点是不是一个从站点
     * 规范：传入站点是一个从站点返回数据库名
     * 实现步骤：
     *   1. 遍历所有的数据从站点信息，如果从站点名得到匹配则立即返回该数据名
     *   2. 没有数据库从站点名匹配传入站点名，则返回null
     * </pre>
     * @param site 站点名
     * @return 站点对应的数据库名
     * @since 0.0.0
     */
    private String siteIsSlave(String site) {
        String dbName = null;
        for (Map.Entry<String, Map<String, SiteInfo>> entry : slaveSite.entrySet()) {
            for (String slave : entry.getValue().keySet()) {
                if (slave.equals(site)) {
                    dbName = entry.getKey();
                    break;
                }
            }
        }
        return dbName;
    }

    /**
     * <pre>
     * 说明：获取数据库对应主站点信息
     * 实现步骤：
     *   1) 从masterSite中获取主站点信息并返回
     * </pre>
     *
     * @param databaseName 数据库名称
     * @return 主站点信息
     */
    public SiteInfo getMasterInfo(String databaseName) {
        return masterSite.get(databaseName);
    }
}

