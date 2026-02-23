package com.ice_berry.callofwar.banner.team;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Team;

/**
 * 原版团队服务实现
 * 使用 Minecraft 原版记分板队伍系统
 * 作为无模组时的降级方案
 */
public class VanillaTeamService implements ITeamService {

    @Override
    public String getName() {
        return "Vanilla Teams";
    }

    @Override
    public boolean isAvailable() {
        return true; // 原版队伍系统始终可用
    }

    @Override
    public Optional<UUID> getTeamId(Player player) {
        // 原版队伍系统使用 Team 对象作为标识
        // 由于 Team 不是 UUID，我们返回玩家的 UUID 作为"个人队伍"
        // 或者可以返回空表示没有团队概念
        Team team = player.getTeam();
        if (team != null) {
            // 使用队伍名称的哈希作为伪 UUID
            // 这样同一队伍的玩家会有相同的 ID
            return Optional.of(UUID.nameUUIDFromBytes(team.getName().getBytes()));
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getTeamName(UUID teamId) {
        // 原版系统无法通过 UUID 反查队伍名称
        return Optional.empty();
    }

    @Override
    public boolean isPlayerInTeam(Player player, UUID teamId) {
        Optional<UUID> playerTeamId = getTeamId(player);
        return playerTeamId.isPresent() && playerTeamId.get().equals(teamId);
    }

    @Override
    public boolean isSameTeam(Player player1, Player player2) {
        Team team1 = player1.getTeam();
        Team team2 = player2.getTeam();
        
        if (team1 != null && team2 != null) {
            return team1.isAlliedTo(team2);
        }
        
        return false;
    }

    @Override
    public boolean isFriendly(Level level, LivingEntity entity1, LivingEntity entity2) {
        // 检查原版队伍系统
        Team team1 = entity1.getTeam();
        Team team2 = entity2.getTeam();
        
        if (team1 != null && team2 != null) {
            return team1.isAlliedTo(team2);
        }
        
        // 如果是玩家实体，检查所有权关系
        if (entity1 instanceof Player player1 && !(entity2 instanceof Player)) {
            return isOwnedByPlayer(entity2, player1);
        }
        if (entity2 instanceof Player player2 && !(entity1 instanceof Player)) {
            return isOwnedByPlayer(entity1, player2);
        }
        
        return false;
    }

    @Override
    public Optional<UUID> getOwnerUUID(LivingEntity entity) {
        if (entity instanceof OwnableEntity ownable) {
            return Optional.ofNullable(ownable.getOwnerUUID());
        }
        return Optional.empty();
    }

    @Override
    public boolean isPlayerUUIDInTeam(UUID teamId, UUID playerUUID) {
        // 原版系统无法通过 UUID 查询
        // 需要遍历在线玩家
        return false;
    }

    /**
     * 检查实体是否属于指定玩家
     */
    private boolean isOwnedByPlayer(LivingEntity entity, Player player) {
        Optional<UUID> ownerUUID = getOwnerUUID(entity);
        return ownerUUID.isPresent() && ownerUUID.get().equals(player.getUUID());
    }
}
