package com.ice_berry.callofwar.banner;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 战旗BlockEntity基类
 */
public class BannerBlockEntity extends BlockEntity {

    private int tickCounter = 0;
    private BannerType cachedBannerType;

    public BannerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * 默认构造函数，供BlockEntityType使用
     */
    public BannerBlockEntity(BlockPos pos, BlockState state) {
        this(null, pos, state);
    }

    /**
     * 获取战旗类型
     */
    protected BannerType getBannerType() {
        if (cachedBannerType == null && getBlockState().getBlock() instanceof COWAbstractBannerBlock bannerBlock) {
            cachedBannerType = bannerBlock.getBannerType();
        }
        return cachedBannerType;
    }

    /**
     * 服务端tick逻辑
     */
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) {
            return;
        }

        BannerType bannerType = getBannerType();
        if (bannerType == null) {
            return;
        }

        tickCounter++;

        int checkInterval = bannerType.getCheckInterval();
        if (tickCounter % checkInterval != 0) {
            return;
        }

        applyEffectsToNearbyEntities(level, pos, bannerType);
    }

    /**
     * 对范围内实体施加效果
     */
    protected void applyEffectsToNearbyEntities(Level level, BlockPos pos, BannerType bannerType) {
        IBannerBehavior behavior = bannerType.getBehavior();
        double radius = behavior.getRadius();

        AABB searchBox = new AABB(
            pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius,
            pos.getX() + radius + 1, pos.getY() + radius + 1, pos.getZ() + radius + 1
        );

        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, searchBox);

        for (LivingEntity entity : entities) {
            if (entity.distanceToSqr(Vec3.atCenterOf(pos)) > radius * radius) {
                continue;
            }

            if (behavior.shouldAffectEntity(level, pos, entity, bannerType)) {
                applyEffects(entity, behavior.getEffects());
            }
        }
    }

    /**
     * 对单个实体施加效果
     */
    protected void applyEffects(LivingEntity entity, List<MobEffectInstance> effects) {
        for (MobEffectInstance effect : effects) {
            MobEffectInstance newEffect = new MobEffectInstance(
                effect.getEffect(),
                effect.getDuration() > 0 ? effect.getDuration() : 200,
                effect.getAmplifier(),
                effect.isAmbient(),
                effect.isVisible()
            );
            entity.addEffect(newEffect);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("TickCounter", tickCounter);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        tickCounter = tag.getInt("TickCounter");
    }

    @Override
    public void setChanged() {
        super.setChanged();
        cachedBannerType = null;
    }
}
