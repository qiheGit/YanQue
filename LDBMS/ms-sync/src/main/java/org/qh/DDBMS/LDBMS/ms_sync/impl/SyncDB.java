package org.qh.DDBMS.LDBMS.ms_sync.impl;

import com.qh.protocol.net.BaseTransportProtocol;
import com.qh.protocol.net.TransportProtocol;
import lombok.SneakyThrows;
import org.qh.DDBMS.LDBMS.ms_sync.AbstractSync;
import org.qh.DDBMS.LDBMS.ms_sync.SyncSender;
import org.qh.DDBMS.LDBMS.ms_sync.dao.SyncDao;
import org.qh.DDBMS.LDBMS.ms_sys.MasterSlaveManager;
import org.qh.DDBMS.LDBMS.tx.LTransactionCenter;
import org.qh.DDBMS.common.Constant;
import org.qh.DDBMS.common.config.GDBMSConfig;
import org.qh.DDBMS.common.entity.SyncInfoEntity;
import org.qh.DDBMS.common.input.ServerConfig;
import org.qh.DDBMS.common.output.DDBMSSender;
import org.qh.DDBMS.common.protocol.RequestSyncInfoProtocol;
import org.qh.DDBMS.common.protocol.SyncInfoProtocol;
import org.qh.tools.clz.LauncherUtils;
import org.qh.tools.thread.ThreadUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/28
 * @Version: 0.0.0
 * @Description: AbstractSync的实现类
 */
public class SyncDB extends AbstractSync<List<SyncInfoEntity>> {

    /**
     * <pre>
     * 说明：局部事务中心
     * </pre>
     */
    @Resource
    private LTransactionCenter txCenter;

    /**
     * <pre>
     * 说明：执行数据库操作
     * </pre>
     */
    @Resource
    private SyncDao syncDao;


    /**
     * <pre>
     * 说明：缓存的同步事务
     * </pre>
     */
    private Queue<SyncInfoEntity> syncInfoQueue = new PriorityQueue<>((s1, s2)-> Math.toIntExact(s1.getId() - s2.getId()));

    /**
     * <pre>
     * 说明：用于将协议信息发送给其他站点
     * </pre>
     */
    @Resource
    private DDBMSSender<String> sender;

    /**
     * <pre>
     * 说明：获取主站点信息
     * </pre>
     */
    @Resource
    private MasterSlaveManager msManager;

    /**
     * <pre>
     * 说明：用于获取GDBMS信息
     * </pre>
     */
    @Resource
    private GDBMSConfig gdbmsConfig;

    /**
     * <pre>
     * 说明：将同步信息发送给从站点的发送器
     * </pre>
     */
    @Resource
    private SyncSender syncSender;

    /**
     * <pre>
     * 说明：用于获取当前站点负责的数据库名
     * </pre>
     */
    @Resource
    private ServerConfig serverConfig;


    /**
     * <pre>
     * 说明：缺失同步信息次数
     * </pre>
     */
    private volatile int lostSyncInfo;

    public SyncDB() {
        super(Constant.ObjectProtocolDataUse.SYNC_INFO_PROTOCOL);
    }

    @PostConstruct
    public void start() {
        ThreadUtils.scheduleWithFixedDelay(() -> doSync0(), 1000, 1000, TimeUnit.MILLISECONDS);
    }

    /**
     * <pre>
     * 说明：用于验证传入参数是否合规
     * 实现步骤：
     *   1) 判定协议的dataUse!=当前实例的dataUse返回null
     *   2) 将BaseTransportProtocol转换成SyncInfoProtocol
     *   3) 判断当前协议的data为null，则表示当前站点缺失的事务太多，无法通过正常途径补全。所以打印出错误信息，
     *   并停止服务。
     *   4) 获取入参的data，并构建list
     *   5) 遍历data
     *     1. 判定当前data[i]已经被执行过，则忽略
     *     2. 将data[i]加入list
     *   6) 判定list为空返回null
     *   7) 返回list
     * </pre>
     *
     * @param protocol 当前协议实例
     * @return 错误信息或null
     * @since 0.0.0
     */
    @SneakyThrows
    @Override
    public List<SyncInfoEntity> validate(BaseTransportProtocol protocol) {
        if (protocol.data()[9] != getDataUse()) return null;
        SyncInfoProtocol syncInfoProtocol = new SyncInfoProtocol(protocol.data());
        ArrayList<SyncInfoEntity> list = (ArrayList<SyncInfoEntity>) syncInfoProtocol.getData()[0];
        if (list == null || list.isEmpty()) {
            System.err.println("The site lack too many transactions to continue!");
            LauncherUtils.destroy();
        }

        ArrayList<SyncInfoEntity> res = new ArrayList<>(list.size());
        for (SyncInfoEntity info : list) {
            if (txCenter.transactionCount() >= info.getId()) continue;
            res.add(info);
        }
        if (res.isEmpty()) return null;
        return res;
    }

