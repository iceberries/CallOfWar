package com.ice_berry.callofwar.banner;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/**
 * 战旗物品渲染器
 * 用于在物品栏、手中、展示框等位置渲染战旗
 */
public class BannerItemRenderer implements IClientItemExtensions {

    private final BannerType bannerType;
    private final DyeColor color;
    private BlockEntityWithoutLevelRenderer renderer;
    
    public BannerItemRenderer(BannerType bannerType, DyeColor color) {
        this.bannerType = bannerType;
        this.color = color;
    }

    @Override
    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        if (renderer == null) {
            renderer = new BlockEntityWithoutLevelRenderer(
                Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels()
            ) {
                @Override
                public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, 
                                         PoseStack poseStack, MultiBufferSource bufferSource, 
                                         int packedLight, int packedOverlay) {
                    renderBannerItem(poseStack, bufferSource, packedLight, packedOverlay);
                }
            };
        }
        return renderer;
    }
    
    private void renderBannerItem(PoseStack poseStack, MultiBufferSource bufferSource, 
                                  int packedLight, int packedOverlay) {
        // 获取模型 - 使用原版旗帜模型层
        ModelPart modelPart = Minecraft.getInstance().getEntityModels().bakeLayer(BannerModelLayers.BANNER);
        ModelPart flag = modelPart.getChild("flag");
        ModelPart pole = modelPart.getChild("pole");
        ModelPart bar = modelPart.getChild("bar");
        
        poseStack.pushPose();
        
        // 物品渲染的初始位置
        poseStack.translate(0.5F, 0.5F, 0.5F);
        
        // 应用缩放（与方块渲染器相同）
        poseStack.pushPose();
        poseStack.scale(0.6666667F, -0.6666667F, -0.6666667F);
        
        // 渲染旗杆和横杆
        Material baseMaterial = net.minecraft.client.resources.model.ModelBakery.BANNER_BASE;
        var vertexConsumer = baseMaterial.buffer(bufferSource, RenderType::entitySolid);
        pole.render(poseStack, vertexConsumer, packedLight, packedOverlay);
        bar.render(poseStack, vertexConsumer, packedLight, packedOverlay);
        
        // 旗帜静态显示（物品形态无动画）
        flag.xRot = 0.0F;
        flag.y = -32.0F;
        
        // 渲染旗帜带颜色
        int colorValue = color.getTextureDiffuseColor();
        flag.render(poseStack, baseMaterial.buffer(bufferSource, RenderType::entitySolid), 
            packedLight, packedOverlay);
        flag.render(poseStack, 
            net.minecraft.client.renderer.Sheets.BANNER_BASE.buffer(bufferSource, RenderType::entityNoOutline), 
            packedLight, packedOverlay, colorValue);
        
        poseStack.popPose();
        poseStack.popPose();
    }
}
