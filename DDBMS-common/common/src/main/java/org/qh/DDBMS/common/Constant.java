package org.qh.DDBMS.common;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/20
 * @Version: 0.0.0
 * @Description:
 */
public interface Constant {
    interface Protocol {
        byte ASSIGN_KEY_PROTOCOL_TYPE = 36;
        byte CALL_METHOD_PROTOCOL_TYPE = 37;
        byte ACK_PROTOCOL_TYPE = 38;
        byte SYNC_INFO_PROTOCOL_TYPE = 39;

    }

    interface ObjectProtocolDataUse {
        byte SYNC_INFO_PROTOCOL = 0;
        byte REQUEST_SYNC_INFO_PROTOCOL = SYNC_INFO_PROTOCOL + 1;
    }

    interface ACKType {
        // 提交事务ackType
        short COMMIT_TYPE = Short.MIN_VALUE;
        // 对提交事务进行响应的ackType
        short RESPONSE_COMMIT_TYPE = COMMIT_TYPE + 1;
        // 异常信息ackType
        short EXCEPTION = RESPONSE_COMMIT_TYPE + 1;

        // 撤销事务的ackType
        short CANCEL_DBTRANSACTION = EXCEPTION + 1;

        // 方法执行结果回调的ackType
        short METHOD_CALLBACK = CANCEL_DBTRANSACTION + 1;

        // 分发主站点信息的ackType
        short ASSIGN_MASTER_INFO = METHOD_CALLBACK + 1;

        // 站点信息协议的ackType
        short SITE_INFO = ASSIGN_MASTER_INFO + 1;

        // 请求主站信息的ackType
        short REQUEST_SITE_INFO = SITE_INFO + 1;

        // 通知站点缺少提交事务的ackType
        short LACK_COMMITED_TRANSACTION = REQUEST_SITE_INFO + 1;

        // 通知客户端事务执行成功的ackType
        short DBTRANSACTION_SUCCESS = LACK_COMMITED_TRANSACTION + 1;

        // 通知客户端事务失败的ackType
        short DBTRANSACTION_FAILURE = DBTRANSACTION_SUCCESS + 1;

        // 通知客户端连接准备完成
        short READY = DBTRANSACTION_FAILURE + 1;
    }

    interface KeyType {
        byte SYMMETRIC_KEY = 0;
        byte PUBLIC_KEY = 1;
    }

    interface Code {

        String RSA = "RSA";  // RSA加密算法名称
        String AES = "AES";  // AES加密算法名称
    }

    interface Sync {
        String SYNC_INFO_TABLE_PREFIX = "sync_info_";

        // 同步信息中sql语句之间的分割符。
        String SQL_STATEMENT_SEPARATOR = "\nSQL_STATEMENT_SEPARATOR\n";

        // 发送同步信息给到从站点的间隔,单位ms
        long DISPATCH_SYNC_INFO_INTERVAL = 1000;

    }

    interface ServerAndClient {
        // netty 中的工作线程数
        int WORK_THREAD_COUNT = Runtime.getRuntime().availableProcessors();
        // netty 中监听线程数
        int BOSS_THREAD_COUNT = Math.max(Runtime.getRuntime().availableProcessors() / 3, 1);
    }

    interface TransactionCenter {

        // 允许事务提交状态
        byte COMMIT = 0;

        // 封锁失败
        byte LOCK_FAILURE = 1;

        // 无法继续执行，该事务必须失败
        byte FAILURE = 2;

        // 由于GDBMS要求撤销事务而导致的失败
        byte CANCEL_FAILURE = 3;

        // 重新启动一个因为封锁失败事务的间隔，单位ms
        long LOCK_FAILURE_REBOOT_INTERVAL = 500;
    }

    interface DBLock{
        // DBLock模块加锁时需要带上的前缀
        String DBLOCK_PREFIX = "dblock_";
    }

    interface SQL {

        // 读SQL类型
        byte READ_TYPE = 1;

        // 写SQL类型
        byte WRITE_TYPE = 2;
    }
}
