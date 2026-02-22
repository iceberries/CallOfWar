package com.ice_berry.callofwar.banner;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.ice_berry.callofwar.CallOfWar;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * 队伍辅助类
 * 整合原版队伍系统和FTB Teams
 */
public class TeamHelper {

    /**
     * 检查两个实体是否在同一队伍
     * @param level 世界
     * @param entity1 实体1
     * @param entity2 实体2
     * @return 是否同队
     */
    public static boolean isSameTeam(Level level, @Nullable LivingEntity entity1, @Nullable LivingEntity entity2) {
        if (entity1 == null || entity2 == null) {
            return false;
        }

        // 优先检查原版队伍系统
        if (entity1.getTeam() != null && entity2.getTeam() != null) {
            return entity1.getTeam().isAlliedTo(entity2.getTeam());
        }

        // 检查FTB Teams
        if (entity1 instanceof Player player1 && entity2 instanceof Player player2) {
            return isSameFTBTeam(player1, player2);
        }

        return false;
    }

    /**
     * 检查玩家是否属于指定团队ID
     * @param player 玩家
     * @param teamId 团队ID
     * @return 是否属于该团队
     */
    public static boolean isPlayerInTeam(Player player, @Nullable UUID teamId) {
        if (teamId == null || player == null) {
            return false;
        }

        Optional<UUID> playerTeamId = getFTBTeamId(player);
        boolean result = playerTeamId.map(id -> id.equals(teamId)).orElse(false);
        
        CallOfWar.LOGGER.debug("isPlayerInTeam check: player={}, playerTeamId={}, bannerTeamId={}, result={}", 
            player.getName().getString(), playerTeamId.orElse(null), teamId, result);
        
        return result;
    }

    /**
     * 获取玩家的FTB Teams团队ID
     * @param player 玩家
     * @return 团队ID，如果玩家没有团队则返回空
     */
    public static Optional<UUID> getFTBTeamId(Player player) {
        if (player == null) {
            return Optional.empty();
        }

        // 只有服务端玩家才能查询FTB Teams
        if (!(player instanceof ServerPlayer serverPlayer)) {
            CallOfWar.LOGGER.debug("Player {} is not a ServerPlayer, cannot get FTB team", 
                player.getName().getString());
            return Optional.empty();
        }

        try {
            Optional<Team> team = FTBTeamsAPI.api().getManager().getTeamForPlayer(serverPlayer);
            if (team.isPresent()) {
                UUID teamId = team.get().getId();
                CallOfWar.LOGGER.debug("Player {} has FTB team: {} ({})", 
                    player.getName().getString(), team.get().getName().getString(), teamId);
                return Optional.of(teamId);
            } else {
                CallOfWar.LOGGER.debug("Player {} has no FTB team", player.getName().getString());
                return Optional.empty();
            }
        } catch (Exception e) {
            CallOfWar.LOGGER.error("Error getting FTB team for player {}: {}", 
                player.getName().getString(), e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 检查两个玩家是否在同一FTB队伍
     */
    public static boolean isSameFTBTeam(Player player1, Player player2) {
        Optional<UUID> team1 = getFTBTeamId(player1);
        Optional<UUID> team2 = getFTBTeamId(player2);

        // 如果两个玩家都有团队，比较团队ID
        if (team1.isPresent() && team2.isPresent()) {
            return team1.get().equals(team2.get());
        }
        // 如果两个玩家都没有团队，返回false
        return false;
    }

    /**
     * 获取玩家所在的FTB队伍名称
     */
    @Nullable
    public static String getFTBTeamName(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return null;
        }
        try {
            return FTBTeamsAPI.api().getManager().getTeamForPlayer(serverPlayer)
                .map(team -> team.getName().getString())
                .orElse(null);
        } catch (Exception e) {
            CallOfWar.LOGGER.error("Error getting FTB team name for player {}: {}", 
                player.getName().getString(), e.getMessage());
            return null;
        }
    }

    /**
     * 检查玩家是否在FTB队伍中
     */
    public static boolean isInFTBTeam(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        try {
            return FTBTeamsAPI.api().getManager().getTeamForPlayer(serverPlayer).isPresent();
        } catch (Exception e) {
            CallOfWar.LOGGER.error("Error checking if player {} is in FTB team: {}", 
                player.getName().getString(), e.getMessage());
            return false;
        }
    }
}
