package org.qh.DDBMS.LDBMS.sql.impl;

import org.qh.DDBMS.LDBMS.sql.Commit;
import org.qh.DDBMS.LDBMS.sql.Reader;
import org.qh.DDBMS.LDBMS.sql.Writer;
import org.qh.DDBMS.LDBMS.sql.SQL;
import org.qh.DDBMS.LDBMS.sql.SQLExecutor;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.List;


/**
 *
 * @Author: qihe
 * @Date: 2024/10/26
 * @Version: 0.0.0
 * @Description: SQLExecutor的实现类
 */
public class SQLExecutorImpl implements SQLExecutor {

    /**
     * <pre>
     * 说明：用于执行读取数据库的操作的对象
     * </pre>
     */
    @Resource
    private Reader reader;

    /**
     * <pre>
     * 说明：用于执行修改数据库操作的对象
     * </pre>
     */
    @Resource
    private Writer writer;

    /**
     * <pre>
     * 说明：用于提交对数据库进行修改的对象
     * </pre>
     */
    @Resource
    private Commit commit;

    /**
     * <pre>
     * 说明：该方法用于查询只有一行返回数据的情况
     * 实现步骤：调用reader的readOne()
     * </pre>
     *
     * @param sql 要执行的SQL
     * @param clazz 返回数据的类型
     * @return 查询结果的单一对象
     * @since 0.0.0
     */
    @Override
    public <F> F selectOne(SQL sql, Class<F> clazz) throws Exception {
        return reader.readOne(sql, clazz);
    }

    /**
     * <pre>
     * 说明：该方法用于查询存在多行数据返回的情况
     * </pre>
     *
     * @param sql 要执行的SQL
     * @param clazz 返回数据的类型
     * @return 查询结果的对象列表
     * @since 0.0.0
     */
    @Override
    public <F> List<F> select(SQL sql, Class<F> clazz) throws Exception {
        return reader.read(sql, clazz);
    }

    /**
     * <pre>
     * 说明：执行更新表的SQL
     * 实现步骤：调用writer的write()
     * </pre>
     *
     * @param sql 要执行的更新SQL
     * @since 0.0.0
     */
    @Override
    public void write(SQL sql) throws SQLException {
        writer.write(sql);
    }

    /**
     * <pre>
     * 说明：
     *   1) 提交SQL中事务对数据库的更新
     *   2) 如果SQL中带有语句也一并执行。
     * 实现步骤：调用commit的commit方法
     * </pre>
     *
     * @param sql 要提交的SQL
     * @since 0.0.0
     */
    @Override
    public void commit(SQL sql) throws Exception {
        commit.commit(sql);
    }
}

