package org.qh.DDBMS.LDBMS.sql;

import org.qh.DDBMS.common.db.DBTransaction;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/26
 * @Version: 0.0.0
 * @Description: 该接口描述了执行一次SQL需要提供的参数
 */
public interface SQL {

    /**
     * <pre>
     * 说明：该方法返回当前SQL属于哪个事务
     * </pre>
     * @return 当前SQL的事务实例
     * @since 0.0.0
     */
    DBTransaction transaction();

    /**
     * <pre>
     * 说明：该方法返回当前SQL的类型
     *   1) READ_TYPE表示查询语句：select
     *   2) WRITE_TYPE表示修改语句：insert，update，delete
     * </pre>
     * @return 当前SQL的类型，0表示查询语句，1表示修改语句
     * @since 0.0.0
     */
    byte type();

    /**
     * <pre>
     * 说明：该方法返回该SQL的执行语句
     * </pre>
     * @return 当前SQL的执行语句
     * @since 0.0.0
     */
    String statement();
}

