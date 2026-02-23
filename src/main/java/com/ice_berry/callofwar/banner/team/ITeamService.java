package com.ice_berry.callofwar.banner.team;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * 团队服务接口
 * 抽象团队查询、友好判断、信息获取
 * 支持多后端：FTB Teams、原版队伍、无队伍
 */
public interface ITeamService {

    /**
     * 获取服务名称（用于调试/日志）
     */
    String getName();

    /**
     * 检查服务是否可用
     */
    boolean isAvailable();

    /**
     * 获取玩家的团队ID
     * @param player 玩家
     * @return 团队ID，如果玩家没有团队则返回空
     */
    Optional<UUID> getTeamId(Player player);

    /**
     * 获取团队的显示名称
     * @param teamId 团队ID
     * @return 团队名称，如果团队不存在则返回空
     */
    Optional<String> getTeamName(UUID teamId);

    /**
     * 检查玩家是否属于指定团队
     * @param player 玩家
     * @param teamId 团队ID
     * @return 是否属于该团队
     */
    boolean isPlayerInTeam(Player player, UUID teamId);

    /**
     * 检查两个玩家是否在同一团队
     * @param player1 玩家1
     * @param player2 玩家2
     * @return 是否同队
     */
    boolean isSameTeam(Player player1, Player player2);

    /**
     * 检查两个实体是否友好（同队或盟友）
     * 包括非玩家实体（通过主人追溯）
     * @param level 世界
     * @param entity1 实体1
     * @param entity2 实体2
     * @return 是否友好
     */
    boolean isFriendly(Level level, LivingEntity entity1, LivingEntity entity2);

    /**
     * 获取实体的主人（如果是驯服的宠物/召唤物）
     * @param entity 实体
     * @return 主人的UUID，如果没有主人则返回空
     */
    Optional<UUID> getOwnerUUID(LivingEntity entity);

    /**
     * 检查实体是否属于指定玩家或该玩家的团队
     * @param level 世界
     * @param entity 实体
     * @param player 玩家
     * @return 是否归属
     */
    default boolean isOwnedByOrFriendly(Level level, LivingEntity entity, Player player) {
        // 如果实体是玩家，检查是否同队
        if (entity instanceof Player targetPlayer) {
            return isSameTeam(player, targetPlayer);
        }
        
        // 如果实体有主人，检查主人是否同队
        Optional<UUID> ownerUUID = getOwnerUUID(entity);
        if (ownerUUID.isPresent()) {
            // 首先检查主人是否就是该玩家
            if (ownerUUID.get().equals(player.getUUID())) {
                return true;
            }
            // 然后检查主人是否在该玩家的团队中
            Optional<UUID> playerTeamId = getTeamId(player);
            if (playerTeamId.isPresent()) {
                return isPlayerInTeam(player, ownerUUID.get());
            }
        }
        
        return false;
    }

    /**
     * 检查UUID对应的玩家是否在指定团队中
     * @param teamId 团队ID
     * @param playerUUID 玩家UUID
     * @return 是否在团队中
     */
    boolean isPlayerUUIDInTeam(UUID teamId, UUID playerUUID);
}
