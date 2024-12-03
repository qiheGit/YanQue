package org.qh.DDBMS.LDBMS.sql;

import org.qh.DDBMS.common.db.DBTransaction;

import java.sql.SQLException;
import java.util.List;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/26
 * @Version: 0.0.0
 * @Description: 该接口用于执行传入的SQL对象描述的SQL语句
 */
public interface SQLExecutor {

    /**
     * <pre>
     * 说明：该方法用于查询只有一行返回数据的情况
     * </pre>
     * @param sql 要执行的SQL对象
     * @param <F> 查询结果的泛型类型
     * @return 查询结果的单个对象
     * @since 0.0.0
     */
    <F> F selectOne(SQL sql, Class<F> clazz) throws Exception;

    /**
     * <pre>
     * 说明：该方法用于查询存在多行数据返回的情况
     * </pre>
     * @param sql 要执行的SQL对象
     * @param <F> 查询结果的泛型类型
     * @return 查询结果的列表
     * @since 0.0.0
     */
    <F> List<F> select(SQL sql, Class<F> clazz) throws Exception;

    /**
     * <pre>
     * 说明：执行更新表的SQL
     * </pre>
     * @param sql 要执行的SQL对象
     * @since 0.0.0
     */
    void write(SQL sql) throws SQLException;

    /**
     * <pre>
     * 说明：
     *   1) 提交SQL中事务对数据库的更新
     *   2) 如果SQL中带有语句也一并执行
     * </pre>
     * @param sql 要提交的SQL对象
     * @since 0.0.0
     */
    void commit(SQL sql) throws Exception;


}

