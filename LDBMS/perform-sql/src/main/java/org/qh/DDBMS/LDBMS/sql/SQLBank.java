package org.qh.DDBMS.LDBMS.sql;

import org.qh.DDBMS.common.db.DBTransaction;

import java.sql.SQLException;
import java.util.List;


/**
 *
 * @Author: qihe
 * @Date: 2024/10/26
 * @Version: 0.0.0
 * @Description: sql仓库接口
 */

public interface SQLBank {

    /**
     * <pre>
     * 说明：存入一个sql
     * </pre>
     * @param sql 要存入的SQL对象
     * @since 0.0.0
     */
    void put(SQL sql) throws SQLException;

    /**
     * <pre>
     * 说明：将list中的sql都存入
     * 实现步骤：
     * 1) 循环执行put()
     * </pre>
     * @param sqlList 要存入的SQL对象列表
     * @since 0.0.0
     */
    void putAll(List<SQL> sqlList) throws SQLException;

    /**
     * <pre>
     * 说明：获取一个事务存入的所有sql
     * </pre>
     * @param transaction 事务对象
     * @return 该事务存入的所有SQL的列表
     * @since 0.0.0
     */
    List<SQL> get(DBTransaction transaction) throws SQLException;

    /**
     * <pre>
     * 说明：删除一个事务存入的所有SQL，并返回
     * </pre>
     * @param transaction 要删除SQL的事务对象
     * @since 0.0.0
     */
    void delete(DBTransaction transaction) throws SQLException;
}

