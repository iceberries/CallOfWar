package com.ice_berry.callofwar.banner.gui;

import com.ice_berry.callofwar.CallOfWar;
import com.ice_berry.callofwar.banner.team.TargetFilterMode;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 战旗配置屏幕
 */
public class BannerScreen extends AbstractContainerScreen<BannerMenu> {

    private TargetFilterMode selectedMode;

    public BannerScreen(BannerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 133;
        this.selectedMode = menu.getFilterMode();
    }

    @Override
    protected void init() {
        super.init();
        
        int centerX = this.leftPos + this.imageWidth / 2;
        
        // 筛选模式按钮
        addRenderableWidget(Button.builder(
            Component.literal(getModeButtonText()),
            button -> cycleFilterMode()
        ).bounds(centerX - 75, this.topPos + 35, 150, 20).build());

        // 确认按钮
        addRenderableWidget(Button.builder(
            Component.translatable("gui.callofwar.banner.confirm"),
            button -> onClose()
        ).bounds(centerX - 75, this.topPos + 90, 150, 20).build());
    }

    private void cycleFilterMode() {
        TargetFilterMode[] modes = TargetFilterMode.values();
        int currentIndex = selectedMode.ordinal();
        selectedMode = modes[(currentIndex + 1) % modes.length];
        menu.setFilterMode(selectedMode);
        
        // 更新按钮文本
        this.children().stream()
            .filter(w -> w instanceof Button)
            .map(w -> (Button) w)
            .findFirst()
            .ifPresent(btn -> btn.setMessage(Component.literal(getModeButtonText())));
    }

    private String getModeButtonText() {
        return Component.translatable("gui.callofwar.banner.filter_mode").getString() + 
               ": " + Component.translatable("gui.callofwar.banner.filter." + selectedMode.getId()).getString();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // 渲染半透明背景
        guiGraphics.fill(this.leftPos, this.topPos, 
            this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 
            0xCC000000);
        
        // 边框
        guiGraphics.renderOutline(this.leftPos, this.topPos, this.imageWidth, this.imageHeight, 0xFF555555);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 先渲染背景
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        // 渲染标题
        guiGraphics.drawCenteredString(this.font, this.title, this.leftPos + this.imageWidth / 2, this.topPos + 6, 0xFFFFFF);
        
        // 渲染团队信息
        String teamInfo = Component.translatable("gui.callofwar.banner.team", menu.getTeamName()).getString();
        guiGraphics.drawCenteredString(this.font, teamInfo, this.leftPos + this.imageWidth / 2, this.topPos + 65, 0xAAAAAA);
        
        // 渲染提示
        if (this.hoveredSlot != null) {
            renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }

    @Override
    public void onClose() {
        menu.applyChanges();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
