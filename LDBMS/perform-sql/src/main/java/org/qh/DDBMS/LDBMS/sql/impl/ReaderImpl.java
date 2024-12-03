package org.qh.DDBMS.LDBMS.sql.impl;

import com.qh.exception.MethodParameterException;
import jdk.nashorn.internal.objects.NativeUint8Array;
import org.qh.DDBMS.LDBMS.sql.Reader;
import org.qh.DDBMS.LDBMS.sql.SQL;
import org.qh.DDBMS.common.Constant;
import org.qh.DDBMS.common.Validator;


import javax.annotation.Resource;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.*;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/26
 * @Version: 0.0.0
 * @Description: Reader的实现类
 */
public class ReaderImpl implements Reader, Validator<SQL, SQL> {

    /**
     * <pre>
     * 说明：获取数据库连接的数据源
     * </pre>
     */
    @Resource
    private DataSource dataSource;

    /**
     * <pre>
     * 说明：该方法检查传入的SQL是否合规
     * 实现步骤：
     *   1) 判定SQL.type不是0则返回null
     *   2) 返回SQL
     * </pre>
     * @param sql 要检查的SQL实例
     * @return 校验后的SQL实例或null
     * @since 0.0.0
     */
    @Override
    public SQL validate(SQL sql) {
        if (sql.type() != Constant.SQL.READ_TYPE) return null;
        return sql;
    }

    /**
     * <pre>
     * 说明：该方法真正将SQL语句交给数据库进行执行。
     * 实现步骤：
     *   1) 执行PrepareStatement的query方法
     *   2) 返回ResultType
     * </pre>
     *
     * @param preparedStatement 预编译的SQL语句
     * @param sql 要执行的SQL实例
     * @return 执行结果的ResultType实例
     * @since 0.0.0
     */
    protected ResultSet doQuery(PreparedStatement preparedStatement, SQL sql) throws SQLException {
        ResultSet result = preparedStatement.executeQuery();
        return result;
    }

