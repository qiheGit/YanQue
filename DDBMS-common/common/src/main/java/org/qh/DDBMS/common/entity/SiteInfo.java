package org.qh.DDBMS.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.qh.DDBMS.common.Validator;
import org.qh.tools.str.StringUtils;

import java.io.Serializable;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/19
 * @Version: 0.0.0
 * @Description: 站点信息实例类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SiteInfo implements Validator<SiteInfo, String> , Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * <pre>
     * 说明：站点名称
     * </pre>
     */
    private String name;

    /**
     * <pre>
     * 说明：站点代表的数据库名称
     * </pre>
     */
    private String dbName;

    /**
     * <pre>
     * 说明：站点ip地址
     * </pre>
     */
    private String ip;

    /**
     * <pre>
     * 说明：站点服务器监听的端口号
     * </pre>
     */
    private int port;

    /**
     * <pre>
     * 说明：该站点是否能作为主站点
     * </pre>
     */
    private boolean canBeMaster;

    /**
     * <pre>
     * 说明：站点执行事务数
     * </pre>
     */
    private long transactionCount;

    /**
     * <pre>
     * 说明：检查传入实例的各个字段是否合法
     * 实现步骤：
     *   1) 判定name为空，则返回
     *   "The name field is empty.";
     *   2) 判定dbName为空，则返回
     *   "The dbName field is empty.";
     *   3) 判定ip为空，则返回
     *   "The ip field is empty.";
     *   4) 判定port小于1，则返回
     *   "The port field is not valid.";
     *   5) 判定transactionCount小于0则返回
     *   "The transactionCount field is not valid.";
     *   6) 返回null
     * </pre>
     * @param info 需要被校验的实例/辅助校验的实例
     * @return null合法，非null则非法
     */
    @Override
    public String validate(SiteInfo info) {
        if (StringUtils.isEmpty(info.getName())) return "The name field is empty.";
        if (StringUtils.isEmpty(info.getDbName())) return "The db name field is empty.";
        if (StringUtils.isEmpty(info.getIp())) return "The ip field is empty.";
        if (info.getPort() < 1) return "The port field is not valid.";
        if (info.getTransactionCount() < 0) return "The transaction count field is not valid.";
        return "";
    }
}

