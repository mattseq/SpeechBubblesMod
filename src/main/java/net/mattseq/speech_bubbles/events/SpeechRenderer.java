package net.mattseq.speech_bubbles.events;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.mattseq.speech_bubbles.SpeechBubblesMod;
import net.mattseq.speech_bubbles.SpeechRenderTracker;
import net.mattseq.speech_bubbles.util.BubbleRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class SpeechRenderer {
    private static final ResourceLocation BUBBLE_TEXTURE = ResourceLocation.fromNamespaceAndPath(SpeechBubblesMod.MODID, "textures/gui/speech_bubble.png");


    @SubscribeEvent
    public static void onRenderWorldStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            PoseStack poseStack = event.getPoseStack();
            Minecraft mc = Minecraft.getInstance();
            EntityRenderDispatcher renderManager = mc.getEntityRenderDispatcher();
            Font fontRenderer = mc.font;
            MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

            int maxTextWidth = 200;
            int lineHeight = fontRenderer.lineHeight;

            double camX = renderManager.camera.getPosition().x;
            double camY = renderManager.camera.getPosition().y;
            double camZ = renderManager.camera.getPosition().z;

            long now = System.currentTimeMillis();
            Map<UUID, SpeechRenderTracker.SpeechEntry> entries = SpeechRenderTracker.getEntries();

            for (Map.Entry<UUID, SpeechRenderTracker.SpeechEntry> entry : entries.entrySet()) {
                SpeechRenderTracker.SpeechEntry speech = entry.getValue();
                if (now - speech.timestamp > speech.duration) continue;
                if (Objects.equals(speech.text, "")) continue;

                Player player = mc.level.getPlayerByUUID(entry.getKey());
                if (player == null || player.isInvisible()) continue;

                float partialTicks = event.getPartialTick();
                double x = Mth.lerp(partialTicks, player.xOld, player.getX()) - camX;
                double y = Mth.lerp(partialTicks, player.yOld, player.getY()) - camY + player.getBbHeight() + 1;
                double z = Mth.lerp(partialTicks, player.zOld, player.getZ()) - camZ;

                float progress = (now - speech.timestamp) / (float) speech.duration;
//                float alpha = 1.0f - progress;
//                float scale = 1.0f + 0.2f * (1.0f - progress);
                float alpha = 1f;
                float scale = 1.2f;

                poseStack.pushPose();

                poseStack.translate(x, y, z);

                poseStack.mulPose(renderManager.cameraOrientation());
                poseStack.scale(-0.025F * scale, -0.025F * scale, 0.025F * scale);

                RenderSystem.disableDepthTest();

                Color color = new Color(0, 0, 0, alpha);

                // Split message into lines
                List<FormattedCharSequence> lines = fontRenderer.split(Component.literal(speech.text), maxTextWidth);

                int textWidth = 0;

                for (FormattedCharSequence line : lines)
                    textWidth = Math.max(textWidth, fontRenderer.width(line));

                // RENDER SPEECH BUBBLE
                int padding = 1;
                int bubbleWidth = textWidth + padding*2;
                int lineNumber = lines.size();

                int j = lineNumber - 1;
                int baseY = (-lineNumber - j * 7) - j;

                // Call render from BubbleRenderer
                BubbleRenderer.renderBubble(poseStack, bubbleWidth, lines.size(), BUBBLE_TEXTURE, padding);


                if (color.getAlpha() > 25) {
                    for (int i = 0; i < lines.size(); i++) {
                        fontRenderer.drawInBatch(
                                lines.get(i),
                                -fontRenderer.width(lines.get(i)) / 2f,
                                baseY + padding + i * lineHeight,
                                color.getRGB(),
                                false,
                                poseStack.last().pose(),
                                bufferSource,
                                Font.DisplayMode.NORMAL,
                                0,
                                15728880
                        );
                    }
                }


                RenderSystem.enableDepthTest();

                RenderSystem.disableBlend();

                poseStack.popPose();
            }
        }
    }
}
