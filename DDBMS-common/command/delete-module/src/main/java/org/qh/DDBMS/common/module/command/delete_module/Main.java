package org.qh.DDBMS.common.module.command.delete_module;

import org.qh.command.Command;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/24
 * @Version: 0.0.0
 * @Description: add module 命令的启动类
 */
public class Main {
    private static DeleteModule deleteModule = new DeleteModule();

    public static void main(String[] args) {
        deleteModule.invoke(args);
    }

    public static Command command() {
        return deleteModule;
    }
}
