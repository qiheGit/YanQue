package org.qh.DDBMS.LDBMS.invoke_method;

import com.qh.protocol.net.BaseTransportProtocol;
import org.qh.DDBMS.common.input.DDBMSReceiver;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/24
 * @Version: 0.0.0
 * @Description: 一个方法执行接口，向其他程序提供调用用户注册模块中方法的API。
 */
public interface MethodExecutor  {

    /**
     * <pre>
     * 说明：根据传入的传输协议实例，调用用户编写的方法，并将执行结果返回给调用者。
     * </pre>
     * @param transportProtocol 传输协议实例
     * @since 0.0.0
     */
    Object invoke(BaseTransportProtocol transportProtocol) throws Exception;
}
