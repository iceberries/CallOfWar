package com.ice_berry.callofwar.banner.gui;

import com.ice_berry.callofwar.banner.BannerBlockEntity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.MenuProvider;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

/**
 * 战旗菜单提供者
 */
public class BannerMenuProvider implements MenuProvider {

    private final BannerBlockEntity blockEntity;

    public BannerMenuProvider(BannerBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.callofwar.banner");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new BannerMenu(containerId, playerInventory, blockEntity);
    }
}
