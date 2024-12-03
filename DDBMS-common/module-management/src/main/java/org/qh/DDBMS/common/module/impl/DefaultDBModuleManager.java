package org.qh.DDBMS.common.module.impl;

import com.qh.exception.MethodParameterException;
import org.qh.DDBMS.common.URI;
import org.qh.DDBMS.common.module.DBMethod;
import org.qh.DDBMS.common.module.DBModule;
import org.qh.DDBMS.common.module.DBModuleManager;
import org.qh.sys.MethodMapper;
import org.qh.tools.exception.ExceptionUtils;
import org.qh.tools.thread.LockUtils;
import org.springframework.stereotype.Controller;

import javax.annotation.PreDestroy;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/22
 * @Version: 0.0.0
 * @Description: 该类是DBModuleManager的默认实现类
 */
public class DefaultDBModuleManager implements DBModuleManager {

    /**
     * 说明：该属性中保存了模块与其中提取出的DBMethod的URI之间的映射关系。
     */
    private Map<DBModule, Set<String>> moduleMethod;

    /**
     * 说明：该属性保存了URI和方法的映射关系
     */
    private Map<String, DBMethod> uriMethod;

    private List<String> apis;

    public DefaultDBModuleManager() {
        init();
        registerAPI();
    }

    private void init() {
        moduleMethod = new ConcurrentHashMap<>();
        uriMethod = new ConcurrentHashMap<>();
        apis = new ArrayList<>();
    }

    /**
     * <pre>
     * 说明：该方法用于注册一个用户定义的模块
     * 实现步骤：
     *   1) 获取模块的ioc
     *   2) 获取ioc中所有的Controller
     *   3) 遍历controllers
     *      1. 获取到当前遍历的controller的uri（可无）
     *      2. 获取当前controller的所有被URI注解的方法
     *      3. 遍历所有获取到的方法method
     *         - 将method封装为一个DBMethod对象
     *         - 将所有的DBMethod收集入一个List
     *   4) 将当前模块和List作为参数执行save方法
     * </pre>
     * @param module 要注册的数据库模块
     * @since 0.0.0
     */
    @Override
    public void registerModule(DBModule module) throws Exception {
        Collection<Object> values = module.ioc().getBeansWithAnnotation(Controller.class).values();
        String ctrlUri = null;
        List<DBMethod> methods = new ArrayList<>();
        for (Object ctrl : values) {
            if (ctrl.getClass().isAnnotationPresent(URI.class)) {
                ctrlUri = ctrl.getClass().getAnnotation(URI.class).value();
            } else ctrlUri = "";
            methods.addAll(resolveMethodInCtr(ctrlUri, ctrl));
        }
        save(module, methods);
    }

