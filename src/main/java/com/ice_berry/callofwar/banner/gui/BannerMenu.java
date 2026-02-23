package com.ice_berry.callofwar.banner.gui;

import com.ice_berry.callofwar.banner.BannerBlockEntity;
import com.ice_berry.callofwar.banner.BannerType;
import com.ice_berry.callofwar.banner.team.TargetFilterMode;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * 战旗配置菜单
 */
public class BannerMenu extends AbstractContainerMenu {

    private final BannerBlockEntity blockEntity;
    private final BannerType bannerType;
    private TargetFilterMode filterMode;

    public BannerMenu(int containerId, Inventory playerInventory, BannerBlockEntity blockEntity) {
        super(BannerMenuType.BANNER_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.bannerType = blockEntity.getBannerType();
        this.filterMode = blockEntity.getFilterMode();
    }

    public BannerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data));
    }

    private static BannerBlockEntity getBlockEntity(Inventory playerInventory, FriendlyByteBuf data) {
        BlockEntity be = playerInventory.player.level().getBlockEntity(data.readBlockPos());
        if (be instanceof BannerBlockEntity bannerBE) {
            return bannerBE;
        }
        throw new IllegalStateException("Block entity is not a BannerBlockEntity");
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        // 只有放置者本人才能操作战旗GUI
        if (blockEntity == null) return false;
        return blockEntity.isPlacer(player);
    }

    /**
     * 获取战旗方块实体
     */
    public BannerBlockEntity getBlockEntity() {
        return blockEntity;
    }

    /**
     * 获取战旗类型
     */
    public BannerType getBannerType() {
        return bannerType;
    }

    /**
     * 获取当前筛选模式
     */
    public TargetFilterMode getFilterMode() {
        return filterMode;
    }

    /**
     * 设置筛选模式
     */
    public void setFilterMode(TargetFilterMode mode) {
        this.filterMode = mode;
    }

    /**
     * 获取团队名称
     */
    public String getTeamName() {
        return blockEntity.getTeamName();
    }

    /**
     * 应用配置到战旗
     */
    public void applyChanges() {
        if (blockEntity != null) {
            blockEntity.setFilterMode(filterMode);
        }
    }
}
