package net.mattseq.speech_bubbles.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

public class BubbleRenderer {
    public static void renderBubble(PoseStack poseStack, int width, int height, ResourceLocation texture, int padding) {
        var consumer = Tesselator.getInstance().getBuilder();
        consumer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.enablePolygonOffset();
        RenderSystem.polygonOffset(3.0F, 3.0F);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        int j = height - 1;
        int baseX = width / 2;
        int baseY = (-height - j * 7) - j;

        // LEFT
        blit(poseStack, consumer, -baseX - 3 - padding, baseY - padding, 5, 5, 0.0F, 0.0F, 5, 5, 32, 32); // TOP
        blit(poseStack, consumer, -baseX - 3 - padding, baseY + 5 - padding, 5, height + j * 8 + (padding * 2), 0.0F, 6.0F, 5, 1, 32, 32); // MID
        blit(poseStack, consumer, -baseX - 3 - padding, 5 + padding, 5, 5, 0.0F, 8.0F, 5, 5, 32, 32); // BOTTOM

        // MID
        blit(poseStack, consumer, -baseX + 2 - padding, baseY - padding, width - 4 + (padding * 2), 5, 6.0F, 0.0F, 5, 5, 32, 32); // TOP
        blit(poseStack, consumer, -baseX + 2 - padding, baseY + 5 - padding, width - 4 + (padding * 2), height + j * 8 + (padding * 2), 6.0F, 6.0F, 5, 1, 32, 32); // MID
        blit(poseStack, consumer, -baseX + 2 - padding, 5 + padding, width - 4 + (padding * 2), 5, 6.0F, 8.0F, 5, 5, 32, 32); // BOTTOM

        // RIGHT
        blit(poseStack, consumer, baseX - 3 + padding, baseY - padding, 5, 5, 12.0F, 0.0F, 5, 5, 32, 32); // TOP
        blit(poseStack, consumer, baseX - 3 + padding, baseY + 5 - padding, 5, height + j * 8 + (padding * 2), 12.0F, 6.0F, 5, 1, 32, 32); // MID
        blit(poseStack, consumer, baseX - 3 + padding, 5 + padding, 5, 5, 12.0F, 8.0F, 5, 5, 32, 32); // BOTTOM

        BufferUploader.drawWithShader(consumer.end());
        RenderSystem.polygonOffset(0.0F, 0.0F);
        RenderSystem.disablePolygonOffset();
        RenderSystem.disableBlend();
    }

    private static void blit(PoseStack poseStack, VertexConsumer consumer, int x, int y, int width, int height,
                             float uOffset, float vOffset, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        var matrix = poseStack.last().pose();

        var x2 = x + width;
        var y2 = y + height;
        var minU = uOffset / textureWidth;
        var maxU = (uOffset + uWidth) / textureWidth;
        var minV = vOffset / textureHeight;
        var maxV = (vOffset + vHeight) / textureHeight;

        consumer.vertex(matrix, (float) x, (float) y, 0f)
                .uv(minU, minV)
                .color(-1)
                .endVertex();

        consumer.vertex(matrix, (float) x, (float) y2, 0f)
                .uv(minU, maxV)
                .color(-1)
                .endVertex();

        consumer.vertex(matrix, (float) x2, (float) y2, 0f)
                .uv(maxU, maxV)
                .color(-1)
                .endVertex();

        consumer.vertex(matrix, (float) x2, (float) y, 0f)
                .uv(maxU, minV)
                .color(-1)
                .endVertex();
    }
}
