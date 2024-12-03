package org.qh.DDBMS.common.msm;

import org.qh.DDBMS.common.entity.SiteInfo;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/27
 * @Version: 0.0.0
 * @Description: 站点信息管理接口
 */
public interface SiteManager {

    /**
     * <pre>
     * 说明：添加站点信息
     * </pre>
     * @param siteInfo SiteInfo 站点信息对象
     * @return String 添加结果的描述
     * @since 0.0.0
     */
    void addSite(SiteInfo siteInfo) throws Exception;

    /**
     * <pre>
     * 说明：删除站点信息
     * </pre>
     *
     * @param site String 站点标识
     *
     * @since 0.0.0
     */
    void delSite(String site) throws Exception;
}
