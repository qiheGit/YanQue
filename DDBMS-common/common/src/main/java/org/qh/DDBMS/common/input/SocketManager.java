package org.qh.DDBMS.common.input;

import io.netty.channel.ChannelHandlerContext;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/16
 * @Version: 0.0.0
 * @Description: 管理所有socket的pipeline和其所属站点的映射关系
 */
public interface SocketManager {

    /**
     * <pre>
     * 说明：注册socket和其站点名的关系
     * </pre>
     * @param ctx ChannelHandlerContext 传入的上下文
     * @param siteName String 站点名称
     * @since 0.0.0
     */
    void register(ChannelHandlerContext ctx, String siteName) throws Exception;

    /**
     * <pre>
     * 说明：
     *   1. 传入socket的上下文，将站点和socket的映射关系从该管理器中移除。
     *   2. 该方法在连接关闭时会被调用
     * </pre>
     * @param ctx ChannelHandlerContext 传入的上下文
     * @since 0.0.0
     */
    void remove(ChannelHandlerContext ctx) throws Exception;

    /**
     * <pre>
     * 说明：传入socket的上下文，将站点和socket的映射关系从该管理器中移除。
     * </pre>
     * @param site 站点名
     * @since 0.0.0
     */
    void removeSite(String site) throws Exception;

    /**
     * <pre>
     * 说明：传入一个站点名，获取其对应的ChannelHandlerContext实例
     * </pre>
     * @param siteName String 站点名称
     * @return ChannelHandlerContext 对应的上下文实例
     * @since 0.0.0
     */
    ChannelHandlerContext get(String siteName);

    /**
     * <pre>
     * 说明：传入一个ChannelHandlerContext实例，获取其对应的站点名称
     * </pre>
     * @param ctx ChannelHandlerContext 传入的上下文
     * @return String 对应的站点名称
     * @since 0.0.0
     */
    String get(ChannelHandlerContext ctx);
}
