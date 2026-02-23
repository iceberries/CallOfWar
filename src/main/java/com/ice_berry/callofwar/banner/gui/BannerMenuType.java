package com.ice_berry.callofwar.banner.gui;

import com.ice_berry.callofwar.CallOfWar;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 战旗菜单类型注册
 */
public class BannerMenuType {

    public static final DeferredRegister<MenuType<?>> MENUS = 
        DeferredRegister.create(Registries.MENU, CallOfWar.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<BannerMenu>> BANNER_MENU = 
        MENUS.register("banner_menu", () -> IMenuTypeExtension.create(BannerMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
