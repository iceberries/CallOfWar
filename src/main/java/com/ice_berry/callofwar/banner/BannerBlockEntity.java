package com.ice_berry.callofwar.banner;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.ice_berry.callofwar.CallOfWar;
import com.ice_berry.callofwar.banner.team.ITeamService;
import com.ice_berry.callofwar.banner.team.TargetFilterMode;
import com.ice_berry.callofwar.banner.team.TeamServiceManager;

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
    private UUID placerUUID;  // 放置者UUID（用于GUI权限检查）
    private UUID teamId;  // 团队ID
    private TargetFilterMode filterMode;  // 目标筛选模式（可配置）

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
    public BannerType getBannerType() {
        if (cachedBannerType == null && getBlockState().getBlock() instanceof COWAbstractBannerBlock bannerBlock) {
            cachedBannerType = bannerBlock.getBannerType();
        }
        return cachedBannerType;
    }

    /**
     * 获取团队服务
     */
    public ITeamService getTeamService() {
        return TeamServiceManager.getInstance().getActiveService();
    }

    /**
     * 获取目标筛选模式
     * 如果未配置，使用 BannerType 中的默认值
     */
    public TargetFilterMode getFilterMode() {
        if (filterMode == null) {
            BannerType type = getBannerType();
            if (type != null) {
                return type.getBehavior().getTargetFilterMode();
            }
            return TargetFilterMode.TEAM_ONLY;
        }
        return filterMode;
    }

    /**
     * 设置目标筛选模式
     */
    public void setFilterMode(TargetFilterMode mode) {
        this.filterMode = mode;
        this.setChanged();
    }

    /**
     * 设置放置者并记录其UUID和团队ID
     */
    public void setPlacer(LivingEntity placer) {
        if (placer instanceof Player player) {
            this.placerUUID = player.getUUID();
            ITeamService teamService = getTeamService();
            this.teamId = teamService.getTeamId(player).orElse(null);
            CallOfWar.LOGGER.info("Banner placed by player {}, placerUUID: {}, teamId: {}, teamService: {}", 
                player.getName().getString(), placerUUID, teamId, teamService.getName());
        } else {
            this.placerUUID = null;
            this.teamId = null;
        }
        this.setChanged();
    }

    /**
     * 获取放置者UUID
     */
    public UUID getPlacerUUID() {
        return placerUUID;
    }

    /**
     * 检查玩家是否是放置者
     */
    public boolean isPlacer(Player player) {
        return placerUUID != null && placerUUID.equals(player.getUUID());
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
     * 获取团队名称
     */
    public String getTeamName() {
        if (teamId == null) {
            return "No Team";
        }
        return getTeamService().getTeamName(teamId).orElse("Unknown Team");
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
        TargetFilterMode mode = getFilterMode();  // 使用可配置的模式
        ITeamService teamService = getTeamService();

        AABB searchBox = new AABB(
            pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius,
            pos.getX() + radius + 1, pos.getY() + radius + 1, pos.getZ() + radius + 1
        );

        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, searchBox);

        for (LivingEntity entity : entities) {
            if (entity.distanceToSqr(Vec3.atCenterOf(pos)) > radius * radius) {
                continue;
            }

            // 使用目标筛选模式判断
            if (!shouldAffectEntity(level, entity, mode, teamService)) {
                continue;
            }

            applyEffects(entity, behavior.getEffects());
        }
    }

    /**
     * 判断是否应该对实体施加效果
     */
    protected boolean shouldAffectEntity(Level level, LivingEntity entity, 
                                         TargetFilterMode filterMode, ITeamService teamService) {
        switch (filterMode) {
            case ALL_ENTITIES:
                return true;

            case ALL_PLAYERS:
                return entity instanceof Player;

            case TEAM_ONLY:
                return checkEntityInTeam(entity, level, teamService, true);

            case TEAM_AND_ALLIES:
                return checkEntityInTeam(entity, level, teamService, false);

            case ENEMIES_ONLY:
                return !checkEntityInTeam(entity, level, teamService, true);

            default:
                return false;
        }
    }

    /**
     * 检查实体是否在团队中
     * @param strictTeamOnly 严格模式：只检查团队；非严格：包含盟友
     */
    protected boolean checkEntityInTeam(LivingEntity entity, Level level, 
                                        ITeamService teamService, boolean strictTeamOnly) {
        // 如果没有记录团队ID，不给任何人生效
        if (teamId == null) {
            CallOfWar.LOGGER.debug("Team-restricted banner has no teamId, skipping entity: {}", 
                entity.getName().getString());
            return false;
        }

        // 玩家实体
        if (entity instanceof Player player) {
            boolean inTeam = teamService.isPlayerInTeam(player, teamId);
            CallOfWar.LOGGER.debug("Player {} in team check: {} (banner teamId: {})", 
                player.getName().getString(), inTeam, teamId);
            return inTeam;
        }

        // 非玩家实体：检查主人是否在团队中
        Optional<UUID> ownerUUID = teamService.getOwnerUUID(entity);
        if (ownerUUID.isPresent()) {
            // 检查主人是否在团队中
            boolean ownerInTeam = teamService.isPlayerUUIDInTeam(teamId, ownerUUID.get());
            CallOfWar.LOGGER.debug("Entity {} owner {} in team check: {}", 
                entity.getName().getString(), ownerUUID.get(), ownerInTeam);
            return ownerInTeam;
        }

        // 没有主人的实体不享受团队限制战旗效果
        return false;
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
        if (placerUUID != null) {
            tag.putUUID("PlacerUUID", placerUUID);
        }
        if (teamId != null) {
            tag.putUUID("TeamId", teamId);
        }
        if (filterMode != null) {
            tag.putString("FilterMode", filterMode.getId());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        tickCounter = tag.getInt("TickCounter");
        if (tag.hasUUID("PlacerUUID")) {
            placerUUID = tag.getUUID("PlacerUUID");
        }
        if (tag.hasUUID("TeamId")) {
            teamId = tag.getUUID("TeamId");
        }
        if (tag.contains("FilterMode")) {
            filterMode = TargetFilterMode.fromId(tag.getString("FilterMode"));
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        cachedBannerType = null;
    }
}
