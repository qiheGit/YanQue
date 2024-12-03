package org.qh.DDBMS.LDBMS.sql.config;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/26
 * @Version: 0.0.0
 * @Description: 用于获取有关SQLBank配置信息的接口
 */
public interface SQLBankConfig {

    /**
     * <pre>
     * 说明：获取配置的缓存事务个数
     * </pre>
     * @return bufferSize 缓存事务个数
     * @since 0.0.0
     */
    int bufferSize();

    /**
     * <pre>
     * 说明：获取最大缓存事务个数
     * <pre/>
     * @return 0.0.0
     * @since 0.0.0
     */
    int maxBufferSize();
}
