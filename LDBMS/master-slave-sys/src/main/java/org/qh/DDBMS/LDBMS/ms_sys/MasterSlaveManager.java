package org.qh.DDBMS.LDBMS.ms_sys;

import org.qh.DDBMS.common.entity.SiteInfo;
import org.qh.DDBMS.common.msm.SiteManager;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/25
 * @Version: 0.0.0
 * @Description: 管理主从站点的接口，也是主从模块的顶层接口
 */
public interface MasterSlaveManager extends SiteManager {

    /**
     * <pre>
     * 说明：获取当前站点是不是主站点
     * </pre>
     * @return boolean 当前站点是否为主站点
     * @since 0.0.0
     */
    boolean isMaster();

    /**
     * <pre>
     * 说明：获取主站点信息
     * </pre>
     * @return SiteInfo 主站点信息对象
     * @since 0.0.0
     */
    SiteInfo master();

    /**
     * <pre>
     * 说明：获取本站点信息
     * </pre>
     * @return SiteInfo 本站点信息对象
     * @since 0.0.0
     */
    SiteInfo siteInfo();

    /**
     * <pre>
     * 说明：设置主站点信息
     * </pre>
     * @param siteInfo SiteInfo 主站点信息对象
     * @since 0.0.0
     */
    void setMaster(SiteInfo siteInfo);

    /**
     * <pre>
     * 说明：判断当前站点是否存在从站点
     * </pre>
     * @return true，当前站点存在从站点，否则没有从站点。
     * @since 0.0.0
     */
    boolean hasSlave();

    /**
     * <pre>
     * 说明：获取当前站点所有的辅站点
     * </pre>
     * @return 本站点所有从站点名数组
     * @since 0.0.0
     */
    String[] slaves();
}