    /**
     * <pre>
     * 说明：根据查询结果和返回值类型，返回Java bean形式的数据
     * 实现步骤：
     *   1) 遍历resultSet
     *      1. 构建F的实例
     *      2. F是一个Map
     *         - 取出一行数据
     *         - 将列名作为Key，将当前行该列值作为值，存入Map中
     *      3. F是一个Java Bean
     *         - 遍历F的属性
     *           > 判定该属性有被@Field注解，则为该属性赋值
     *      4. 将F实例放入list中
     *   2) 返回list
     * </pre>
     *
     * @param resultSet 查询结果集
     * @param clazz 返回值类型
     * @return Java bean形式的数据列表
     * @since 0.0.0
     */
    private <F> List<F> toBean(ResultSet resultSet, Class<F> clazz) throws SQLException {
        List<F> list = new ArrayList<>();
        F instance = null;
        Map<String, String> fieldMap = null;
        if (!Map.class.isAssignableFrom(clazz)) fieldMap = getFieldMap(clazz);
        try {
            while (resultSet.next()) {
                instance = null;
                if (Map.class.isAssignableFrom(clazz)) {
                    instance = clazz.cast(resultSetToMap(resultSet));
                } else instance = resultSetToBean(resultSet, clazz, fieldMap);
                list.add(instance);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (resultSet != null) resultSet.close();
        }
        return list;
    }

    /**
     * <pre>
     * 说明：根据class类型获取该实例字段和数据表字段的映射关系
     * 实现步骤：
     *   1. 构建一个Map， res
     *   2. 遍历cls所有的字段
     *     2.1. 该字段没有field注解修饰，则忽略
     *     2.2. 将该字段注解值作为key，字段名作为值传入res
     *   3. 返回res
     * </pre>
     * @param cls javaBean实例类型
     * @return cls中字段和数据库表字段的映射关系
     * @throws SQLException
     * @since 0.0.0
     */
    private Map<String, String> getFieldMap(Class<?> cls) throws SQLException {
        Map<String, String> res = new HashMap<>();
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(org.qh.DDBMS.LDBMS.sql.annotation.Field.class)) continue;
            String fieldV = field.getAnnotation(org.qh.DDBMS.LDBMS.sql.annotation.Field.class).value();
            res.put(fieldV, field.getName());
        }
        return res;
    }

    /**
     * <pre>
     * 说明：将一行数据封装为一个map
     * 实现步骤：
     *   1. 构建结果map
     *   2. 通过metaData获取当前结果集有多少个字段
     *   3. 循环取出各个字段
     *     3.1. 通过metaData得到字段名
     *     3.2. 通过resultSet得到字段值
     *     3.3. 将字段名和字段值装入map中
     *   4. 返回map实例
     * </pre>
     * @param resultSet 查询语句得到的结果集
     * @return 一行数据
     * @throws SQLException
     * @since 0.0.0
     */
    private HashMap<String, Object> resultSetToMap(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        HashMap<String, Object> result = new HashMap<>();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            Object value = resultSet.getObject(columnName);
            result.put(columnName, value);
        }
        return result;
    }

    /**
     * <pre>
     * 说明：将一行数据封装为一个JavaBean
     * 实现步骤：
     *   1. 构建bean实例
     *   2. 通过metaData获取当前行有多少个字段
     *   3. 循环地将所有字段封装入bean中
     *     3.1. 通过metaData获取表字段名
     *     3.2. 通过resultSet获取字段值
     *     3.3. 将字段值赋值给bean的属性实例
     *   4. 返回bean实例
     * </pre>
     * @param resultSet 查询语句返回的结果集
     * @param clazz     JavaBean实例类型
     * @param fieldMap  表字段和JavaBean字段的映射关系
     * @return 一个clazz实例
     * @throws Exception
     * @since 0.0.0
     */
    private <F> F resultSetToBean(ResultSet resultSet, Class<F> clazz,
                                  Map<String, String> fieldMap) throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        F f = clazz.getDeclaredConstructor().newInstance();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            Object value = resultSet.getObject(columnName);

            Field field = clazz.getDeclaredField(fieldMap.get(columnName));
            field.setAccessible(true);
            field.set(f, value);
        }
        return f;
    }

    /**
     * <pre>
     * 说明：该方法执行SQL中指定的语句，从数据库中读取多行数据并返回。
     * 实现步骤：
     *   1) 执行validate()，返回Null则抛出异常
     *   2) 获取数据库连接
     *   3) 获取PrepareStatement实例
     *   4) 执行doQuery(),如有异常则抛出
     *   5) 执行toBean()
     *   6) 返回结果
     *   7) finally，
     *      1. 关闭ResultType
     *      2. 关闭PrepareStatement
     *      3. 关闭连接
     * </pre>
     *
     * @param sql 要执行的SQL实例
     * @param clazz 返回值类型
     * @return 多行数据的Java bean列表
     * @since 0.0.0
     */
    public <F> List<F> read(SQL sql, Class<F> clazz) {
        if (validate(sql) == null) {
            throw new MethodParameterException("The sql's type is " + sql.type() + " (invalid)");
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql.statement());
             ResultSet resultSet = doQuery(preparedStatement, sql);) {

            return toBean(resultSet, clazz);
        } catch (SQLException e) {
            throw new RuntimeException("数据库查询失败", e);
        }
    }

    /**
     * <pre>
     * 说明：该方法执行SQL中指定的语句，从数据库中读取一行数据并返回。
     * 实现步骤：
     *   1) 执行read()，得到list
     *   2) 判定list中存在一个以上元素，则抛出异常
     *   3) 判定list中没有元素，则返回null
     *   4) 返回list中的一个元素
     * </pre>
     *
     * @param sql 要执行的SQL实例
     * @param clazz 返回值类型
     * @return 一行数据的Java bean实例或null
     * @since 0.0.0
     */
    public <F> F readOne(SQL sql, Class<F> clazz) {
        List<F> list = read(sql, clazz);
        if (list.size() > 1) {
            throw new IllegalStateException("The rows is " + list.size() + " but only one is returned");
        }
        return list.isEmpty() ? null : list.get(0);
    }
}
