package org.qh.DDBMS.common.module.command.add_module;

import org.qh.command.Command;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/24
 * @Version: 0.0.0
 * @Description: add module 命令的启动类
 */
public class Main {
    private static AddModule addModule = new AddModule();

    public static void main(String[] args) {
        addModule.invoke(args);
    }

    public static Command command() {
        return addModule;
    }
}
