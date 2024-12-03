package org.qh.DDBMS.LDBMS.ms_sync.impl;

import org.qh.DDBMS.LDBMS.ms_sync.SyncSender;
import org.qh.DDBMS.LDBMS.ms_sys.MasterSlaveManager;
import org.qh.DDBMS.common.Constant;
import org.qh.DDBMS.common.entity.SyncInfoEntity;
import org.qh.DDBMS.common.output.DDBMSSender;
import org.qh.DDBMS.common.protocol.SyncInfoProtocol;
import org.qh.tools.thread.ThreadUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/29
 * @Version: 0.0.0
 * @Description: SyncSender接口的默认实现类
 */
public class SyncSenderImpl implements SyncSender {

    /**
     * <pre>
     * 说明：用于缓存需要同步的SyncInfoEntity
     * </pre>
     */
    @Resource
    private Queue<SyncInfoEntity> buffer = new LinkedList<>();

    /**
     * <pre>
     * 说明：用于将同步信息发送给从站点
     * </pre>
     */
    @Resource
    private DDBMSSender<String> sender;

    /**
     * <pre>
     * 说明：用于获取从站点信息
     * </pre>
     */
    @Resource
    private MasterSlaveManager msManager;

    @PostConstruct
    public void start() {
        ThreadUtils.scheduleWithFixedDelay(this::sendSyncInfo,
                Constant.Sync.DISPATCH_SYNC_INFO_INTERVAL, Constant.Sync.DISPATCH_SYNC_INFO_INTERVAL,
                TimeUnit.MILLISECONDS);
    }

    /**
     * <pre>
     * 说明：该方法用于发送同步信息给从站点
     * 实现步骤：
     *   1) 判定当前站点有从站点
     *     1. 将收到的同步数据加入从站点同步队列中
     * </pre>
     * @param syncInfo 一个事务的同步信息
     * @since 0.0.0
     */
    @Override
    public void send(SyncInfoEntity syncInfo) {
        if (!msManager.hasSlave()) return;
        synchronized (buffer) {
            buffer.add(syncInfo);
        }
    }

    /**
     * <pre>
     * 说明：该方法执行将同步信息发送给从站点的业务
     * 规范：该方法每SEND_SYNC_INFO_INTERVAL执行一次
     * 实现步骤：
     *   1) 当前缓存队列为空，返回
     *   2) 获取所有缓存的entity，并清空缓存队列
     *   3) 构建一个SyncProtocol实例
     *   4) 获取所有的从站点的站点名
     *   5) 将SyncProtocol实例发送到各个站点
     * </pre>
     * @since 0.0.0
     */
    private void sendSyncInfo() {
        synchronized (buffer) {
            if (buffer.isEmpty()) return;
            SyncInfoProtocol protocol = new SyncInfoProtocol(new ArrayList<>(buffer));
            for (String slave : msManager.slaves()) {
                sender.send(slave, protocol, null, false);
            }
            buffer.clear();
        }
    }
}
