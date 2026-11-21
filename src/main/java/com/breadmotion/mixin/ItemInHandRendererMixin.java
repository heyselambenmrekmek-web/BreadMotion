package com.breadmotion.mixin;

import com.breadmotion.anim.BreadAnimator;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Inject(method = "renderArmWithItem", at = @At("HEAD"))
    private void breadmotion$begin(AbstractClientPlayer player, float partialTicks, float pitch,
                                   InteractionHand hand, float swingProgress, ItemStack stack,
                                   float equippedProgress, PoseStack poseStack,
                                   SubmitNodeCollector collector, int light, CallbackInfo ci) {
        poseStack.pushPose();
        BreadAnimator.apply(player, partialTicks, hand, swingProgress, stack, equippedProgress, poseStack);
    }

    @Inject(method = "renderArmWithItem", at = @At("RETURN"))
    private void breadmotion$end(AbstractClientPlayer player, float partialTicks, float pitch,
                                 InteractionHand hand, float swingProgress, ItemStack stack,
                                 float equippedProgress, PoseStack poseStack,
                                 SubmitNodeCollector collector, int light, CallbackInfo ci) {
        poseStack.popPose();
    }
}
