package org.qh.DDBMS.LDBMS.tx.impl;

import lombok.Data;
import org.qh.DDBMS.LDBMS.tx.LTransactionConfig;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/25
 * @Version: 0.0.0
 * @Description: LTransactionConfig接口的默认实现类
 */
@Data
public class DefaultLTransactionConfig implements LTransactionConfig {
    private int transactionType;

    @Override
    public int transactionType() {
        return transactionType;
    }
}
