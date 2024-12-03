package org.qh.DDBMS.common.module.impl;

import com.qh.exception.ClassFieldException;
import org.qh.DDBMS.common.Validator;
import org.qh.DDBMS.common.module.DBMethod;

import java.lang.reflect.Method;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/22
 * @Version: 0.0.0
 * @Description: 该类是DBMethod的默认实现类
 */
public class DefaultDBMethod implements DBMethod, Validator<DefaultDBMethod, String> {

    private Object obj; // 说明：method属性的执行者
    private String uri; // 说明：method属性对应的uri
    private Method method; // 说明：需要执行的数据库方法

    /**
     * <pre>
     * 说明：该方法是此类的全参构造器
     * 实现步骤：
     *   1) 为所有的属性赋值
     *   2) 将当前实例作为参数调用validate()
     *      1. 判定validate()返回非null，则抛出字段异常
     * </pre>
     * @param obj    method属性的执行者
     * @param uri    method属性对应的uri
     * @param method 需要执行的数据库方法
     * @throws ClassFieldException 如果验证失败
     * @since 0.0.0
     */
    public DefaultDBMethod(Object obj, String uri, Method method) {
        this.obj = obj;
        this.uri = uri;
        this.method = method;
        String validationError = validate(this);
        if (validationError != null) {
            throw new ClassFieldException(validationError);
        }
    }

    /**
     * <pre>
     * 说明：该方法用于检查传入参数是否符合规范
     * 实现步骤：
     *   1) 判定obj为null，则返回
     *      "The obj field is null."
     *   2) 判定uri为空，则返回
     *      "The uri field is empty."
     *   3) 判定method为null，则返回
     *      "The method field is null."
     * </pre>
     * @param defaultDBMethod 要验证的对象
     * @return 错误信息，如果合法则返回null
     * @since 0.0.0
     */
    @Override
    public String validate(DefaultDBMethod defaultDBMethod) {
        if (defaultDBMethod.obj == null) {
            return "The obj field is null.";
        }
        if (defaultDBMethod.uri == null || defaultDBMethod.uri.isEmpty()) {
            return "The uri field is empty.";
        }
        if (defaultDBMethod.method == null) {
            return "The method field is null.";
        }
        return null; // 合法
    }

    @Override
    public Object obj() {
        return this.obj;
    }

    @Override
    public String uri() {
        return this.uri;
    }

    @Override
    public Method method() {
        return this.method;
    }
}

