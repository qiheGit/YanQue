package org.qh.DDBMS.GDBMS.idm;

import org.qh.DDBMS.common.input.DDBMSReceiver;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/17
 * @Version: 0.0.0
 * @Description: 执行数据库中方法的接口(Invoke Distributed Database Method)
 */
public interface IDDBMethod {

    /**
     * <pre>
     * 说明：执行参数指定数据库上的方法
     * </pre>
     * @param dbName String 数据库名称
     * @param callbackKey 本次调用的回调key
     * @param uri String 方法uri
     * @param receiver 处理结果的接收者
     * @param args Object... 可变参数，方法执行所需的参数
     * @since 0.0.0
     */
    void invoke(String dbName, long callbackKey, String uri, DDBMSReceiver receiver, Object... args) throws Exception;

    /**
     * <pre>
     * 说明：执行参数指定数据库上的方法，且当前事务是只读事务。
     * </pre>
     * @param dbName String 数据库名称
     * @param callbackKey 本次调用的回调key
     * @param uri String 方法uri
     * @param receiver 处理结果的接收者
     * @param args Object... 可变参数，方法执行所需的参数
     * @since 0.0.0
     */
    void readOnly(String dbName, long callbackKey, String uri, DDBMSReceiver receiver, Object... args) throws Exception;
}

