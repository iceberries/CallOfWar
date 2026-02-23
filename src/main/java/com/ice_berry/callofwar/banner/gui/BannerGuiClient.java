package com.ice_berry.callofwar.banner.gui;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * 战旗 GUI 客户端注册
 */
@EventBusSubscriber(modid = "callofwar", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class BannerGuiClient {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(BannerMenuType.BANNER_MENU.get(), BannerScreen::new);
    }
}
