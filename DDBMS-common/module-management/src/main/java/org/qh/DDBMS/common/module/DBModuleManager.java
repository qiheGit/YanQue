package org.qh.DDBMS.common.module;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/22
 * @Version: 0.0.0
 * @Description: 该接口是一个用户定义的数据库模块的管理接口，用于维护用户定义的模块
 */
public interface DBModuleManager {

    /**
     * 说明：该方法用于注册一个用户定义的模块
     * @param module 要注册的数据库模块
     * @since 0.0.0
     */
    void registerModule(DBModule module) throws Exception;

    /**
     * 说明：该方法用于删除一个用户已经注册的模块
     * @param module 要删除的数据库模块
     * @since 0.0.0
     */
    void deleteModule(DBModule module) throws Exception;

    /**
     * 说明：该方法用于更新一个用户已经注册的模块
     * @param oldModule 旧的数据库模块
     * @param newModule 新的数据库模块
     * @since 0.0.0
     */
    void update(DBModule oldModule, DBModule newModule) throws Exception;

    /**
     * 说明：该方法获取一个URI对应的操作数据的方法
     * @param uri 数据库操作的URI
     * @return 对应的DBMethod
     * @since 0.0.0
     */
    DBMethod getMethod(String uri);
}

