package org.qh.DDBMS.LDBMS.sql;

import java.sql.SQLException;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/26
 * @Version: 0.0.0
 * @Description: 该接口用于执行对数据库进行修改的SQL语句
 */
public interface Writer {

    /**
     * <pre>
     * 说明：执行写SQL
     * </pre>
     * @param sql 要执行的SQL对象
     * @since 0.0.0
     */
    void write(SQL sql) throws SQLException;
}
