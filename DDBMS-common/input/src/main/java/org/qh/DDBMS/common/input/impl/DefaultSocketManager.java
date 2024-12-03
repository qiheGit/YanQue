package org.qh.DDBMS.common.input.impl;

import com.qh.func.Destroyer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundInvoker;
import org.qh.DDBMS.common.input.SocketManager;
import org.qh.DDBMS.common.msk.SymmetricKeyManager;
import org.qh.DDBMS.common.msm.SiteManager;
import org.qh.tools.pool.DefinedElementPool;
import org.qh.tools.pool.DefinedElementPoolImpl;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/16
 * @Version: 0.0.0
 * @Description: SocketManage接口的默认实现类
 */
public class DefaultSocketManager implements SocketManager {

    /**
     * <pre>
     * 说明：socket和站点名的映射关系
     * </pre>
     */
    private Map<Channel, String> socketSite = new ConcurrentHashMap<>();


    /**
     * <pre>
     * 说明：站点名和socket的映射关系
     * 注意：一个站点名可以映射多个socket
     * </pre>
     */
    private Map<String, DefinedElementPool<Channel>> siteSocket = new ConcurrentHashMap<>();

    /**
     * <pre>
     * 说明：用于删除无连接的站点
     * </pre>
     */
    @Resource
    private SiteManager siteManager;

    /**
     * <pre>
     * 说明：用于删除无连接站点对应的密钥
     * </pre>
     */
    @Resource
    private SymmetricKeyManager<String> keyManager;

    private static final Destroyer<Channel> CHANNEL_DESTROYER = ChannelOutboundInvoker::close;


    /**
     * <pre>
     * 说明：注册socket和其站点名的关系
     * 实现步骤：
     *   1) 判定socket已经被保存，则直接返回
     *   2) 将该socket和站点名的映射关系保存到socketSite中
     *   3) 将站点名和socket的关系保存到sitesocket中
     *   4) 2)和3)要么同时成功要么同时失败，失败则抛出异常信息
     * </pre>
     *
     * @param ctx ChannelHandlerContext 实例
     * @param siteName 站点名
     * @since 0.0.0
     */
    public void register(ChannelHandlerContext ctx, String siteName) throws Exception {
        if (socketSite.containsKey(ctx.pipeline().channel())) return;
        try {
            socketSite.put(ctx.pipeline().channel(), siteName);
            DefinedElementPool<Channel> pool = getSiteSocket(siteName);
            pool.addObject(ctx.pipeline().channel());
        } catch (Throwable e) {
            remove(ctx);
            throw new RuntimeException(e);
        }
    }

    /**
     * <pre>
     * 说明：获取站点名对应的ObjectPool
     * 实现步骤：
     *   1. 从sitesocket中得到pool
     *   2. pool == null
     *     2.1. 构建一个pool实例加入sitesocket
     *   3. 返回pool
     * </pre>
     * @param siteName 站点名
     * @return ObjectPool实例
     * @since 0.0.0
     */
    private DefinedElementPool<Channel> getSiteSocket(String siteName) {
        DefinedElementPool<Channel> pool = siteSocket.get(siteName);
        if (pool == null) {
            pool = new DefinedElementPoolImpl<>(CHANNEL_DESTROYER);
            siteSocket.put(siteName, pool);
        }
        return pool;
    }

    /**
     * <pre>
     * 说明：传入一个站点名，将其和socket的映射关系从该管理器中移除。
     * 注意：该操作是一个原子操作
     * 实现步骤：
     *   1) 该socket没有被保存，则直接返回
     *   2) 将该socket和站点名的映射关系从socketSite中删除
     *   3) 将站点名和socket的关系从sitesocket中删除
     *   4) 2)和3)要么同时成功要么同时失败，失败则抛出异常信息
     *   5) 删除后池子是空的则将池子本身删除和站点信息删除
     * </pre>
     *
     * @param ctx socket上下文
     * @since 0.0.0
     */
    @Override
    public void remove(ChannelHandlerContext ctx) throws Exception {
        ctx.close();
        if (!socketSite.containsKey(ctx.pipeline().channel())) return;
        String site = null;
        site = socketSite.remove(ctx.pipeline().channel());
        if (site == null) return;

        DefinedElementPool<Channel> pool = siteSocket.get(site);
        if (pool != null) return;
        pool.removeObject(ctx.pipeline().channel());
        if (pool.isEmpty()) {
            pool.close();
            delSite(site);
        }
    }

    /**
     * <pre>
     * 说明：删除一个站点的缓存信息
     * 实现步骤：
     *   1) 将站点的连接池删除
     *   2) 从站点管理器中删除该站点信息
     *   3) 删除该站点的对称密钥
     * </pre>
     *
     * @param site 站点名
     * @since 0.0.0
     */
    private void delSite(String site) throws Exception {
        siteSocket.remove(site);
        siteManager.delSite(site);
        keyManager.unregisterKey(site);
    }

    /**
     * <pre>
     * 说明：删除一个站点的所有连接
     * 注意：该操作是一个原子操作
     * 实现步骤：
     *   1) 获取移除站点对应的连接池
     *   2) 将连接池关闭
     *   3) 将该站点信息删除
     * </pre>
     *
     * @param site 站点名
     * @since 0.0.0
     */
    @Override
    public void removeSite(String site) throws Exception {
        DefinedElementPool<Channel> pool = siteSocket.remove(site);
        if (pool == null) return;
        pool.close();
        delSite(site);
    }


    /**
     * <pre>
     * 说明：传入一个站点名，获取其对应的ChannelHandlerContext实例
     * 规范：
     *   1) 采用负载均衡的方式获取socket实例
     * 实现步骤：
     *   1) 负载均衡地获取ChannelHandlerContext实例
     *   2) 返回ChannelHandlerContext实例
     * </pre>
     *
     * @param siteName 站点名
     * @return ChannelHandlerContext 对应的socket上下文
     * @since 0.0.0
     */
    public ChannelHandlerContext get(String siteName) {
        Channel socket = null;
        DefinedElementPool<Channel> pool = siteSocket.get(siteName);
        synchronized (pool) {
            try {
                socket = pool.borrowObject();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    pool.returnObject(socket);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return socket.pipeline().lastContext();
    }

    /**
     * <pre>
     * 说明：传入一个ChannelHandlerContext实例，获取其对应的站点名称
     * 实现步骤：
     *   1) 从socketSite中获取对应的站点名并返回
     * </pre>
     *
     * @param ctx ChannelHandlerContext 实例
     * @return String 对应的站点名称
     * @since 0.0.0
     */
    public String get(ChannelHandlerContext ctx) {
        return socketSite.get(ctx.pipeline().channel());
    }
}












