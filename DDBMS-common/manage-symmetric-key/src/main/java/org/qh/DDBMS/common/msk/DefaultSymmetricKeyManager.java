package org.qh.DDBMS.common.msk;

import org.qh.DDBMS.common.key.DefaultSymmetricKey;
import org.qh.DDBMS.common.key.SymmetricKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/20
 * @Version: 0.0.0
 * @Description: 该类是SymmetricKeyManager接口的默认实现类
 */
public class DefaultSymmetricKeyManager implements SymmetricKeyManager<String> {

    private Map<String, byte[]> symmetricMap; // 该map中保存了站点和其对称密钥的映射关系

    public DefaultSymmetricKeyManager() {
        this.symmetricMap = new ConcurrentHashMap<>();
    }

    /**
     * 说明：注册对称密钥到该管理器中
     * 实现步骤：
     *   1) 将所有者和对称密钥的关系映射到symmetricMap中。
     * @param key 要注册的对称密钥
     * @since 0.0.0
     */
    @Override
    public void registerKey(SymmetricKey<String> key) {
        symmetricMap.put(key.owner(), key.key());
    }

    /**
     * 说明：注销传入所有者对应的对称密钥
     * 实现步骤：
     *   1) 将所有者和对称密钥的关系从symmetricMap中删除。
     * @param owner 对称密钥的所有者
     * @return 被注销的对称密钥
     * @since 0.0.0
     */
    @Override
    public SymmetricKey<String> unregisterKey(String owner) {
        byte[] key = symmetricMap.remove(owner);
        return key != null ? new DefaultSymmetricKey(owner, key) : null;
    }

    /**
     * 说明：获取所有者对应的对称密钥
     * 实现步骤：
     *   1) 根据传入的所有者，获取在symmetricMap中映射的对称密钥key
     *   2) 返回key。
     * @param owner 对称密钥的所有者
     * @return 对称密钥的字节数组
     * @since 0.0.0
     */
    @Override
    public byte[] getKey(String owner) {
        return symmetricMap.get(owner);
    }
}

