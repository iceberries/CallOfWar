package com.ice_berry.callofwar.banner.team;

import java.util.ArrayList;
import java.util.List;

import com.ice_berry.callofwar.CallOfWar;

/**
 * 团队服务管理器
 * 自动选择可用的团队服务后端
 */
public class TeamServiceManager {

    private static final TeamServiceManager INSTANCE = new TeamServiceManager();
    
    private final List<ITeamService> services = new ArrayList<>();
    private ITeamService activeService;

    private TeamServiceManager() {
        // 按优先级注册服务（FTB Teams 优先，原版降级）
        registerService(new FTBTeamService());
        registerService(new VanillaTeamService());
        
        // 自动选择可用的服务
        selectActiveService();
    }

    /**
     * 获取单例实例
     */
    public static TeamServiceManager getInstance() {
        return INSTANCE;
    }

    /**
     * 注册团队服务
     */
    public void registerService(ITeamService service) {
        services.add(service);
        CallOfWar.LOGGER.debug("Registered team service: {}", service.getName());
    }

    /**
     * 选择可用的服务
     */
    public void selectActiveService() {
        for (ITeamService service : services) {
            if (service.isAvailable()) {
                activeService = service;
                CallOfWar.LOGGER.info("Selected team service: {}", service.getName());
                return;
            }
        }
        
        // 如果没有可用的服务，使用原版作为降级
        activeService = new VanillaTeamService();
        CallOfWar.LOGGER.warn("No team service available, using fallback: {}", activeService.getName());
    }

    /**
     * 获取当前活动的团队服务
     */
    public ITeamService getActiveService() {
        // 每次获取时检查是否仍然可用（可选）
        if (activeService == null || !activeService.isAvailable()) {
            selectActiveService();
        }
        return activeService;
    }

    /**
     * 获取所有已注册的服务
     */
    public List<ITeamService> getRegisteredServices() {
        return new ArrayList<>(services);
    }

    /**
     * 强制指定服务（用于测试或配置）
     */
    public void setActiveService(ITeamService service) {
        this.activeService = service;
        CallOfWar.LOGGER.info("Manually set team service to: {}", service.getName());
    }
}
