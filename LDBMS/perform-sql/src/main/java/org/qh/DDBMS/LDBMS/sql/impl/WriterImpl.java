package org.qh.DDBMS.LDBMS.sql.impl;

import com.qh.exception.MethodParameterException;
import org.qh.DDBMS.LDBMS.sql.SQL;
import org.qh.DDBMS.LDBMS.sql.SQLBank;
import org.qh.DDBMS.LDBMS.sql.Writer;
import org.qh.DDBMS.common.Constant;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/26
 * @Version: 0.0.0
 * @Description: Writer的实现类
 */
public class WriterImpl implements Writer {

    /**
     * <pre>
     * 说明：更新数据库的sql库
     * </pre>
     */
    @Resource
    private SQLBank bank;

    /**
     * <pre>
     * 说明：检查传入的SQL是否合规
     * 实现步骤：
     *   1) 判定SQL.type不是1则返回false
     * </pre>
     *
     * @param sql 要检查的SQL实例
     * @return 如果SQL合规则返回true， 否则返回false
     * @since 0.0.0
     */
    private boolean checkSql(SQL sql) {
        return sql.type() == Constant.SQL.WRITE_TYPE;
    }

    /**
     * <pre>
     * 说明：执行写SQL
     * 实现步骤：
     *   1) 执行checkSQL(), 返回false，则抛出异常
     *   2) 将该sql保存到bank中
     * </pre>
     *
     * @param sql 要写入的SQL实例
     * @throws IllegalArgumentException 如果SQL不合规
     * @since 0.0.0
     */
    @Override
    public void write(SQL sql) throws SQLException {
        // 1) 执行checkSQL(), 返回false，则抛出异常
        if (!checkSql(sql)) {
            throw new MethodParameterException("The sql is not valid");
        }
        bank.put(sql);
    }
}

