package com.ice_berry.callofwar.banner.gui;

import java.util.UUID;

import com.ice_berry.callofwar.banner.BannerBlockEntity;
import com.ice_berry.callofwar.banner.BannerType;
import com.ice_berry.callofwar.banner.team.TargetFilterMode;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * 战旗配置菜单
 * 使用 ContainerData 同步数据到客户端
 */
public class BannerMenu extends AbstractContainerMenu {

    private final BannerBlockEntity blockEntity;
    private final BannerType bannerType;
    private final ContainerData data;
    private String teamName;
    private UUID placerUUID;
    private BlockPos blockPos;

    public BannerMenu(int containerId, Inventory playerInventory, BannerBlockEntity blockEntity) {
        super(BannerMenuType.BANNER_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.bannerType = blockEntity.getBannerType();
        this.teamName = blockEntity.getTeamName();
        this.placerUUID = blockEntity.getPlacerUUID();
        this.blockPos = blockEntity.getBlockPos();
        
        // 使用 ContainerData 同步筛选模式索引
        this.data = new SimpleContainerData(1);
        this.data.set(0, blockEntity.getFilterMode().ordinal());
        addDataSlots(this.data);
    }

    public BannerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        super(BannerMenuType.BANNER_MENU.get(), containerId);
        
        // 从网络缓冲区读取同步数据
        this.blockPos = buf.readBlockPos();
        this.teamName = buf.readUtf();
        boolean hasPlacer = buf.readBoolean();
        this.placerUUID = hasPlacer ? buf.readUUID() : null;
        int filterModeOrdinal = buf.readInt();
        
        // 获取客户端 BlockEntity
        BlockEntity be = playerInventory.player.level().getBlockEntity(blockPos);
        if (be instanceof BannerBlockEntity bannerBE) {
            this.blockEntity = bannerBE;
            this.bannerType = bannerBE.getBannerType();
        } else {
            this.blockEntity = null;
            this.bannerType = null;
        }
        
        // 使用 ContainerData 同步筛选模式
        this.data = new SimpleContainerData(1);
        this.data.set(0, filterModeOrdinal);
        addDataSlots(this.data);
    }

    /**
     * 将数据写入网络缓冲区（服务端 -> 客户端）
     */
    public static void writeData(FriendlyByteBuf buf, BannerBlockEntity blockEntity) {
        buf.writeBlockPos(blockEntity.getBlockPos());
        buf.writeUtf(blockEntity.getTeamName());
        UUID placerUUID = blockEntity.getPlacerUUID();
        buf.writeBoolean(placerUUID != null);
        if (placerUUID != null) {
            buf.writeUUID(placerUUID);
        }
        buf.writeInt(blockEntity.getFilterMode().ordinal());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        // 使用服务端的 placerUUID 验证（不依赖客户端 BlockEntity）
        if (placerUUID == null) return false;
        return placerUUID.equals(player.getUUID());
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
     * 获取当前筛选模式（从同步数据获取）
     */
    public TargetFilterMode getFilterMode() {
        return TargetFilterMode.values()[data.get(0)];
    }

    /**
     * 设置筛选模式（更新同步数据）
     */
    public void setFilterMode(TargetFilterMode mode) {
        this.data.set(0, mode.ordinal());
    }

    /**
     * 获取团队名称（从服务端缓存）
     */
    public String getTeamName() {
        return teamName != null ? teamName : "No Team";
    }

    /**
     * 获取放置者UUID
     */
    public UUID getPlacerUUID() {
        return placerUUID;
    }

    /**
     * 获取战旗位置
     */
    public BlockPos getBlockPos() {
        return blockPos;
    }
}
