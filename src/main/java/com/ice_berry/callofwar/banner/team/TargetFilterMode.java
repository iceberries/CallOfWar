package com.ice_berry.callofwar.banner.team;

/**
 * 目标筛选模式
 * 定义战旗效果的目标选择策略
 */
public enum TargetFilterMode {

    /**
     * 仅团队成员
     * 只有与战旗放置者同一团队的玩家才能获得效果
     * 仆从/宠物只有在其主人在团队中时才能获得效果
     */
    TEAM_ONLY("team_only", "Only team members"),

    /**
     * 团队成员 + 盟友
     * 团队成员和盟友都能获得效果
     */
    TEAM_AND_ALLIES("team_allies", "Team members and allies"),

    /**
     * 全体玩家
     * 所有玩家都能获得效果，不区分团队
     * 非玩家生物不受影响
     */
    ALL_PLAYERS("all_players", "All players"),

    /**
     * 全体实体
     * 所有范围内的实体都能获得效果
     */
    ALL_ENTITIES("all_entities", "All entities"),

    /**
     * 敌对反向（排除团队）
     * 只有非团队成员/敌对玩家获得效果
     * 用于负面效果的战旗
     */
    ENEMIES_ONLY("enemies_only", "Only enemies (non-team members)");

    private final String id;
    private final String description;

    TargetFilterMode(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 是否需要对团队进行检查
     */
    public boolean requiresTeamCheck() {
        return this == TEAM_ONLY || this == TEAM_AND_ALLIES || this == ENEMIES_ONLY;
    }

    /**
     * 是否包含非玩家实体
     */
    public boolean includesNonPlayers() {
        return this == ALL_ENTITIES;
    }

    /**
     * 是否应该对友好目标生效
     */
    public boolean shouldAffectFriendly() {
        return this != ENEMIES_ONLY;
    }

    /**
     * 从 ID 获取模式
     */
    public static TargetFilterMode fromId(String id) {
        for (TargetFilterMode mode : values()) {
            if (mode.id.equals(id)) {
                return mode;
            }
        }
        return TEAM_ONLY; // 默认值
    }
}
