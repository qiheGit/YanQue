package org.qh.DDBMS.common.input;

import com.qh.exception.ExceptionHandlerSign;
import com.qh.protocol.exception.ProtocolException;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.DecoderException;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.qh.tools.exception.ExceptionUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @Author: qihe
 * @Date: 2024/6/27
 * @Version: 1.0.0
 * @Description: 用来处理netty异常的handler
 */
@ChannelHandler.Sharable
@Slf4j
public class ChannelExceptionHandler extends ChannelInboundHandlerAdapter {
    /**
     * 说明：该属性持有该类中所有处理异常的方法
     * 规范：
     *   1. 该类中所有被 {@link ExceptionHandlerSign} 修饰的方法则是处理异常的方法
     */
    private static final Set<Method> METHODS = new HashSet<>(
            Arrays.stream(ChannelExceptionHandler.class.getDeclaredMethods())
                    .filter((m) -> m.isAnnotationPresent(ExceptionHandlerSign.class))
                    .collect(Collectors.toList())
    );


    /**
     * 说明：接收处理特定的连接事件，使其不再向后传递以至于污染日志
     * @param ctx channel的上下文对象
     * @param cause 事件对象
     * @throws Exception
     * @since 1.0.0
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (handlerEx(ctx, cause)) return;
        super.exceptionCaught(ctx, cause);
    }

    /**
     * 说明：调用具体异常处理方法进行异常处理
     * @param ctx 上下文对象
     * @param cause 发生的异常
     * @return true表示异常被处理，false表异常没有被处理
     * @throws Exception
     * @since 1.0.0
     */
    private boolean handlerEx(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        boolean res = false;
        for (Method method : METHODS) {
            res = (boolean) method.invoke(this, ctx, cause);
            if (res) break;
        }
        if (res) ReferenceCountUtil.release(cause); // 模仿tailHandler处理
        return res;
    }

    /**
     * 说明：处理连接超时事件
     * 实现步骤：
     *   1. 判定不是一个连接超时事件返回false
     *   2. 关闭连接返回true
     * @param ctx channel上下文
     * @param cause 事件
     * @return false表示事件没有被处理，true表示事件被处理
     * @since 1.0.0
     */
    @ExceptionHandlerSign
    private boolean handlerConnectionTimeOutEx(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof IOException && "Connection timed out".equals(cause.getMessage())) {
            ctx.close();
            return true;
        }
        return false;
    }

    /**
     * 说明：对连接释放异常进行处理
     * 实现步骤：
     *   1. 判断不是一个连接释放异常返回false
     *   2. 关闭连接并返回true
     * @param ctx 上下文对象
     * @param cause 出现的异常
     * @return false表示事件没有被处理，true表示事件被处理
     * @since 1.0.0
     */
    @ExceptionHandlerSign
    private boolean handlerConnectionResetByPeerEx(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof IOException && "Connection reset by peer".equals(cause.getMessage())) {
            ctx.close();
            return true;
        }
        return false;
    }

    /**
     * 说明： 对发生的{@link ProtocolException} 异常进行处理
     * 实现步骤：
     *   1. 判断不是{@link ProtocolException} 则返回false
     *   2. 对异常进行处理
     *     2.1. 打印出连接的远程地址
     *     2.2. 输出错误信息
     *     2.3. 关闭连接
     * @param ctx 上下文对象
     * @param cause 发生的异常
     * @return false表示事件没有被处理，true表示事件被处理
     * @since 1.0.0
     */
    @ExceptionHandlerSign
    private boolean handlerProtocolEx(ChannelHandlerContext ctx, Throwable cause) {
        if (!(cause instanceof DecoderException &&
                cause.getCause() instanceof ProtocolException)) return false;
        log.info("Remote Address: {}", ctx.channel().remoteAddress());
        ExceptionUtils.printStackTrace(cause);
        ctx.close();
        return true;
    }
}
