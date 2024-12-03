package org.qh.DDBMS.common.decoder;

import com.qh.exception.MethodParameterException;
import org.qh.DDBMS.common.URI;
import org.qh.DDBMS.common.Validator;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/18
 * @Version: 0.0.0
 * @Description: 该类是一个抽象的Decode接口的实现类
 */

public abstract class AbstractDecoder implements Decoder,
        Validator<DecoderEntity, DecoderEntity> {

    /**
     * <pre>
     * 说明：该属性中保存了算法和对应解密方法的映射关系。
     * </pre>
     */
    protected Map<String, Method> algorithmMap;

    public AbstractDecoder() {
        initMap();
    }

    /**
     * <pre>
     * 说明：该方法用于初始化algorithmMap
     * 规范：
     *   1) 如果，子类中有方法和父类中的URI一致，则子类的
     *   方法会被保存到algorithmMap中。
     * 实现步骤：
     *   1) 通过反射获取当前实例的Class对象，leafClz
     *   2) 通过leafClz获取所有的父类对象，直到AbstractDecode.class为止
     *   3) 得到子类中所有被@URI标记的方法。
     *   4) 将其URI和方法的映射关系保存至algorithmMap
     *   属性中。
     * @since 0.0.0
     */
    private void initMap() {
        List<Method> methods = getAllMethods().stream()
                .filter(m -> m.isAnnotationPresent(URI.class))
                .collect(Collectors.toList());
        algorithmMap = new HashMap<>();
        for (Method method : methods) {
            algorithmMap.put(method.getAnnotation(URI.class).value(),
                    method);
        }
    }

    /**
     * <pre>
     * 说明：获取从子类到本类所有声明的方法
     * 实现步骤：
     *   1. 获取子类class对象clz
     *   2. 循环地得到父类class对象，直到当前类为止
     *     2.1. 将clz所有声明的方法加入到集合中
     *   3. 返回集合实例
     * </pre>
     * @return
     * @since
     */
    private List<Method> getAllMethods() {
        Class clz = getClass();
        List<Method> methods = new ArrayList<>();
        while (clz.equals(AbstractDecoder.class)) {
            methods.addAll(Arrays.asList(clz.getDeclaredMethods()));
            clz = clz.getSuperclass();
        }
        return methods;
    }

    /**
     * <pre>
     * 说明：验证传入参数是否和合法
     * 实现步骤：
     *   1) 验证entity.algorithm是否存在于algorithmMap的keySet中。
     *     1. 不存在则报错
     * @param entity 需要验证的DecodeEntity
     * @return 异常信息
     * @since 0.0.0
     */
    @Override
    public DecoderEntity validate(DecoderEntity entity) {
        if (algorithmMap.containsKey(entity.getAlgorithm())) {
            return entity;
        } else return null;
    }

    /**
     * <pre>
     * 说明：当前方法是真正执行解密方法。
     * 实现步骤：
     *   1) 根据entity.algorithm获取对应的解密方法。
     *   2) 执行对应的解密方法，返回解密后的数据。
     * @param entity 解密所需的DecodeEntity
     * @return 解密后的字节数组
     * @since 0.0.0
     */
    private byte[] doDecode(DecoderEntity entity) {
        Method method = algorithmMap.get(entity.getAlgorithm());
        try {
            return (byte[]) method.invoke(this, entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * <pre>
     * 说明：根据传入的DecodeEntity返回解密后的数据
     * 实现步骤：
     *   1) 验证参数合法性，不合法则抛出异常
     *   2) 执行doDecode()
     * @param entity 解码参数容器实例
     * @return 解密后的字节数组
     * @since 0.0.0
     */
    @Override
    public byte[] decode(DecoderEntity entity) {
        DecoderEntity validEntity = validate(entity);
        if (validEntity == null) {
            throw new MethodParameterException("The algorithm " + entity.getAlgorithm() + " does not exist");
        }
        return doDecode(validEntity);
    }
}

















