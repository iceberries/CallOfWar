package com.ice_berry.callofwar.banner.team;

import java.util.Optional;
import java.util.UUID;

import com.ice_berry.callofwar.CallOfWar;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.TeamManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * FTB Teams 团队服务实现
 * 适配 FTB Teams API
 */
public class FTBTeamService implements ITeamService {

    @Override
    public String getName() {
        return "FTB Teams";
    }

    @Override
    public boolean isAvailable() {
        try {
            // 检查 FTB Teams API 是否可用
            return FTBTeamsAPI.api() != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Optional<UUID> getTeamId(Player player) {
        if (player == null || !(player instanceof ServerPlayer serverPlayer)) {
            return Optional.empty();
        }

        try {
            return FTBTeamsAPI.api().getManager().getTeamForPlayer(serverPlayer)
                .map(Team::getId);
        } catch (Exception e) {
            CallOfWar.LOGGER.debug("Error getting FTB team for player {}: {}", 
                player.getName().getString(), e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> getTeamName(UUID teamId) {
        if (teamId == null) {
            return Optional.empty();
        }

        try {
            TeamManager manager = FTBTeamsAPI.api().getManager();
            Optional<Team> team = manager.getTeamByID(teamId);
            return team.map(t -> t.getName().getString());
        } catch (Exception e) {
            CallOfWar.LOGGER.debug("Error getting FTB team name for id {}: {}", 
                teamId, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean isPlayerInTeam(Player player, UUID targetTeamId) {
        if (targetTeamId == null || player == null) {
            return false;
        }

        Optional<UUID> playerTeamId = getTeamId(player);
        boolean result = playerTeamId.map(id -> id.equals(targetTeamId)).orElse(false);
        
        CallOfWar.LOGGER.debug("FTBTeamService.isPlayerInTeam: player={}, playerTeamId={}, targetTeamId={}, result={}", 
            player.getName().getString(), playerTeamId.orElse(null), targetTeamId, result);
        
        return result;
    }

    @Override
    public boolean isSameTeam(Player player1, Player player2) {
        Optional<UUID> team1 = getTeamId(player1);
        Optional<UUID> team2 = getTeamId(player2);

        if (team1.isPresent() && team2.isPresent()) {
            return team1.get().equals(team2.get());
        }

        // 降级到原版队伍系统
        net.minecraft.world.scores.Team vanillaTeam1 = player1.getTeam();
        net.minecraft.world.scores.Team vanillaTeam2 = player2.getTeam();
        if (vanillaTeam1 != null && vanillaTeam2 != null) {
            return vanillaTeam1.isAlliedTo(vanillaTeam2);
        }

        return false;
    }

    @Override
    public boolean isFriendly(Level level, LivingEntity entity1, LivingEntity entity2) {
        // 优先检查原版队伍系统（因为它也可能被使用）
        net.minecraft.world.scores.Team team1 = entity1.getTeam();
        net.minecraft.world.scores.Team team2 = entity2.getTeam();
        if (team1 != null && team2 != null && team1.isAlliedTo(team2)) {
            return true;
        }

        // 如果都是玩家，检查 FTB Teams
        if (entity1 instanceof Player player1 && entity2 instanceof Player player2) {
            return isSameTeam(player1, player2);
        }

        // 检查所有权关系
        if (entity1 instanceof Player player1 && !(entity2 instanceof Player)) {
            return isOwnedByOrFriendly(level, entity2, player1);
        }
        if (entity2 instanceof Player player2 && !(entity1 instanceof Player)) {
            return isOwnedByOrFriendly(level, entity1, player2);
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
    public boolean isPlayerUUIDInTeam(UUID targetTeamId, UUID playerUUID) {
        if (targetTeamId == null || playerUUID == null) {
            return false;
        }

        try {
            TeamManager manager = FTBTeamsAPI.api().getManager();
            Optional<Team> team = manager.getTeamByID(targetTeamId);
            if (team.isPresent()) {
                // FTB Teams Team 接口有 getMembers() 方法返回成员UUID集合
                return team.get().getMembers().contains(playerUUID);
            }
        } catch (Exception e) {
            CallOfWar.LOGGER.debug("Error checking if player {} is in team {}: {}", 
                playerUUID, targetTeamId, e.getMessage());
        }
        return false;
    }

    @Override
    public boolean isOwnedByOrFriendly(Level level, LivingEntity entity, Player player) {
        // 如果实体是玩家，检查是否同队
        if (entity instanceof Player targetPlayer) {
            return isSameTeam(player, targetPlayer);
        }

        // 获取实体的主人
        Optional<UUID> ownerUUID = getOwnerUUID(entity);
        if (ownerUUID.isPresent()) {
            // 首先检查主人是否就是该玩家
            if (ownerUUID.get().equals(player.getUUID())) {
                return true;
            }
            
            // 检查主人是否在该玩家的团队中
            Optional<UUID> playerTeamId = getTeamId(player);
            if (playerTeamId.isPresent()) {
                return isPlayerUUIDInTeam(playerTeamId.get(), ownerUUID.get());
            }
        }

        return false;
    }
}