    /**
     * <pre>
     * 说明：缓存数据同步信息操作
     * 注意：同步方法
     * 实现步骤：
     *   1) 将入参全部装入syncInfoQueue
     * </pre>
     *
     * @param syncInfoEntities 需要同步的信息列表
     * @return void
     * @since 0.0.0
     */
    @Override
    public TransportProtocol doSync(List<SyncInfoEntity> syncInfoEntities) throws Exception {
        synchronized (syncInfoQueue) {
            syncInfoEntities.addAll(syncInfoEntities);
        }
        return null;
    }

    /**
     * <pre>
     * 说明：将同步信息提交到数据库中
     * 规范：
     *   1) 该方法每秒执行一次
     *   2) 缺失同步信息3次向主站点请求一次同步性信息
     * 注意：同步问题
     * 实现步骤：
     *   1) 当前队列为空，直接返回
     *   2) 判定队列中第一个同步信息的id>当前数据库提交事务数+1
     *     1. 判定lostSyncInfo<3
     *       - lostSyncInfo+=1
     *     2. 判定lostSyncInfo>=3
     *       - lostSyncInfo=0
     *       - 向主站点发送请求同步信息协议
     *     3. 退出当前执行
     *   3) 判定队列中第一个同步信息的id<当前数据库提交事务数+1
     *     1. 移除该同步信息
     *   4) 判定队列中第一个同步信息的id=当前数据库提交事务数+1
     *     1. 当前数据库提交事务数+1
     *     2. 将同步信息和事务对数据库的更新均写入数据库
     *     3. 将该同步信息给到syncSender
     *     4. 将该同步信息移除
     *   5) 执行2),3)和4)，直到无法进行下去
     * </pre>
     *
     * @since 0.0.0
     */
    public void doSync0()  {
        synchronized (syncInfoQueue) {
            while (!syncInfoQueue.isEmpty()) {
                SyncInfoEntity peek = syncInfoQueue.peek();
                long expected = txCenter.transactionCount() + 1;
                if (peek.getId() > expected) { // 2)
                    lostSyncInfo += 1;
                    if (lostSyncInfo >= 3) {
                        lostSyncInfo = 0;
                        requestSyncInfo();
                        return;
                    }
                } else if (peek.getId() == expected) { // 4)
                    commitSyncInfo(peek);
                    syncSender.send(peek);
                }
                syncInfoQueue.poll();
            }
        }
    }


    /**
     * <pre>
     * 说明：提交同步信息
     * 实现步骤：
     *   1. 将commitSyncInfo0注册到事务中心
     *   2. 执行事务中心提交事务逻辑
     * </pre>
     * @param info 同步信息
     * @since 0.0.0
     */
    private void commitSyncInfo(SyncInfoEntity info) {
        txCenter.registerCommit(info.getTransaction(), id -> syncDao.commitSyncInfo(info));
        txCenter.commit(info.getTransaction(), info.getId());
    }


    /**
     * <pre>
     * 说明：请求缺失的同步信息
     * 实现步骤：
     *   1. 当前站点有主站点则直接向主站点请求同步信息，否则向GDBMS请求同步信息
     *   2. 构建请求同步信息协议
     *   3. 将协议内容发送出去
     * </pre>
     * @since 0.0.0
     */
    private void requestSyncInfo() {
        String site = null;
        if (msManager.master() == null) site = gdbmsConfig.siteName();
        else site = msManager.master().getName();

        RequestSyncInfoProtocol protocol = new RequestSyncInfoProtocol(serverConfig.dbName(),
                txCenter.transactionCount(), serverConfig.siteName());

        sender.send(site, protocol, null, false);

    }


}

