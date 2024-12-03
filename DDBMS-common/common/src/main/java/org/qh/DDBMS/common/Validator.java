package org.qh.DDBMS.common;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/18
 * @Version: 0.0.0
 * @Description: 该接口用于自检，实现该接口的类的实例，都可以进行自检。
 * @Genericity
 *   Q: 需要被校验的实例/辅助校验的实例
 *   F: 返回值
 */
public interface Validator<Q,F> {
    /**
     * @param q 需要被校验的实例/辅助校验的实例
     * @return 校验结果
     * @since 0.0.0
     */
    public F validate(Q q);
}
