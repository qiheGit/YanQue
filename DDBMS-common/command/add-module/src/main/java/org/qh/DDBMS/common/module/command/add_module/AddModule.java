package org.qh.DDBMS.common.module.command.add_module;

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

/**
 *
 * @Author: qihe
 * @Date: 2024/10/24
 * @Version: 0.0.0
 * @Description: 该类是实现添加用户数据库模块的命令行程序中的业务类
 */
public class AddModule extends SyncCommand<String[], Boolean> {
    // 用户模块管理提供注册用户模块的API的签名
    private static String REGISTER_API = "org.qh.DDBMS.common.module.DBModuleManager.registerModule(org.qh.DDBMS.common.module.DBModule)";
    // 控制程序执行命令的API接口标签
    private static String CMD_SERVICE_API = "org.qh.command.CMDService.exec(java.lang.String)";

    private static String IOC = "getIOC"; // 获取IOC的方法名

    private static String BOOT_CMD = "boot"; // boot命令名

    public AddModule() {
        super("adm");
    }

    /**
     * 说明：当前命令的补全器
     * 实现步骤：
     *   1) 第一个参数是当前命令的key
     *   2) 第二个参数是一个Java程序的启动程序
     *   3) 第三个参数是一个Java程序(用户编写的操作数据库的模块)
     * @since 0.0.0
     */
    @Override
    protected void initCompleter() {
        ArgumentCompleter completer = new ArgumentCompleter(
                new StringsCompleter(key()),
                new Completers.FileNameCompleter(),
                new Completers.FileNameCompleter(),
                new NullCompleter()
        );
        completer(new CommandCompleter(key(), completer));
    }

    /**
     * <pre>
     * 说明：在执行前验证传入参数，是否合法
     * 实现步骤：
     *   1) 判定入参为空，返回null
     *   2) 判定第一个入参不是一个Java程序，返回null
     *   3) 返回入参
     * </pre>
     * @param args 输入参数
     * @return 验证后的参数
     * @since 0.0.0
     */
    public String[] beforeInvoke(String[] args) {
        // 实现逻辑
        if (args == null || args.length == 0) {
            return null;
        }
        if (!isJavaProgram(args[0])) {
            return null;
        }
        return args;
    }

    /**
     * <pre>
     * 说明：执行注册用户模块的业务
     * 实现步骤：
     *   1) 调用控制程序API启动传入参数指定的Java程序
     *   2) 获取到被启动程序的启动类和Spring容器
     *   3) 构建DBModule实例
     *   4) 调用Module Manager模块的注册用户模块的API真正地注册用户模块
     * </pre>
     * @param args 输入参数
     * @since 0.0.0
     */
    protected Boolean invoke0(String[] args) {
        Long aid = bootModule(args);
        try {
            DBModule dbModule = createDBModule(aid);
            MethodMapper.invoke(REGISTER_API, dbModule);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    /**
     * <pre>
     * 说明：判断给定的程序名是否为Java程序
     * </pre>
     * @param programName 程序名
     * @return 是否为Java程序
     */
    private boolean isJavaProgram(String programName) {
        return FileUtils.isJar(programName);
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

    private Long bootModule(String[] args) {
        String bootModuleArgs = bootModuleArgs(args);
        Long aid = (Long) MethodMapper.invoke(CMD_SERVICE_API, bootModuleArgs);
        if (aid == null) {
            throw new MethodParameterException("The module(" + args[0] + ")" + "boot failed.");
        }
        return aid;
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
}

