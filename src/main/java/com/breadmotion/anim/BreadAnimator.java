package com.breadmotion.anim;

import com.breadmotion.BreadConfig;
import com.breadmotion.BreadMotionClient;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;

/**
 * BreadMotion'ın tüm animasyon matematiği burada.
 * Vanilla'nın el çiziminden ÖNCE pose stack'e ek dönüşüm bindirir (pivot = elin durduğu nokta).
 */
public final class BreadAnimator {
    // Vanilla'nın eli/eşyayı yerleştirdiği yaklaşık nokta (kamera uzayı: +x sağ, +y yukarı, -z ileri)
    private static final float PX = 0.56f;
    private static final float PY = -0.52f;
    private static final float PZ = -0.72f;

    // equip yönü takibi (0 = ana el, 1 = yan el)
    private static final float[] lastEquip = new float[2];
    private static final boolean[] rising = {true, true};

    // sway için kendi tick'imizde tuttuğumuz kamera ve "geriden gelen" kamera değerleri
    private static boolean swayReady = false;
    private static float prevCurYaw, curYaw, prevLagYaw, lagYaw;
    private static float prevCurPitch, curPitch, prevLagPitch, lagPitch;

    private BreadAnimator() {}

    /** Her client tick'inde çağrılır: kamerayı geriden takip eden yaylı değeri günceller. */
    public static void clientTick(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null) {
            swayReady = false;
            return;
        }
        float yaw = p.getYRot();
        float pitch = p.getXRot();
        if (!swayReady) {
            prevCurYaw = curYaw = prevLagYaw = lagYaw = yaw;
            prevCurPitch = curPitch = prevLagPitch = lagPitch = pitch;
            swayReady = true;
            return;
        }
        prevCurYaw = curYaw;
        prevCurPitch = curPitch;
        prevLagYaw = lagYaw;
        prevLagPitch = lagPitch;
        curYaw = yaw;
        curPitch = pitch;
        lagYaw += Mth.wrapDegrees(yaw - lagYaw) * 0.4f;
        lagPitch += (pitch - lagPitch) * 0.4f;
    }

    public static void apply(AbstractClientPlayer player, float pt, InteractionHand hand, float swing,
                             ItemStack stack, float equip, PoseStack ps) {
        BreadConfig c = BreadMotionClient.config;
        if (c == null || !c.enabled || c.intensity <= 0f) return;
        if (player.isScoping() || stack.is(Items.FILLED_MAP)) return;

        final float k = c.intensity;
        final int h = hand.ordinal();
        final HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        final float side = arm == HumanoidArm.RIGHT ? 1f : -1f;
        // Ekmek elindeyken her şey biraz daha "hamur" gibi
        final float squish = stack.is(Items.BREAD) ? 1.5f : 1f;

        float sx = 1f, sy = 1f, sz = 1f;
        float roll = 0f, tiltX = 0f;
        float dx = 0f, dy = 0f, dz = 0f;

        // ---------- 1) Elastik sallanma (sway) ----------
        if (c.sway && swayReady) {
            float cy = Mth.rotLerp(pt, prevCurYaw, curYaw);
            float ly = Mth.rotLerp(pt, prevLagYaw, lagYaw);
            float dYaw = Mth.clamp(Mth.wrapDegrees(cy - ly), -40f, 40f);
            float dPitch = Mth.clamp(Mth.lerp(pt, prevCurPitch, curPitch) - Mth.lerp(pt, prevLagPitch, lagPitch), -40f, 40f);
            ps.mulPose(Axis.XP.rotationDegrees(dPitch * 0.2f * k));
            ps.mulPose(Axis.YP.rotationDegrees(dYaw * 0.2f * k));
            roll += Mth.clamp(-dYaw * 0.2f, -10f, 10f) * k;
        }

        // ---------- 2) Yaylı eşya değiştirme (equip bounce) ----------
        if (c.equipBounce) {
            if (equip != lastEquip[h]) {
                rising[h] = equip < lastEquip[h];
                lastEquip[h] = equip;
            }
            float shaped;
            if (rising[h]) {
                // yeni eşya: aşağıdan fırlar, biraz aşırı yükselir, yerine oturur
                shaped = 1f - easeOutBack(1f - equip);
            } else {
                // eski eşya: yavaş başlayıp hızlanarak düşer
                shaped = easeInCubic(equip);
            }
            shaped = equip + (shaped - equip) * k;
            dy += (equip - shaped) * 0.6f;      // vanilla: y = -0.52 + equip * -0.6
            tiltX += (shaped - equip) * 30f;    // fırlarken hafif devrilme
        }

        // ---------- 3) Hamur yumruğu (squash & stretch) ----------
        if (c.doughPunch && swing > 0f) {
            float kk = k * squish;
            float stretch;
            if (swing < 0.35f) {
                stretch = easeOutQuad(swing / 0.35f);
            } else {
                float u = swing - 0.35f;
                // çarpma sonrası sönümlenen jöle titreşimi
                stretch = (float) (Math.exp(-4.5 * u) * Math.cos(13.0 * u));
            }
            sz *= 1f + 0.22f * kk * stretch;
            float thin = 1f - 0.10f * kk * stretch;
            sx *= thin;
            sy *= thin;
            roll += -side * 7f * kk * (float) Math.sin(swing * Math.PI * 2.0);

            if (stack.isEmpty()) {
                float lunge = (float) Math.sin(swing * Math.PI);
                dz -= 0.14f * kk * lunge;
                dx -= side * 0.04f * kk * lunge;
            }
        }

        // ---------- 4) Kabaran hamur nefesi (boştayken) ----------
        if (c.breathing && swing <= 0f && !player.isUsingItem()) {
            float t = player.tickCount + pt;
            float phase = t * 0.08f + h * 1.7f;
            dy += (float) Math.sin(phase) * 0.0035f * k;
            float br = 1f + (float) Math.sin(phase + 0.6f) * 0.006f * k;
            sy *= br;
            sx *= 2f - br;
        }

        // ---------- 5) Lokma lokma yeme/içme ----------
        if (c.nibble && player.isUsingItem() && player.getUsedItemHand() == hand) {
            ItemUseAnimation anim = stack.getUseAnimation();
            if (anim == ItemUseAnimation.EAT || anim == ItemUseAnimation.DRINK) {
                float f = player.getUseItemRemainingTicks() - pt + 1.0f;
                float chew = (float) Math.sin(f * 1.4f);
                float kk = k * squish;
                sx *= 1f + 0.05f * kk * chew;
                sy *= 1f - 0.05f * kk * chew;
                roll += chew * 4f * kk * side;
            }
        }

        // ---------- Uygula: pivota git, döndür, ölçekle, geri dön ----------
        ps.translate(side * PX + dx, PY + dy, PZ + dz);
        if (tiltX != 0f) ps.mulPose(Axis.XP.rotationDegrees(tiltX));
        if (roll != 0f) ps.mulPose(Axis.ZP.rotationDegrees(roll));
        ps.scale(sx, sy, sz);
        ps.translate(-side * PX, -PY, -PZ);
    }

    private static float easeOutBack(float t) {
        final float c1 = 1.70158f;
        final float c3 = c1 + 1f;
        float u = t - 1f;
        return 1f + c3 * u * u * u + c1 * u * u;
    }

    private static float easeInCubic(float t) {
        return t * t * t;
    }

    private static float easeOutQuad(float t) {
        float u = 1f - t;
        return 1f - u * u;
    }
}
