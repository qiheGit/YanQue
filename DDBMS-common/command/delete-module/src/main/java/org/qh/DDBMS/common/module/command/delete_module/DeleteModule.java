package org.qh.DDBMS.common.module.command.delete_module;

import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.qh.DDBMS.common.module.DBModule;
import org.qh.DDBMS.common.module.impl.DefaultDBModule;
import org.qh.command.SyncCommand;
import org.qh.command.completer.CommandCompleter;
import org.qh.sys.MethodMapper;
import org.qh.sys.app.Application;
import org.qh.sys.app.impl.ApplicationManagementImpl;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/24
 * @Version: 0.0.0
 * @Description: 该类是一个接受用户输入，删除用户数据库模块的命令行程序中的业务类。
 */
public class DeleteModule extends SyncCommand<Long, Boolean> {
    // 用户模块管理提供注册用户模块的API的签名
    private static String DELETE_API = "org.qh.DDBMS.common.module.DBModuleManager.deleteModule(org.qh.DDBMS.common.module.DBModule)";
    // 控制程序执行命令的API接口标签
    private static String IOC = "getIOC"; // 获取IOC的方法名
    // 控制程序执行命令的API接口标签
    private static String CMD_SERVICE_API = "org.qh.command.CMDService.exec(java.lang.String)";
    private static String KILL_CMD = "kill"; // kill命令名
    public DeleteModule() {
        super("ddm");
    }

    /**
     * 说明：当前命令的补全器
     * 实现步骤：
     *   1) 第一个参数是当前命令的key
     *   2) 第二个参数是一个Java程序的aid
     * @since 0.0.0
     */
    @Override
    protected void initCompleter() {
        ArgumentCompleter completer = new ArgumentCompleter(
                new StringsCompleter(key()),
                new StringsCompleter("aid"),
                new NullCompleter());
        completer(new CommandCompleter(key(), completer));
    }

    /**
     * <pre>
     * 说明：在执行前验证传入参数，是否合法
     * 实现步骤：
     *   1) 判定入参为空，返回null
     *   2) 判定第一个参数不是一个Java程序的aid，返回null
     *   3) 返回入参
     * </pre>
     * @param args 输入参数
     * @return 验证后的参数
     * @since 0.0.0
     */
    public Long beforeInvoke(String[] args) {
        // 实现逻辑
        if (args == null || args.length == 0) {
            return null;
        }
        if (!isValidJavaProgramId(args[0])) {
            return null;
        }
        return Long.parseLong(args[0]);
    }

    /**
     * <pre>
     * 说明：执行注册用户模块的业务
     * 实现步骤：
     *   1) 获取模块的启动类和Spring容器，构建DBModule。
     *   2) 调用Module Manager模块的删除用户模块的API，真正地删除用户模块。
     *   3) 调用控制程序的API销毁模块对应的Java程序。
     * </pre>
     * @param aid 用户模块的aid
     * @return 操作是否成功
     * @since 0.0.0
     */
    public Boolean invoke0(Long aid) {
        try {
            DBModule dbModule = createDBModule(aid);
            MethodMapper.invoke(DELETE_API, dbModule);
            MethodMapper.invoke(CMD_SERVICE_API, killArgs(aid.toString()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    /**
     * <pre>
     * 说明：生产出执行删除旧模块对应程序的命令行参数
     * </pre>
     * @param oldModuleAid 旧模块二点applicationId
     * @return 用于执行删除旧模块对应程序的命令行参数
     * @since 0.0.0
     */
    private String killArgs(String oldModuleAid) {
        return KILL_CMD + " " + oldModuleAid;
    }
    /**
     * <pre>
     * 说明：根据applicationId 构建一个DBModule
     * 实现步骤：
     *   1) 根据aid获取app实例对象
     *   2) 获取到app的启动类
     *   3) 获取到app的spring容器
     *   4) 构建一个DBModule并返回
     * </pre>
     * @param aid applicationId，一个Java程序唯一对应一个aid
     * @return 用户编写额数据库模块
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @since 0.0.0
     */
    private DBModule createDBModule(Long aid) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Application application = ApplicationManagementImpl.getInstance().application(aid);
        Class<? extends Application> main = application.getClass();
        Method method = main.getMethod(IOC);
        return new DefaultDBModule(main, (ApplicationContext) method.invoke(main));
    }

    /**
     * <pre>
     * 说明：判断给定的程序Id是否为有效的Java程序aid
     * </pre>
     * @param programId 程序Id
     * @return 是否为有效的Java程序aid
     * @since 0.0.0
     */
    private boolean isValidJavaProgramId(String programId) {
        return ApplicationManagementImpl.getInstance()
                .application(Long.valueOf(programId)) != null;
    }

}
