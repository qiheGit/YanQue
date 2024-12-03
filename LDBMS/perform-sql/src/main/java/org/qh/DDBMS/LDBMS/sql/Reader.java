package org.qh.DDBMS.LDBMS.sql;


import java.sql.SQLException;
import java.util.List;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/26
 * @Version: 0.0.0
 * @Description: 该类是Reader的接口，提供执行从数据库读取数据的SQL
 */
public interface Reader {

    /**
     * <pre>
     * 说明：该方法执行SQL中指定的语句，从数据库中读取一行数据并返回。
     * </pre>
     * @param sql 要执行的SQL对象
     * @param <F> 查询结果的泛型类型
     * @return 查询结果的单个对象
     * @since 0.0.0
     */
    <F> F readOne(SQL sql, Class<F> clazz) throws Exception;

    /**
     * <pre>
     * 说明：该方法执行SQL中指定的语句，从数据库中读取多行数据并返回。
     * </pre>
     * @param sql 要执行的SQL对象
     * @param <F> 查询结果的泛型类型
     * @return 查询结果的列表
     * @since 0.0.0
     */
    <F> List<F> read(SQL sql, Class<F> clazz) throws Exception;
}
