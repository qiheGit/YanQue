package org.qh.DDBMS.GDBMS.msm;

import org.qh.DDBMS.common.entity.SiteInfo;
import org.qh.DDBMS.common.msm.SiteManager;


/**
 *
 * @Author: qihe
 * @Date: 2024/11/19
 * @Version:
 * @Description: 主从站点的管理接口
 */
public interface MasterSlaveManager extends SiteManager {

    /**
     * <pre>
     * 说明：获取传输数据库的主站点
     * </pre>
     * @param dbName String 数据库名称
     * @return String 主站点地址
     * @since 0.0.0
     */
    String masterSite(String dbName);

    /**
     * <pre>
     * 说明：获取传入数据库的从站点
     * </pre>
     * @param dbName String 数据库名称
     * @return String 从站点地址
     * @since 0.0.0
     */
    String slaveSite(String dbName) throws Exception;


    /**
     * <pre>
     * 说明：获取数据库对应主站点信息
     * </pre>
     * @param dbName String 数据库名称
     * @return SiteInfo 主站点信息对象
     * @since 0.0.0
     */
    SiteInfo getMasterInfo(String dbName);
}

