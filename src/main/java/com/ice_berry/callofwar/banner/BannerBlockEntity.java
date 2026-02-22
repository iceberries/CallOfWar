package com.ice_berry.callofwar.banner;

import java.util.List;
import java.util.UUID;

import com.ice_berry.callofwar.CallOfWar;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
    private UUID teamId;  // FTB Teams 团队ID

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
     * 设置放置者并记录其团队ID
     */
    public void setPlacer(LivingEntity placer) {
        if (placer instanceof Player player) {
            this.teamId = TeamHelper.getFTBTeamId(player).orElse(null);
            CallOfWar.LOGGER.info("Banner placed by player {}, teamId: {}", player.getName().getString(), teamId);
        } else {
            this.teamId = null;
        }
        this.setChanged();
    }

    /**
     * 直接设置团队ID
     */
    public void setTeamId(UUID teamId) {
        this.teamId = teamId;
        this.setChanged();
    }

    /**
     * 获取团队ID
     */
    public UUID getTeamId() {
        return teamId;
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

            // 团队检查逻辑
            if (bannerType.isTeamRestricted()) {
                // 如果没有记录团队ID，不给任何人生效
                if (teamId == null) {
                    CallOfWar.LOGGER.debug("Team-restricted banner has no teamId, skipping entity: {}", 
                        entity.getName().getString());
                    continue;
                }

                // 检查实体是否属于该团队
                if (entity instanceof Player player) {
                    boolean inTeam = TeamHelper.isPlayerInTeam(player, teamId);
                    CallOfWar.LOGGER.debug("Player {} in team check: {} (banner teamId: {})", 
                        player.getName().getString(), inTeam, teamId);
                    if (!inTeam) {
                        continue;
                    }
                } else {
                    // 非玩家实体不享受团队限制战旗效果
                    continue;
                }
            }

            applyEffects(entity, behavior.getEffects());
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
        if (teamId != null) {
            tag.putUUID("TeamId", teamId);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        tickCounter = tag.getInt("TickCounter");
        if (tag.hasUUID("TeamId")) {
            teamId = tag.getUUID("TeamId");
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        cachedBannerType = null;
    }
}
