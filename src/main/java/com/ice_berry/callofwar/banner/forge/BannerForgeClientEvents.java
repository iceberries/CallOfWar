package com.ice_berry.callofwar.banner.forge;

import com.ice_berry.callofwar.CallOfWar;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * 战旗工坊客户端事件处理
 */
@EventBusSubscriber(modid = CallOfWar.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class BannerForgeClientEvents {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(BannerForgeRegistrar.MENU_TYPE.get(), BannerForgeScreen::new);
        CallOfWar.LOGGER.info("BannerForge screen registered");
    }
}
