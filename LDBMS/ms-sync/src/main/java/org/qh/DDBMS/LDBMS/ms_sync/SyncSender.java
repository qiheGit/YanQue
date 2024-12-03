package org.qh.DDBMS.LDBMS.ms_sync;

import org.qh.DDBMS.common.db.DBResource;
import org.qh.DDBMS.common.db.DBTransaction;
import org.qh.DDBMS.common.entity.SyncInfoEntity;

import java.util.List;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/26
 * @Version: 0.0.0
 * @Description: 发送同步信息接口
 */
public interface SyncSender {

    /**
     * <pre>
     * 说明：该方法用于发送同步信息
     * </pre>
     * @param syncInfo 一个事务的同步信息
     * @since 0.0.0
     */
    void send(SyncInfoEntity syncInfo);
}

