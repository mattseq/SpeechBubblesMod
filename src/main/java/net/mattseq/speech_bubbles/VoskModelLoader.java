package net.mattseq.speech_bubbles;

import net.minecraft.client.Minecraft;
import org.vosk.Model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

public class VoskModelLoader {

    private static final String MODELS_DIR_NAME = "models";
    private static final String MODEL_CONFIG_RELATIVE_PATH = "conf/model.conf";

    public static Model loadModel() throws IOException {
        Path gameDir = Minecraft.getInstance().gameDirectory.toPath();
        Path modelsDir = gameDir.resolve(MODELS_DIR_NAME);

        if (!Files.exists(modelsDir) || !Files.isDirectory(modelsDir)) {
            throw new FileNotFoundException("Models folder not found at: " + modelsDir);
        }

        // Find a valid model folder
        try (Stream<Path> dirs = Files.list(modelsDir)) {
            Optional<Path> modelFolderOpt = dirs
                    .filter(Files::isDirectory)
                    .filter(VoskModelLoader::isValidVoskModelFolder)
                    .findFirst();

            if (modelFolderOpt.isPresent()) {
                Path modelPath = modelFolderOpt.get();
                SpeechBubblesMod.LOGGER.info("Loading Vosk model from: " + modelPath);
                return new Model(modelPath.toString());
            } else {
                throw new FileNotFoundException("No valid Vosk model folder found in: " + modelsDir);
            }
        }
    }

    private static boolean isValidVoskModelFolder(Path folder) {
        Path confPath = folder.resolve(MODEL_CONFIG_RELATIVE_PATH);
        return Files.exists(confPath);
    }
}
