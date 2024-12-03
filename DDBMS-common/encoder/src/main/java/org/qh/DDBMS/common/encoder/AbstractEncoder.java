package org.qh.DDBMS.common.encoder;


import com.qh.exception.MethodParameterException;
import org.qh.DDBMS.common.URI;
import org.qh.DDBMS.common.Validator;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/18
 * @Version:
 * @Description: 该类是一个抽象的Encoder接口的实现类。
 */
public abstract class AbstractEncoder implements Encoder, Validator<EncodeEntity, EncodeEntity> {
    /**
     * <pre>
     * 说明：该属性中保存了算法和对应加密方法的映射关系。
     */
    private Map<String, Method> algorithmMap = new HashMap<>();

    public AbstractEncoder () {
        initMap();
    }
    /**
     * <pre>
     * 说明：该方法用于初始化algorithmMap。
     * 实现步骤：
     *   1) 通过反射获取当前实例的Class对象。
     *   2) 获取所有父类对象，直到AbstractEncoder.class为止。
     *   3) 获取被@URI标记的方法并保存至algorithmMap。
     */
    private void initMap() {
        Class<?> leafClz = this.getClass();
        while (leafClz != null && leafClz != AbstractEncoder.class) {
            Method[] methods = leafClz.getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(URI.class)) {
                    URI uriAnnotation = method.getAnnotation(URI.class);
                    algorithmMap.put(uriAnnotation.value(), method);
                }
            }
            leafClz = leafClz.getSuperclass();
        }
    }

    /**
     * <pre>
     * 说明：验证传入参数是否合法。
     * 实现步骤：
     *   1) 验证entity.algorithm是否存在于algorithmMap的keySet中。
     *      1. 非法：抛出IllegalArgumentException。
     * @param entity 需要验证的EncodeEntity
     * @return 验证后的EncodeEntity
     * @throws IllegalArgumentException 如果验证失败
     */
    public EncodeEntity validate(EncodeEntity entity) {
        if (!algorithmMap.containsKey(entity.getAlgorithm())) {
            throw new MethodParameterException("Invalid algorithm: " + entity.getAlgorithm());
        }
        return entity;
    }

    /**
     * <pre>
     * 说明：当前方法是真正执行加密的方法。
     * 实现步骤：
     *   1) 根据entity.algorithm获取对应的加密方法。
     *   2) 执行对应的加密方法，返回加密后的数据。
     * @param entity 需要加密的EncodeEntity
     * @return byte[] 加密后的数据
     * @throws Exception 如果加密过程中发生错误
     */
    private byte[] doEncode(EncodeEntity entity) throws Exception {
        Method method = algorithmMap.get(entity.getAlgorithm());
        return (byte[]) method.invoke(this, entity);
    }

    /**
     * <pre>
     * 说明：对数据进行加密
     * 实现步骤：
     *   1) 校验参数，不合法直接抛出异常
     *   2) 调用doEncode()
     * @param entity 需要加密的数据实体
     * @return
     * @throws Exception
     */
    @Override
    public byte[] encode(EncodeEntity entity) throws Exception {
        EncodeEntity validate = validate(entity);
        if (validate != null) return doEncode(validate);
        return null;
    }
}
