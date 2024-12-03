package org.qh.DDBMS.LDBMS.sql.config.impl;

import org.qh.DDBMS.LDBMS.sql.config.SQLBankConfig;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/26
 * @Version: 0.0.0
 * @Description: SQLBankConfig实现类
 */

public class SQLBankConfigImpl implements SQLBankConfig {
    private int bufferSize = 1024; // 缓存事务个数
    private int maxBufferSize = 2048; // 最大缓存事务个数

    @Override
    public int bufferSize() {
        return this.bufferSize;
    }

    @Override
    public int maxBufferSize() {
        return this.maxBufferSize;
    }
}
