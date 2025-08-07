package net.mattseq.speech_bubbles;

import com.sun.jna.Library;
import com.sun.jna.Native;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.*;

@OnlyIn(Dist.CLIENT)
public class NativeLoader {
    private static boolean loaded = false;

    // Interface to call SetDllDirectoryA from kernel32
    public interface Kernel32 extends Library {
        boolean SetDllDirectoryA(String path);
    }

    public static void loadOnce() {
        if (!loaded) {
            try {
                File nativeDll = new File("natives/libvosk.dll");

                // Set the DLL search path so Windows loads dependencies from the same folder
                File nativeFolder = nativeDll.getParentFile();
                Kernel32 kernel32 = Native.load("kernel32", Kernel32.class);
                kernel32.SetDllDirectoryA(nativeFolder.getAbsolutePath());

                System.load(nativeDll.getAbsolutePath());
                SpeechBubblesMod.LOGGER.debug("libvosk.dll loaded successfully");
                loaded = true;
            } catch (Exception e) {
                SpeechBubblesMod.LOGGER.debug("libvosk.dll not found in 'natives' folder. Please create 'natives' folder in this instance and download libvosk.dll from this mod's Modrinth/Curseforge page");
                throw new RuntimeException(e);
            }
        }
    }

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
