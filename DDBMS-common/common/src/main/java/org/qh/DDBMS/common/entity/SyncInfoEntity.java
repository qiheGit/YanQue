package org.qh.DDBMS.common.entity;

import com.qh.exception.ClassFieldException;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.qh.DDBMS.common.Validator;
import org.qh.DDBMS.common.db.DBResource;
import org.qh.DDBMS.common.db.DBTransaction;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/27
 * @Version: 0.0.0
 * @Description: 同步数据实例类
 */
@Data
@NoArgsConstructor
public class SyncInfoEntity implements Validator<SyncInfoEntity, String>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * <pre>
     * 说明：同步信息的id，也代表当前事务是数据库中提交的第几个事务
     * </pre>
     */
    private Long id;

    /**
     * <pre>
     * 说明：当前提交的事务
     * </pre>
     */
    private DBTransaction transaction;

    /**
     * <pre>
     * 说明：修改数据库的sql语句。
     * </pre>
     */
    private String sqlStatement;

    /**
     * <pre>
     * 说明：该类的全参构造器
     * 实现步骤：
     *   1) 为属性赋值
     *   2) 将当前实例作为参数执行validate()
     *      1. 返回非null，则抛出参数异常，同时返回值作为异常消息
     * </pre>
     *
     * @param transaction 最近一个事务
     * @param sqlStatement 修改数据库的SQL语句
     * @since 0.0.0
     */
    public SyncInfoEntity(Long id, DBTransaction transaction, String sqlStatement) {
        this.id = id;
        this.transaction = transaction;
        this.sqlStatement = sqlStatement;

        String validationMessage = validate(this);
        if (validationMessage != null) {
            throw new ClassFieldException(validationMessage);
        }
    }

    /**
     * <pre>
     * 说明：该方法用于验证参数是否合规
     * 实现步骤：
     *   1) 判定lastTransaction为null，返回
     *      "The lastTransaction filed is null"
     *   2) 判定currentTransaction是null，返回
     *      "The currentTransaction filed is null"
     *   3) 判定dbResource是空或null，返回
     *      "The resource filed is empty"
     *   4) 判定sqlStatement是空或null，返回
     *      "The sqlStatement filed is empty"
     * </pre>
     *
     * @param entity 当前实例
     * @return 错误信息或null
     * @since 0.0.0
     */
    @Override
    public String validate(SyncInfoEntity entity) {
        if (entity.transaction == null) {
            return "The lastTransaction field is null";
        }
        if (entity.sqlStatement == null || entity.sqlStatement.isEmpty()) {
            return "The sqlStatement field is empty";
        }
        return null;
    }

}

