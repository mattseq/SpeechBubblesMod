package net.mattseq.speech_bubbles;

import net.mattseq.speech_bubbles.networking.ModNetworking;
import net.mattseq.speech_bubbles.networking.SpeechPacketC2S;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.*;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpeechRecognizer {

    private static final AudioFormat format = new AudioFormat(16000, 16, 1, true, false);

    public static boolean listening = false;
    private static TargetDataLine microphone;
    private static Model model;

    public static boolean modelLoaded = false;

    public static void initialize() {
        new Thread(() -> {
            try {
                // set Vosk log level to DEBUG (optional)
                LibVosk.setLogLevel(LogLevel.DEBUG);

                // load the Vosk model folder (downloaded separately)
                model = VoskModelLoader.loadModel();

                // get the microphone line
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

                if (!AudioSystem.isLineSupported(info)) {
                    System.err.println("Microphone with required format not supported");
                    System.exit(1);
                }

                microphone = (TargetDataLine) AudioSystem.getLine(info);

                modelLoaded = true;
            } catch (IOException | LineUnavailableException e) {
                throw new RuntimeException(e);
            }
        }, "VoskModelLoader").start();
    }

    public static void startListening() throws LineUnavailableException {
        if (!modelLoaded) {
            SpeechBubblesMod.LOGGER.debug("Model is still loading...");
            return;
        }

        microphone.open(format);
        microphone.start();
        listening = true;

        SpeechBubblesMod.LOGGER.debug("Start speaking...");

        Thread thread = new Thread(() -> {
            try (Recognizer recognizer = new Recognizer(model, 16000)) {
                byte[] buffer = new byte[4096];

                while (listening) {
                    int bytesRead = microphone.read(buffer, 0, buffer.length);

                    SpeechBubblesMod.LOGGER.debug(String.valueOf(bytesRead));

                    if (bytesRead < 0) break;
//
                    if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                        SpeechBubblesMod.LOGGER.debug("Result: " + recognizer.getResult());
                    } else {
                        SpeechBubblesMod.LOGGER.debug("Partial: " + recognizer.getPartialResult());
                    }
                }

                String finalResult = recognizer.getFinalResult();
                if (finalResult == null || finalResult.contains("\"text\":\"\"")) {
                    finalResult = recognizer.getPartialResult();
                }

                SpeechBubblesMod.LOGGER.debug("Final: " + finalResult);

                String finalResult1 = finalResult;
                Minecraft.getInstance().execute(() -> {
                    Minecraft.getInstance().player.sendSystemMessage(Component.literal("Final: " + finalResult1));
                    ModNetworking.CHANNEL.sendToServer(new SpeechPacketC2S(extractSpeechText(finalResult1)));
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }, "MicAudioRecorder");

        thread.setDaemon(true);
        thread.start();
    }

    public static void stopListening() {
        if (!modelLoaded) {
            SpeechBubblesMod.LOGGER.debug("Model is still loading...");
            return;
        }

        listening = false;
        microphone.stop();
        microphone.flush(); // flush any buffered audio
        microphone.close(); // release the mic line so Vosk finalizes cleanly
    }

    public static String extractSpeechText(String input) {
        // This matches the value of the "text" key in the JSON output
        Pattern pattern = Pattern.compile("\"text\"\\s*:\\s*\"(.*?)\"");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group(1); // return the content inside the quotes
        }
        return "";
    }

//    public static void main(String[] args) throws Exception {
//        // set Vosk log level to DEBUG (optional)
//        LibVosk.setLogLevel(LogLevel.DEBUG);
//
//        // load the Vosk model folder (downloaded separately)
//        try (Model model = new Model("C:\\Users\\matth\\Documents\\Minecraft\\mods\\SpeechBubbles\\run\\models\\vosk-model-en-us-0.22")) {
//
//            // define audio format matching model requirements (16kHz, 16 bit, mono)
//            AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
//
//            // get the microphone line
//            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
//
//            if (!AudioSystem.isLineSupported(info)) {
//                System.err.println("Microphone with required format not supported");
//                System.exit(1);
//            }
//
//            TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
//            microphone.open(format);
//            microphone.start();
//
//            System.out.println("Start speaking...");
//
//            try (Recognizer recognizer = new Recognizer(model, 16000)) {
//                byte[] buffer = new byte[4096];
//
//                while (true) {
//                    int bytesRead = microphone.read(buffer, 0, buffer.length);
//                    if (bytesRead < 0) break;
//
//                    if (recognizer.acceptWaveForm(buffer, bytesRead)) {
//                        System.out.println("Result: " + recognizer.getResult());
//                    } else {
//                        System.out.println("Partial: " + recognizer.getPartialResult());
//                    }
//                }
//                System.out.println("Final: " + recognizer.getFinalResult());
//            }
//
//            microphone.stop();
//            microphone.close();
//        }
//    }
}
