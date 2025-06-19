package net.mattseq.speech_bubbles;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = SpeechBubblesMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModKeyBindings {
    public static final KeyMapping PUSH_TO_TALK_KEY = new KeyMapping(
            "key.push_to_talk", // The translation key of the key binding's name
            InputConstants.Type.KEYSYM, // The type of the key binding, in this case, a keyboard key
            GLFW.GLFW_KEY_H, // The default key for the key binding
            "key.categories." + SpeechBubblesMod.MODID // The translation key of the key binding's category
    );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(PUSH_TO_TALK_KEY);
    }
}
