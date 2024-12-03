package org.qh.DDBMS.common.db;

import com.qh.exception.ClassFieldException;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/18
 * @Version: 0.0.0
 * @Description: 该类用来描述一个事务。
 * @Specification
 *   1. 只允许一个最老的线程等待
 *   2. 当全局事务ID和局部事务ID相等时，全局事务ID值小于局部事务ID值
 */
@Getter
@NoArgsConstructor
public class DBTransaction implements Comparable<DBTransaction> , Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * <pre>
     * 说明：当前事务id
     * </pre>
     */
    private Long id;

    /**
     * <pre>
     * 说明：当前事务类型，全局事务为0, 局部事务为处理局部事务数据库的编号
     * </pre>
     */
    private int type;

    /**
     * <pre>
     * 说明：该事务的优先级
     * </pre>
     */
    private byte order;

    /**
     * <pre>
     * 说明：该类的全参构造器。
     * 实现步骤：
     *   1) 属性赋值。
     *   2) 将当前对象作为参数，调用validate方法。
     *   3) validate方法返回非null，则将返回值作为异常信息抛出。
     * </pre>
     * @param id 事务id
     * @param type 事务类型
     * @param order 事务优先级
     * @throws IllegalArgumentException 如果验证失败
     * @since 0.0.0
     */
    public DBTransaction(Long id, int type, byte order) {
        this.id = id;
        this.type = type;
        this.order = order;
        String validationMessage = validate(this);
        if (validationMessage != null) {
            throw new ClassFieldException(validationMessage);
        }
    }

    /**
     * <pre>
     * 说明：当前事务和另一个事务进行比较，判断当前事务是否年老。
     * 实现步骤：
     *   1) 如果两个事务id一致：
     *      1. 当前事务type是1返回false。
     *      2. 当前事务type是0返回true。
     *   2) 如果两个事务id不一致：
     *      1. 当前事务id较小返回true。
     *      2. 当前事务id较大返回false。
     * </pre>
     * @param other 另一个事务
     * @return boolean 如果当前事务年老，返回true，否则返回false
     * @since 0.0.0
     */
    public boolean older(DBTransaction other) {
        if (this.id.equals(other.id)) return this.type == 0;
        else return this.id < other.id;
    }

    /**
     * <pre>
     * 说明：验证传入的事务是否有效。
     * 实现步骤：
     *   1) 如果id为null，返回"Invalid id"。
     *   2) 如果type既不等于0也不等于1，返回"Invalid type"。
     *   3) 否则返回null表示验证成功。
     * </pre>
     * @param transaction 需要验证的事务
     * @return String 如果无效，返回错误信息；如果有效，返回null
     * @since 0.0.0
     */
    public String validate(DBTransaction transaction) {
        if (transaction.id == null) {
            return "Invalid id";
        }
        if (transaction.type < 0) {
            return "Invalid type";
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DBTransaction that = (DBTransaction) o;
        return type == that.type && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type);
    }

    /**
     * <pre>
     * 说明：
     *   1. 当前实例年老则返回-1
     *   2. 当前实例年轻则返回1
     * </pre>
     * @param o the object to be compared.
     * @return 两个事务的比较结果
     * @since 0.0.0
     */
    @Override
    public int compareTo(DBTransaction o) {
        return equals(o) ? 0 : older(o) ? -1 : 1;
    }
}

