package org.qh.DDBMS.common.module.command.update_module;

import com.qh.exception.MethodParameterException;
import org.jline.builtins.Completers;
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
import org.qh.tools.file.FileUtils;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/24
 * @Version: 0.0.0
 * @Description: 该类是实现用户更新已经注册模块的命令行程序中的具体业务类。
 */
public class UpdateModule extends SyncCommand<String[], Boolean> {
    // 用户模块管理提供注册用户模块的API的签名
    private static String UPDATE_API = "org.qh.DDBMS.common.module.DBModuleManager.update(org.qh.DDBMS.common.module.DBModule,org.qh.DDBMS.common.module.DBModule)";
    // 控制程序执行命令的API接口标签
    private static String CMD_SERVICE_API = "org.qh.command.CMDService.exec(java.lang.String)";
    private static String IOC = "getIOC"; // 获取IOC的方法名

    private static String KILL_CMD = "kill"; // kill命令名
    private static String BOOT_CMD = "boot"; // boot命令名

    public UpdateModule() {
        super("udm");
    }

    /**
     * 说明：当前命令的补全器
     * 实现步骤：
     *   1) 第一个参数是当前命令的key
     *   2) 第二个参数是一个Java程序的aid(旧模块)
     *   3) 第三个参数是一个Java程序(新模块)
     * @since 0.0.0
     */
    @Override
    protected void initCompleter() {
        ArgumentCompleter completer = new ArgumentCompleter(
                new StringsCompleter(key()),
                new StringsCompleter("aid"),
                new Completers.FileNameCompleter(),
                new NullCompleter());
        completer(new CommandCompleter(key(), completer));
    }

    /**
     * <pre>
     * 说明：在执行前验证传入参数，是否合法
     * 实现步骤：
     *   1) 判定入参为空，返回null
     *   2) 判定第一个入参不是一个Java程序的Id，返回null
     *   3) 判定第二个入参不是一个Java程序，返回null
     *   4) 返回入参
     * </pre>
     * @param args 输入参数
     * @return 验证后的参数
     * @since 0.0.0
     */
    public String[] beforeInvoke(String[] args) {
        // 实现逻辑
        if (args == null || args.length < 2) {
            return null;
        }
        if (!isValidJavaProgramId(args[0])) {
            return null;
        }
        if (!isJavaProgram(args[1])) {
            return null;
        }
        return args;
    }

    /**
     * <pre>
     * 说明：更新用户注册的业务模块
     * 实现步骤：
     *   1) 获取旧模块的启动类和Spring容器，构建DBModule
     *   2) 调用控制程序API启动新模块Java程序
     *   3) 获取到新模块的启动类和Spring容器，构建DBModule
     *   4) 调用Module Manager模块的更新用户模块的API，真正地更新用户模块
     *   5) 调用控制程序的API销毁旧模块对应的Java程序
     * </pre>
     * @param args 输入参数
     * @return 更新是否成功
     * @since 0.0.0
     */
    public Boolean invoke0(String[] args) {
        try {
            DBModule oldModule = getOldModule(args[0]);
            DBModule newModule = bootNewModule(Arrays.copyOfRange(args, 1, args.length));
            MethodMapper.invoke(UPDATE_API, oldModule, newModule);
            MethodMapper.invoke(CMD_SERVICE_API, killArgs(args[0]));
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
     * 说明： 获取旧模块
     * 实现步骤：
     *   1. 将字符串的aid转化为Long型aid
     *   2. 根据aid获取对应的application实例
     *   3. 获取该app的主启动类和spring容器
     *   4. 构建DBModule实例并返回
     * </pre>
     * @param aidStr 传入的appId
     * @return appId对应程序的DB模块实例
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @since 0.0.0
     */
    private DBModule getOldModule(String aidStr) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Long aid = Long.valueOf(aidStr);
        return createDBModule(aid);
    }

    /**
     * <pre>
     * 说明：启动新模块Java程序
     * 实现步骤：
     *   1) 将启动参数转化为命令参数型式
     *   2) 调用控制程序的API启动对应的Java程序，并得到程序启动后的aid
     *   3) 根据新程序aid创建DBModule并返回
     * </pre>
     * @param args Java程序的地址，及其启动所需要的参数
     * @return 一个数据库模块
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @since 0.0.0
     */
    private DBModule bootNewModule(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String bootArg = bootModuleArgs(args);
        Long aid = (Long) MethodMapper.invoke(CMD_SERVICE_API, bootArg);
        if (aid == null) {
            throw new MethodParameterException("The module(" + args[0] + ")" + "boot failed.");
        }
        return createDBModule(aid);
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
     * 说明：该方法用于生成执行boot命令所需要的参数
     * 实现步骤:
     *   1) 构建一个buf
     *   2) 将boot装入buf
     *   3) 循环第将各个参数装入buffer, 注意参数用""包裹，参数之间用空格间隔
     *   4) 返回结果
     * </pre>
     * @param args 启动一个模块所须的参数
     * @return 整合后的模块参数
     * @since 0.0.0
     */
    private String bootModuleArgs(String[] args) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(BOOT_CMD).append(" ");
        for (String arg : args) {
            buffer.append("\"")
                    .append(arg)
                    .append("\"")
                    .append(" ");
        }
        buffer.setLength(buffer.length() - 1);
        return buffer.toString();
    }

    /**
     * <pre>
     * 说明：判断给定的程序Id是否为有效的Java程序Id
     * </pre>
     * @param programId 程序Id
     * @return 是否为有效的Java程序Id
     * @since 0.0.0
     */
    private boolean isValidJavaProgramId(String programId) {
        return ApplicationManagementImpl.getInstance()
                .application(Long.valueOf(programId)) != null;
    }

    /**
     * <pre>
     * 说明：判断给定的程序名是否为Java程序
     * </pre>
     * @param programName 程序名
     * @return 是否为Java程序
     * @since 0.0.0
     */
    private boolean isJavaProgram(String programName) {
        return FileUtils.isJar(programName);
    }
}

