package org.qh.DDBMS.LDBMS.sql;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/26
 * @Version: 0.0.0
 * @Description: 该接口提供提交事务对于数据库修改的API
 */
public interface Commit {

    /**
     * <pre>
     * 说明：提交SQL指定的事务
     * </pre>
     * @param sql 要提交的SQL对象
     * @since 0.0.0
     */
    void commit(SQL sql) throws Exception;
}

