package org.qh.DDBMS.LDBMS.sql.impl;

import com.qh.exception.ClassFieldException;
import org.qh.DDBMS.LDBMS.sql.SQL;
import org.qh.DDBMS.common.Constant;
import org.qh.DDBMS.common.Validator;
import org.qh.DDBMS.common.db.DBTransaction;
import org.qh.tools.str.StringUtils;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/26
 * @Version: 0.0.0
 * @Description: 该类是SQL接口的默认实现类
 */
public class SQLImpl implements SQL, Validator<SQLImpl, String> {

    /**
     * <pre>
     * 说明：是一个操作数据库事务对象
     * </pre>
     */
    private DBTransaction transaction;

    /**
     * <pre>
     * 说明：当前SQL类型
     * </pre>
     */
    private byte type;

    /**
     * <pre>
     * 说明：
     *   1) 具体的sql语句
     *   2) sql中不应当存在占位符
     * </pre>
     */
    private String statement;

    /**
     * <pre>
     * 说明：该类的全参构造器
     * 实现步骤：
     *   1) 为属性赋值
     *   2) 将当前实例作为参数执行validate()
     *      1. validate()返回非null，抛出字段异常
     * </pre>
     *
     * @param transaction 数据库事务对象
     * @param type 当前事务类型
     * @param statement 具体的SQL语句
     * @since 0.0.0
     */
    public SQLImpl(DBTransaction transaction, byte type, String statement) {
        this.transaction = transaction;
        this.type = type;
        this.statement = statement;

        String validationError = validate(this);
        if (validationError != null) {
            throw new ClassFieldException(validationError);
        }
    }

    /**
     * <pre>
     * 说明：该方法用于检验参数是否符合规范
     * 实现步骤：
     *   1) 判定transaction是 null，则返回"The transaction field is null."
     *   2) 判定type不是0也不是1，则返回"The type field is value(invalid)."
     *   3) 判定statement存在且最后一个字符不是";",则返回"The statement field is not complete."
     *   4) 判定statement中存在SQL_STATEMENT_SEPARATOR则返回
     *   "The statement field contains SQL_STATEMENT_SEPARATOR."
     * </pre>
     *
     * @param sqlImpl 要检验的SQLImpl实例
     * @return 校验结果，返回错误信息或null
     * @since 0.0.0
     */
    @Override
    public String validate(SQLImpl sqlImpl) {
        if (sqlImpl.transaction == null) {
            return "The transaction field is null.";
        }
        if (sqlImpl.type != Constant.SQL.READ_TYPE && sqlImpl.type != Constant.SQL.WRITE_TYPE) {
            return "The type field is " + sqlImpl.type + "(invalid).";
        }
        if (StringUtils.isEmpty(sqlImpl.statement)) return null;
        if (sqlImpl.statement.charAt(sqlImpl.statement.length() - 1) != ';') {
            return "The statement field is not complete.";
        }
        if (sqlImpl.statement.contains(Constant.Sync.SQL_STATEMENT_SEPARATOR)) {
            return "The statement field contains SQL_STATEMENT_SEPARATOR.";
        }
        return null;
    }

    @Override
    public DBTransaction transaction() {
        return this.transaction;
    }

    @Override
    public byte type() {
        return this.type;
    }

    @Override
    public String statement() {
        return this.statement;
    }
}

