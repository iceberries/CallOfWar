package com.ice_berry.callofwar.banner.forge;

import com.ice_berry.callofwar.CallOfWar;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 战旗工坊屏幕
 * 交互设计参考原版织布机
 */
public class BannerForgeScreen extends AbstractContainerScreen<BannerForgeMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        CallOfWar.MODID, "textures/gui/banner_forge.png");
    
    // 预览区域位置（相对于GUI左上角）
    private static final int PREVIEW_X = 60;
    private static final int PREVIEW_Y = 17;
    private static final int PREVIEW_WIDTH = 56;
    private static final int PREVIEW_HEIGHT = 56;

    public BannerForgeScreen(BannerForgeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 221;  // 额外高度用于输出槽
    }

    @Override
    protected void init() {
        super.init();
        // 标题居中
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        
        // 绘制主背景
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        
        // 绘制预览区域背景（稍暗的背景）
        graphics.fill(x + PREVIEW_X, y + PREVIEW_Y, 
                      x + PREVIEW_X + PREVIEW_WIDTH, y + PREVIEW_Y + PREVIEW_HEIGHT, 
                      0x40101010);
        
        // TODO: 渲染战旗预览
        renderBannerPreview(graphics, x + PREVIEW_X + PREVIEW_WIDTH / 2, y + PREVIEW_Y + PREVIEW_HEIGHT / 2);
    }

    /**
     * 渲染战旗预览
     */
    private void renderBannerPreview(GuiGraphics graphics, int centerX, int centerY) {
        BannerForgeBlockEntity blockEntity = this.menu.getBlockEntity();
        if (blockEntity == null) return;
        
        // TODO: 实现旗帜预览渲染
        // 1. 获取输入槽的战旗物品
        // 2. 渲染旗帜模型到预览区域
        // 3. 显示当前属性和效果
        
        // 占位符：显示提示文字
        if (blockEntity.getItemHandler().getStackInSlot(BannerForgeMenu.INPUT_SLOT).isEmpty()) {
            Component hint = Component.translatable("gui.callofwar.banner_forge.insert_banner");
            graphics.drawCenteredString(this.font, hint, centerX, centerY - 4, 0x808080);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // 绘制标题
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        
        // 绘制玩家背包标签
        graphics.drawString(this.font, this.playerInventoryTitle, 8, 128, 0x404040, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);
        
        // 检查是否悬停在预览区域
        int relX = mouseX - leftPos;
        int relY = mouseY - topPos;
        
        if (relX >= PREVIEW_X && relX < PREVIEW_X + PREVIEW_WIDTH &&
            relY >= PREVIEW_Y && relY < PREVIEW_Y + PREVIEW_HEIGHT) {
            renderPreviewTooltip(graphics, mouseX, mouseY);
        }
    }

    /**
     * 渲染预览区域提示
     */
    private void renderPreviewTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        BannerForgeBlockEntity blockEntity = this.menu.getBlockEntity();
        if (blockEntity == null) return;
        
        // TODO: 显示战旗属性和效果提示
    }
}
