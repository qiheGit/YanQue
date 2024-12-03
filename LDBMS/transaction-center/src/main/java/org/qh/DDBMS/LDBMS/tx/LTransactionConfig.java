package org.qh.DDBMS.LDBMS.tx;


/**
 *
 * @Author: qihe
 * @Date: 2024/11/25
 * @Version: 0.0.0
 * @Description: 局部事务中心配置接口
 */
public interface LTransactionConfig {

    /**
     * <pre>
     * 说明：配置的事务类型值
     * </pre>
     * @return int 事务类型值
     * @since 0.0.0
     */
    int transactionType();
}

