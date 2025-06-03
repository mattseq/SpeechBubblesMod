package net.mattseq.speech_bubbles.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SpeechPacketC2S {
    private final String message;

    public SpeechPacketC2S(String message) {
        this.message = message;
    }

    public static void encode(SpeechPacketC2S packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.message);
    }

    public static SpeechPacketC2S decode(FriendlyByteBuf buf) {
        return new SpeechPacketC2S(buf.readUtf());
    }

    public static void handle(SpeechPacketC2S packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null) {
                for (ServerPlayer player : sender.server.getPlayerList().getPlayers()) {
                    ModNetworking.CHANNEL.sendTo(new SpeechPacketS2C(player.getUUID(), packet.message),
                            player.connection.connection,
                            NetworkDirection.PLAY_TO_CLIENT);
                }
            }
        });
        context.setPacketHandled(true);
    }
}
