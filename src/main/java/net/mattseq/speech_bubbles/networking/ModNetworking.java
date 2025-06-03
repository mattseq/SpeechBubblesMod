package net.mattseq.speech_bubbles.networking;

import net.mattseq.speech_bubbles.SpeechBubblesMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetworking {
    private static final String PROTOCOL_VERSION = "1.0";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(SpeechBubblesMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        // Client → Server
        CHANNEL.registerMessage(
                packetId++,
                SpeechPacketC2S.class,
                SpeechPacketC2S::encode,
                SpeechPacketC2S::decode,
                SpeechPacketC2S::handle
        );

        // Server → Client
        CHANNEL.registerMessage(
                packetId++,
                SpeechPacketS2C.class,
                SpeechPacketS2C::encode,
                SpeechPacketS2C::decode,
                SpeechPacketS2C::handle
        );
    }
}
