package net.mattseq.speech_bubbles;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public class SpeechRenderTracker {
    public static class SpeechEntry {
        public final String text;
        public final long timestamp;
        public final long duration;

        public SpeechEntry(String text, long timestamp, long duration) {
            this.text = text;
            this.timestamp = timestamp;
            this.duration = duration;
        }
    }

    private static final Map<UUID, SpeechEntry> SPEECH_ENTRIES = new ConcurrentHashMap<>();

    public static void setSpeech(UUID uuid, String text) {
        SPEECH_ENTRIES.put(uuid, new SpeechEntry(text, System.currentTimeMillis(), 3000));
        SpeechBubblesMod.LOGGER.debug(SPEECH_ENTRIES.toString());
        SpeechBubblesMod.LOGGER.debug(SPEECH_ENTRIES.get(uuid).text);
    }

    public static Map<UUID, SpeechEntry> getEntries() {
        return SPEECH_ENTRIES;
    }
}
