package com.ice_berry.callofwar.banner;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 战旗行为接口
 * 封装范围计算、效果获取、目标筛选、放置/移除回调
 */
public interface IBannerBehavior {

    /**
     * 获取效果作用半径
     */
    double getRadius();

    /**
     * 获取要施加的效果列表
     */
    List<MobEffectInstance> getEffects();

    /**
     * 筛选目标实体
     * @param level 世界
     * @param pos 战旗位置
     * @param entity 候选实体
     * @param bannerType 战旗类型（用于获取配置如同队检测等）
     * @return 是否应该对目标施加效果
     */
    boolean shouldAffectEntity(Level level, BlockPos pos, LivingEntity entity, BannerType bannerType);

    /**
     * 战旗放置时调用
     */
    default void onPlaced(Level level, BlockPos pos, BlockState state) {}

    /**
     * 战旗移除时调用
     */
    default void onRemoved(Level level, BlockPos pos, BlockState state) {}

    /**
     * 获取效果检查间隔（tick）
     */
    default int getCheckInterval() {
        return 20; // 默认每秒检查一次
    }

    /**
     * 是否启用同队限制
     */
    default boolean isTeamRestricted() {
        return false;
    }
}
