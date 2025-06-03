package net.mattseq.speech_bubbles.networking;

import net.mattseq.speech_bubbles.SpeechRenderTracker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SpeechPacketS2C {
    private final UUID playerId;
    private final String message;

    public SpeechPacketS2C(UUID playerId, String message) {
        this.playerId = playerId;
        this.message = message;
    }

    public static void encode(SpeechPacketS2C packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.playerId);
        buf.writeUtf(packet.message);
    }

    public static SpeechPacketS2C decode(FriendlyByteBuf buf) {
        return new SpeechPacketS2C(buf.readUUID(), buf.readUtf());
    }

    public static void handle(SpeechPacketS2C packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            SpeechRenderTracker.setSpeech(packet.playerId, packet.message);
        });
        context.setPacketHandled(true);
    }
}