    /**
     * <pre>
     * 说明：解析一个controller中的操作数据库的方法
     * 实现步骤：
     *   1) 获取当前controller的所有被URI注解的方法
     *   2) 遍历所有获取到的方法method
     *     1. 将method封装为一个DBMethod对象
     *     2. 将所有的DBMethod收集入一个List
     * @param ctrlUri controller上的uri
     * @param ctrl controller实例
     * @return ctrl中所有被 {@link org.qh.DDBMS.common.URI}注解的方法
     * @since 0.0.0
     */
    private List<DBMethod> resolveMethodInCtr(String ctrlUri, Object ctrl) {
        List<DBMethod> res = new ArrayList<>();
        Method[] declaredMethods = ctrl.getClass().getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            if (!declaredMethod.isAnnotationPresent(URI.class)) continue;
            URI uri = declaredMethod.getAnnotation(URI.class);
            res.add(new DefaultDBMethod(ctrl, ctrlUri + uri.value(), declaredMethod));
        }
        return res;
    }

    /**
     * <pre>
     * 说明：该方法真正地保存用户注册的模块
     * 实现步骤：
     *   1) 对模块进行加锁
     *   2) 将所有uri和DBMethod的关系映射到uriMethod中
     *   3) 将模块和uri的关系映射到moduleMethod中
     *   4) 释放模块锁
     * </pre>
     * @param module 要保存的数据库模块
     * @param methods 要保存的方法列表
     * @since 0.0.0
     */
    private void save(DBModule module, List<DBMethod> methods) throws InterruptedException {
        Set<String> uri = new HashSet<>();
        try {
            lockModule(module);
            for (DBMethod method : methods) {
                DBMethod old = uriMethod.put(method.uri(), method);
                if (old != null) {
                    uriMethod.put(method.uri(), old);
                    for (String newUri : uri) uriMethod.remove(newUri);

                    throw new MethodParameterException("The '" + method.uri() + "'(uri) has already existed.");
                }
                uri.add(method.uri());
            }
            moduleMethod.put(module, Collections.unmodifiableSet(uri));
        } finally {
            unlockModule(module);
        }
    }

    /**
     * 说明：对一个模块进行封锁
     * @param module 模块
     * @throws InterruptedException 可能出现的异常
     * @since 0.0.0
     */
    private void lockModule(DBModule module) throws InterruptedException {
        String mainName = module.main().getName();
        LockUtils.waitLock(mainName);
    }

    /**
     * 说明：释放一个模块的锁
     * @param module 模块
     * @throws InterruptedException 可能出现的异常
     * @since 0.0.0
     */
    private void unlockModule(DBModule module) throws InterruptedException {
        String mainName = module.main().getName();
        LockUtils.unlock(mainName);
    }

    /**
     * <pre>
     * 说明：该方法用于删除一个用户已经注册的模块
     * 实现步骤：
     *   1) 获取模块所有DBMethod的uri
     *   2) 对模块进行加锁
     *   3) 将所有uri和DBMethod的关系从uriMethod中删除
     *   4) 将模块和DBMethods的映射关系从moduleMethod中删除
     *   5) 将释放锁
     * </pre>
     * @param module 要删除的数据库模块
     * @since 0.0.0
     */
    @Override
    public void deleteModule(DBModule module) throws Exception {
        try {
            lockModule(module);
            Set<String> remove = moduleMethod.remove(module);
            for (String uri : remove) uriMethod.remove(uri);
        } finally {
            unlockModule(module);
        }
    }

    /**
     * <pre>
     * 说明：该方法用于更新一个用户已经注册的模块
     * 实现步骤：
     *   1) 调用deleteModule()删除旧模块
     *   2) 调用registerModule()注册新模块
     * </pre>
     * @param oldModule 旧的数据库模块
     * @param newModule 新的数据库模块
     * @since 0.0.0
     */
    @Override
    public void update(DBModule oldModule, DBModule newModule) throws Exception {
        deleteModule(oldModule);
        registerModule(newModule);
    }

    /**
     * <pre>
     * 说明：该方法获取一个URI对应的操作数据的方法。
     * 实现步骤：
     *   1) 根据uri获取对应模块
     *   2) 对模块进行加锁
     *   3) 获取uri对应方法
     *   4) 释放模块锁
     *   5) 返回方法
     * </pre>
     * @param uri 要获取的URI
     * @return 对应的DBMethod
     * @since 0.0.0
     */
    @Override
    public DBMethod getMethod(String uri) {
        DBModule module = getModuleByURI(uri);
        DBMethod method = null;
        if (module == null) return null;
        try {
            lockModule(module);
            method = uriMethod.get(uri);
        } catch (Exception e) {
            ExceptionUtils.printStackTrace(e);
        } finally {
            try {
                unlockModule(module);
            } catch (InterruptedException e) {
                ExceptionUtils.printStackTrace(e);
            }
        }
        return method;
    }

    private DBModule getModuleByURI(String uri) {
        for (DBModule module : moduleMethod.keySet()) {
            if (moduleMethod.get(module).contains(uri)) return module;
        }
        return null;
    }

    /**
     * <pre>
     * 说明：该方法用于将当前接口的API注册到方法映射器中，
     * 这样其他的程序就能够调用该方法
     * 实现步骤：
     *   1) 调用MethodMapper的方法依次注册三个接口方法到MethodMapper中
     *   2) 依次输出方法签名
     * </pre>
     * @since 0.0.0
     */
    private void registerAPI() {
        Method[] apis = DBModuleManager.class.getDeclaredMethods();
        for (Method api : apis) {
            this.apis.add(MethodMapper.registerMethod(this, api));
        }
        for (String api : this.apis) {
            System.out.println("DBModuleManager api: " + api.toString());
        }
    }

    /**
     * <pre>
     * 说明：注销注册到MethodMapper中的API
     * 实现步骤：
     *   1) 调用MethodMapper的API依次注销三个API
     * </pre>
     * @since 0.0.0
     */
    private void unregisterAPI() {
        for (String api : apis) {
            MethodMapper.unregisterMethod(api);
        }
    }

    /**
     * <pre>
     * 说明：当该对象被销毁时执行此方法。
     * 实现步骤：
     *   1) 执行unregister方法
     * </pre>
     * @since 0.0.0
     */
    @PreDestroy
    public void destroy() {
        unregisterAPI();
    }
}
