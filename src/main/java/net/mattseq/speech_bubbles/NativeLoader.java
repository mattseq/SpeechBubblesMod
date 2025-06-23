package net.mattseq.speech_bubbles;

import java.io.*;

public class NativeLoader {
    public static void loadDLL() throws IOException {
        String dllName = "libvosk.dll";
        InputStream in = NativeLoader.class.getResourceAsStream("/assets/speech_bubbles/natives/" + dllName);

        if (in == null) {
            throw new FileNotFoundException(dllName + " not found inside mod resources: /assets/speech_bubbles/natives/");
        }

        File tempDll = File.createTempFile("libvosk", ".dll");
        tempDll.deleteOnExit();

        try (FileOutputStream out = new FileOutputStream(tempDll)) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            throw new IOException("Failed to extract DLL to temporary location", e);
        }

        try {
            System.load(tempDll.getAbsolutePath());
        } catch (UnsatisfiedLinkError e) {
            throw new RuntimeException("Failed to load native library: " + tempDll.getAbsolutePath(), e);
        }
    }
}
