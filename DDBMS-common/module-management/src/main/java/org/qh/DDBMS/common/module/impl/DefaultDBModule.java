package org.qh.DDBMS.common.module.impl;

import org.qh.DDBMS.common.Validator;
import org.qh.DDBMS.common.module.DBModule;
import org.springframework.context.ApplicationContext;

import java.util.Objects;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/22
 * @Version: 0.0.0
 * @Description: 该类是DBModule接口的默认实现类
 */

public class DefaultDBModule implements DBModule, Validator<DefaultDBModule, String> {

    private Class<?> main; // 模块的启动类
    private ApplicationContext ioc; // 模块的spring容器

    /**
     * <pre>
     * 说明：此方法是当前类的全参构造器
     * 实现步骤：
     *   1) 为所有的属性赋值
     *   2) 将当前实例作为参数调用validate()
     *      1. 判定validate()返回非null，则抛出字段异常
     * </pre>
     * @param main 模块的启动类
     * @param ioc  模块的spring容器
     * @throws IllegalArgumentException 如果验证失败
     * @since 0.0.0
     */
    public DefaultDBModule(Class<?> main, ApplicationContext ioc) {
        this.main = main;
        this.ioc = ioc;
        String validationError = validate(this);
        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }
    }

    /**
     * <pre>
     * 说明：该方法用于检查传入参数是否符合规范
     * 实现步骤：
     *   1) 判定main为null，则返回
     *      "The main field is null."
     *   2) 判定ioc为null，则返回
     *      "The ioc field is null."
     * </pre>
     * @param module 要验证的对象
     * @return 错误信息，如果合法则返回null
     * @since 0.0.0
     */
    @Override
    public String validate(DefaultDBModule module) {
        if (module.main == null) {
            return "The main field is null.";
        }
        if (module.ioc == null) {
            return "The ioc field is null.";
        }
        return null; // 合法
    }

    @Override
    public Class<?> main() {
        return this.main;
    }

    @Override
    public ApplicationContext ioc() {
        return this.ioc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultDBModule that = (DefaultDBModule) o;
        return Objects.equals(main.getName(), that.main.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(main.getName());
    }
}
