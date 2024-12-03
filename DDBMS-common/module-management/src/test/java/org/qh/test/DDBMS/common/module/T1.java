package org.qh.test.DDBMS.common.module;

import org.qh.DDBMS.common.module.DBMethod;
import org.qh.DDBMS.common.module.impl.DefaultDBModuleManager;
import org.qh.sys.MethodMapper;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/23
 * @Version:
 * @Description:
 */
public class T1 {
    public static void main(String[] args) {
        DefaultDBModuleManager defaultDBModuleManager = new DefaultDBModuleManager();
        DBMethod invoke = (DBMethod) MethodMapper.invoke("org.qh.DDBMS.common.module.DBModuleManager.getMethod(java.lang.String)", "hello/world");
        System.out.println(invoke);
    }

    public static void destroy(String[] args) {

    }
}
