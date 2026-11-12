package com.mcking.stonespeaker.client;

import com.mcking.stonespeaker.SpeakerBlockEntity;
import com.mcking.stonespeaker.StoneSpeaker;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** 讲解石插了 U 盘时，在方块上方渲染一个 U 盘模型。 */
public class SpeakerRenderer implements BlockEntityRenderer<SpeakerBlockEntity> {
    public SpeakerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SpeakerBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        if (!be.hasTracks()) {
            return;
        }
        pose.pushPose();
        // U 盘竖直插在方块顶部，稍微朝向玩家
        pose.translate(0.5, 0.91, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(45.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(90.0F));
        pose.scale(0.4F, 0.4F, 0.4F);
        pose.translate(-0.406F, -0.5F, -0.5F);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                new ItemStack(StoneSpeaker.USB_DRIVE.get()),
                ItemDisplayContext.FIXED, packedLight, packedOverlay, pose, buffer, be.getLevel(), 0);
        pose.popPose();
    }
}
