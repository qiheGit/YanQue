package org.qh.DDBMS.common.db;

import com.qh.exception.ClassFieldException;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/18
 * @Version: 0.0.0
 * @Description: 该类实例表示一个数据库资源
 */
@Getter
public class DBResource implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     *<pre>
     * 说明：表示一个表的id
     */
    private Integer tableId;

    /**
     *<pre>
     * 说明：表示表中行数据的id
     */
    private Long rowId;

    /**
     * <pre>
     * 说明：全参数构造器。
     * 实现步骤：
     *   1) 为属性赋值。
     *   2) 将当前实例作为参数调用validate方法：
     *      1. 如果validate方法返回非null，则抛出ClassFieldException。
     *      2. 返回值作为异常信息。
     * </pre>
     * @param tableId 表的id
     * @param rowId 表中行数据的id
     * @throws ClassFieldException 如果字段验证失败
     * @since 0.0.0
     */
    public DBResource(Integer tableId, Long rowId) {
        this.tableId = tableId;
        this.rowId = rowId;
        String validationMessage = validate(this);
        if (validationMessage != null) {
            throw new ClassFieldException(validationMessage);
        }
    }

    public boolean isRow() {
        return rowId > 0;
    }

    public boolean isTable() {
        return rowId == 0;
    }

    /**
     * <pre>
     * 说明：该方法验证当前资源是否有效。
     * 实现步骤：
     *   1) 如果tableId小于等于0或tableId为null，则返回。
     *   2) 如果rowId小于等于0，则返回
     * </pre>
     * @param resource 需要验证的资源
     * @return String 如果无效，返回错误信息；如果有效，返回null
     * @since 0.0.0
     */
    public String validate(DBResource resource) {
        if (resource.tableId == null || resource.tableId <= 0) {
            return "The field tableId is " + resource.tableId + "(invalid)";
        }
        if (resource.rowId == null || resource.rowId < 0) {
            return "The field rowId is " + resource.rowId + "(invalid)";
        }
        return null;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DBResource that = (DBResource) o;
        return Objects.equals(tableId, that.tableId) && Objects.equals(rowId, that.rowId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableId, rowId);
    }
}

