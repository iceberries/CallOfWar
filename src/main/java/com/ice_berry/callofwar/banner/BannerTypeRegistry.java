package com.ice_berry.callofwar.banner;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.ice_berry.callofwar.CallOfWar;

import net.minecraft.resources.ResourceLocation;

/**
 * 战旗类型注册表
 * 支持战旗类型的运行时注册与查询
 */
public class BannerTypeRegistry {

    private static final BannerTypeRegistry INSTANCE = new BannerTypeRegistry();

    private final Map<ResourceLocation, BannerType> registry = new HashMap<>();

    private BannerTypeRegistry() {}

    public static BannerTypeRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * 注册战旗类型
     */
    public void register(BannerType bannerType) {
        ResourceLocation id = bannerType.getId();
        if (registry.containsKey(id)) {
            CallOfWar.LOGGER.warn("BannerType {} is already registered, overwriting", id);
        }
        registry.put(id, bannerType);
        CallOfWar.LOGGER.info("Registered BannerType: {}", id);
    }

    /**
     * 获取战旗类型
     */
    public Optional<BannerType> get(ResourceLocation id) {
        return Optional.ofNullable(registry.get(id));
    }

    /**
     * 检查是否已注册
     */
    public boolean isRegistered(ResourceLocation id) {
        return registry.containsKey(id);
    }

    /**
     * 获取所有已注册的战旗类型
     */
    public Collection<BannerType> getAll() {
        return Collections.unmodifiableCollection(registry.values());
    }

    /**
     * 获取所有已注册的战旗类型ID
     */
    public Collection<ResourceLocation> getAllIds() {
        return Collections.unmodifiableSet(registry.keySet());
    }

    /**
     * 清空注册表（用于测试或重载）
     */
    public void clear() {
        registry.clear();
    }
}
