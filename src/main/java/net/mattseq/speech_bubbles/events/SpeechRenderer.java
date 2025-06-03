package net.mattseq.speech_bubbles.events;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mattseq.speech_bubbles.SpeechRenderTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;
import java.util.Map;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class SpeechRenderer {
    @SubscribeEvent
    public static void onRenderWorldStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            PoseStack poseStack = event.getPoseStack();
            Minecraft mc = Minecraft.getInstance();
            EntityRenderDispatcher renderManager = mc.getEntityRenderDispatcher();
            Font fontRenderer = mc.font;
            MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

            double camX = renderManager.camera.getPosition().x;
            double camY = renderManager.camera.getPosition().y;
            double camZ = renderManager.camera.getPosition().z;

            long now = System.currentTimeMillis();
            Map<UUID, SpeechRenderTracker.SpeechEntry> entries = SpeechRenderTracker.getEntries();

            for (Map.Entry<UUID, SpeechRenderTracker.SpeechEntry> entry : entries.entrySet()) {
                SpeechRenderTracker.SpeechEntry speech = entry.getValue();
                if (now - speech.timestamp > speech.duration) continue;

                Player player = mc.level.getPlayerByUUID(entry.getKey());
                if (player == null || player.isInvisible()) continue;

                float partialTicks = event.getPartialTick();
                double x = Mth.lerp(partialTicks, player.xOld, player.getX()) - camX;
                double y = Mth.lerp(partialTicks, player.yOld, player.getY()) - camY + player.getBbHeight() + 0.5;
                double z = Mth.lerp(partialTicks, player.zOld, player.getZ()) - camZ;

                float progress = (now - speech.timestamp) / (float) speech.duration;
                float alpha = 1.0f - progress;
                float scale = 2.0f + 0.2f * (1.0f - progress);

                poseStack.pushPose();

                // Get player view direction (unit vector)
                Vec3 look = player.getLookAngle();

                // Push the bubble slightly forward from the player's head
                double forwardOffset = 2; // Adjust this value as needed
                poseStack.translate(x + look.x * forwardOffset,
                        y + look.y * forwardOffset,
                        z + look.z * forwardOffset);

                poseStack.mulPose(renderManager.cameraOrientation());
                poseStack.scale(-0.025F * scale, -0.025F * scale, 0.025F * scale);

                RenderSystem.disableDepthTest();

                Color color = new Color(0, 0, 0, alpha);

                // render text (if statement prevents flicker of alpha at end)
                if (color.getAlpha() > 25) {
                    fontRenderer.drawInBatch(
                            speech.text,
                            -fontRenderer.width(speech.text) / 2f,
                            0,
                            color.getRGB(),
                            false,
                            poseStack.last().pose(),
                            bufferSource,
                            Font.DisplayMode.NORMAL,
                            0,
                            15728880
                    );
                }


                RenderSystem.enableDepthTest();
                poseStack.popPose();
            }

//            bufferSource.endBatch();
        }
    }
}
