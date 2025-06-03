package net.mattseq.speech_bubbles.events;

import net.mattseq.speech_bubbles.SpeechRecognizer;
import net.mattseq.speech_bubbles.ModKeyBindings;
import net.mattseq.speech_bubbles.SpeechBubblesMod;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

import javax.sound.sampled.LineUnavailableException;

@Mod.EventBusSubscriber(modid = SpeechBubblesMod.MODID, value = Dist.CLIENT)
public class PushToTalkKey {

    private static boolean wasPressed = false;

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        boolean isPressed = ModKeyBindings.PUSH_TO_TALK_KEY.isDown();

        // Key was just pressed
        if (isPressed && !wasPressed) {
//            Main.startListening();
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                try {
                    SpeechRecognizer.startListening();
                } catch (LineUnavailableException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        // Key was just released
        if (!isPressed && wasPressed) {
//            Main.stopListening();
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SpeechRecognizer.stopListening());
//            SpeechBubblesMod.LOGGER.debug("Transcript: {}", Main.getTranscript());
        }

        wasPressed = isPressed;
    }
}
