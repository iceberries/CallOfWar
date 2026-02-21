package com.ice_berry.callofwar.banner;

import javax.annotation.Nullable;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * 队伍辅助类
 * 用于处理同队检测，预留FTP队伍系统集成接口
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

        // 使用原版队伍系统作为基础
        if (entity1.getTeam() != null && entity2.getTeam() != null) {
            return entity1.getTeam().isAlliedTo(entity2.getTeam());
        }

        // 如果是玩家，检查FTP队伍
        if (entity1 instanceof Player player1 && entity2 instanceof Player player2) {
            return isSameFTPTeam(level, player1, player2);
        }

        return false;
    }

    /**
     * 检查两个玩家是否在同一FTP队伍
     * TODO: 与FTP队伍系统集成
     */
    public static boolean isSameFTPTeam(Level level, Player player1, Player player2) {
        // 预留FTP队伍系统集成接口
        // 示例实现（需要实际FTP API）:
        // return FTPTeamAPI.isSameTeam(player1.getUUID(), player2.getUUID());
        
        // 暂时返回false，等待FTP集成
        return false;
    }

    /**
     * 获取玩家所在的FTP队伍名称
     * TODO: 与FTP队伍系统集成
     */
    @Nullable
    public static String getFTPTeamName(Player player) {
        // 预留FTP队伍系统集成接口
        // 示例实现（需要实际FTP API）:
        // return FTPTeamAPI.getTeamName(player.getUUID());
        
        return null;
    }

    /**
     * 检查玩家是否在FTP队伍中
     * TODO: 与FTP队伍系统集成
     */
    public static boolean isInFTPTeam(Player player) {
        // 预留FTP队伍系统集成接口
        // 示例实现（需要实际FTP API）:
        // return FTPTeamAPI.isInTeam(player.getUUID());
        
        return false;
    }
}
