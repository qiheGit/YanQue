package org.qh.DDBMS.LDBMS.tx;

import org.qh.DDBMS.common.db.DBTransaction;
import org.qh.DDBMS.common.tx.TransactionExec;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/25
 * @Version: 0.0.0
 * @Description: 局部事务中心接口
 */

public interface LTransactionCenter {

    /**
     * <pre>
     * 说明: 该方法用于创建一个事务
     * </pre>
     * @param name String 事务名称
     * @param exec TransactionExec 事务执行逻辑
     * @return DBTransaction 创建的事务对象
     * @since 0.0.0
     */
    DBTransaction createTransaction(String name, TransactionExec exec);

    /**
     * <pre>
     * 说明: 该方法用于注册一个事务提交接口实例
     * </pre>
     * @param transaction 等待提交的事务
     * @param doCommit 事务提交逻辑
     * @since 0.0.0
     */
    void registerCommit(DBTransaction transaction, DoCommit doCommit);

    /**
     * <pre>
     * 说明：该方法用于撤销一个事务
     * 规范；如果是因为用户代码调用该方法，请自行释放事务持有的资源
     *   1. 事务需要持有的表/行资源
     *   2. 事务已经缓存的修改数据库sql语句
     *   3. 其他
     * </pre>
     * @param transaction DBTransaction 需要撤销的事务
     * @param reason int 撤销原因代码
     * @since 0.0.0
     */
    void cancel(DBTransaction transaction, int reason);

    /**
     * <pre>
     * 说明：该方法由GDBMS进行调用，真正地提交事务
     * </pre>
     * @param transaction DBTransaction 需要提交的事务
     * @param syncInfoId Long 事务对应同步信息id
     * @since 0.0.0
     */
    void commit(DBTransaction transaction, Long syncInfoId);


    /**
     * <pre>
     * 说明：获取本站点提交事务数
     * </pre>
     * @return long 提交的事务数量
     * @since 0.0.0
     */
    long transactionCount();
}

