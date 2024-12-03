package org.qh.DDBMS.LDBMS.ms_sys;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/25
 * @Version: 0.0.0
 * @Description: 主从模块的配置接口
 */
public interface MasterSlaveConfig {
    /**
     * 说明：本站点是否可以作为主站点的配置
     * @return true表示可以成为主站点，false表示不可以
     * @since 0.0.0
     */
    boolean canBeMaster();
}
