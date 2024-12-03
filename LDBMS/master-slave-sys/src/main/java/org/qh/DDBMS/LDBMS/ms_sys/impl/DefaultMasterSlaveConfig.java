package org.qh.DDBMS.LDBMS.ms_sys.impl;

import lombok.Data;
import org.qh.DDBMS.LDBMS.ms_sys.MasterSlaveConfig;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/25
 * @Version: 0.0.0
 * @Description: MasterSlaveConfig接口的默认实现类
 */
@Data
public class DefaultMasterSlaveConfig implements MasterSlaveConfig {

    /**
     * <pre>
     * 说明：本站点是否可以作为主站点
     * </pre>
     */
    private boolean canBeMaster;

    @Override
    public boolean canBeMaster() {
        return canBeMaster;
    }
}
