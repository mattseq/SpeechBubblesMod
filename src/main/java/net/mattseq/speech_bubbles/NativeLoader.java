package net.mattseq.speech_bubbles;

import java.io.*;

public class NativeLoader {
    public static void loadVoskDLL() {
        try {
            // Extract from inside the jar
            InputStream in = NativeLoader.class.getResourceAsStream("/natives/libvosk.dll");
            if (in == null) {
                throw new IOException("libvosk.dll not found in resources");
            }

            // Write to temp file
            File tempFile = File.createTempFile("libvosk", ".dll");
            tempFile.deleteOnExit();

            try (OutputStream out = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            // Load from temp location
            System.load(tempFile.getAbsolutePath());
            SpeechBubblesMod.LOGGER.info("Successfully loaded libvosk.dll");

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load libvosk.dll", e);
        }
    }
}
