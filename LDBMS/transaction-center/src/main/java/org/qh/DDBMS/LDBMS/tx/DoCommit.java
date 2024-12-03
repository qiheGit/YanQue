package org.qh.DDBMS.LDBMS.tx;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/25
 * @Version: 0.0.0
 * @Description: 用于提交事务的函数式接口
 */
@FunctionalInterface
public interface DoCommit {

    /**
     * <pre>
     * 说明：提交事务
     * </pre>
     * @param syncInfoId Long 同步信息id
     * @since 0.0.0
     */
    void commit(Long syncInfoId);
}

