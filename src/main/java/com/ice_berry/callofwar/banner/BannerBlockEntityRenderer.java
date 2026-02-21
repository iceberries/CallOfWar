package com.ice_berry.callofwar.banner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

/**
 * 战旗方块实体渲染器
 * 匹配原版旗帜渲染逻辑
 */
public class BannerBlockEntityRenderer implements BlockEntityRenderer<BannerBlockEntity> {

    private static final float SCALE = 0.6666667F;
    
    // 模型部件 - public 供物品渲染器使用
    public final ModelPart flag;
    public final ModelPart pole;
    public final ModelPart bar;

    public BannerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart modelPart = context.bakeLayer(BannerModelLayers.BANNER);
        this.flag = modelPart.getChild("flag");
        this.pole = modelPart.getChild("pole");
        this.bar = modelPart.getChild("bar");
    }

    @Override
    public void render(BannerBlockEntity blockEntity, float partialTick, PoseStack poseStack, 
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        
        boolean isItemRenderer = blockEntity.getLevel() == null;
        poseStack.pushPose();
        
        long gameTime;
        if (isItemRenderer) {
            // 物品渲染模式
            gameTime = 0L;
            poseStack.translate(0.5F, 0.5F, 0.5F);
            this.pole.visible = true;
        } else {
            // 方块渲染模式
            gameTime = blockEntity.getLevel().getGameTime();
            BlockState blockState = blockEntity.getBlockState();
            
            if (blockState.getBlock() instanceof BannerBlock) {
                // 站立式旗帜
                poseStack.translate(0.5F, 0.5F, 0.5F);
                float rotation = -net.minecraft.world.level.block.state.properties.RotationSegment.convertToDegrees(
                    blockState.getValue(BlockStateProperties.ROTATION_16));
                poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
                this.pole.visible = true;
            } else if (blockState.getBlock() instanceof WallBannerBlock) {
                // 墙面旗帜
                poseStack.translate(0.5F, -0.16666667F, 0.5F);
                float rotation = -blockState.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot();
                poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
                poseStack.translate(0.0F, -0.3125F, -0.4375F);
                this.pole.visible = false;
            } else {
                // 默认处理
                poseStack.translate(0.5F, 0.5F, 0.5F);
                this.pole.visible = true;
            }
        }

        // 应用缩放和渲染
        poseStack.pushPose();
        poseStack.scale(SCALE, -SCALE, -SCALE);
        
        // 获取基础颜色
        DyeColor baseColor = getBaseColor(blockEntity);
        
        // 渲染旗杆和横杆
        Material baseMaterial = net.minecraft.client.resources.model.ModelBakery.BANNER_BASE;
        VertexConsumer vertexConsumer = baseMaterial.buffer(bufferSource, RenderType::entitySolid);
        this.pole.render(poseStack, vertexConsumer, packedLight, packedOverlay);
        this.bar.render(poseStack, vertexConsumer, packedLight, packedOverlay);
        
        // 旗帜摆动动画
        BlockPos blockPos = blockEntity.getBlockPos();
        float swingProgress = ((float)Math.floorMod(
            (long)(blockPos.getX() * 7 + blockPos.getY() * 9 + blockPos.getZ() * 13) + gameTime, 100L) 
            + partialTick) / 100.0F;
        this.flag.xRot = (-0.0125F + 0.01F * Mth.cos((float)(Math.PI * 2) * swingProgress)) * (float)Math.PI;
        this.flag.y = -32.0F;
        
        // 渲染旗帜（带颜色）
        renderFlagWithColor(poseStack, bufferSource, packedLight, packedOverlay, baseColor);
        
        poseStack.popPose();
        poseStack.popPose();
    }
    
    /**
     * 获取战旗的基础颜色
     */
    private DyeColor getBaseColor(BannerBlockEntity blockEntity) {
        BlockState state = blockEntity.getBlockState();
        if (state.getBlock() instanceof COWAbstractBannerBlock bannerBlock) {
            return bannerBlock.getColor();
        }
        return DyeColor.WHITE;
    }
    
    /**
     * 渲染带颜色的旗帜
     */
    private void renderFlagWithColor(PoseStack poseStack, MultiBufferSource bufferSource, 
                                     int packedLight, int packedOverlay, DyeColor color) {
        // 使用原版的旗帜基础材质
        Material flagMaterial = net.minecraft.client.resources.model.ModelBakery.BANNER_BASE;
        
        // 渲染旗帜基础层
        this.flag.render(poseStack, flagMaterial.buffer(bufferSource, RenderType::entitySolid), 
            packedLight, packedOverlay);
        
        // 渲染颜色层
        int colorValue = color.getTextureDiffuseColor();
        this.flag.render(poseStack, 
            net.minecraft.client.renderer.Sheets.BANNER_BASE.buffer(bufferSource, RenderType::entityNoOutline), 
            packedLight, packedOverlay, colorValue);
    }

    /**
     * 创建旗帜模型层定义
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        // 旗帜主体
        partDefinition.addOrReplaceChild("flag", 
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-10.0F, 0.0F, -2.0F, 20.0F, 40.0F, 1.0F),
            PartPose.ZERO);

        // 旗杆
        partDefinition.addOrReplaceChild("pole", 
            CubeListBuilder.create()
                .texOffs(44, 0)
                .addBox(-1.0F, -30.0F, -1.0F, 2.0F, 42.0F, 2.0F),
            PartPose.ZERO);

        // 横杆
        partDefinition.addOrReplaceChild("bar", 
            CubeListBuilder.create()
                .texOffs(0, 42)
                .addBox(-10.0F, -32.0F, -1.0F, 20.0F, 2.0F, 2.0F),
            PartPose.ZERO);

        return LayerDefinition.create(meshDefinition, 64, 64);
    }
    
    @Override
    public AABB getRenderBoundingBox(BannerBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        boolean standing = blockEntity.getBlockState().getBlock() instanceof BannerBlock;
        return AABB.encapsulatingFullBlocks(pos, standing ? pos.above() : pos);
    }
}
