package net.mattseq.speech_bubbles;

import com.mojang.logging.LogUtils;
import net.mattseq.speech_bubbles.networking.ModNetworking;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.io.File;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(SpeechBubblesMod.MODID)
public class SpeechBubblesMod
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "speech_bubbles";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public SpeechBubblesMod(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        ModNetworking.register();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
//            event.accept(EXAMPLE_BLOCK_ITEM);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            try {
                // TODO: remove or modify NativeLoader to allow bundled libvosk.dll

                File nativeDll = new File("natives/libvosk.dll");
                System.load(nativeDll.getAbsolutePath());
                SpeechBubblesMod.LOGGER.debug("libvosk.dll loaded successfully");
            } catch (Exception e) {
                SpeechBubblesMod.LOGGER.debug("libvosk.dll not found in 'natives' folder. Please create 'natives' folder in this instance and download libvosk.dll from this mod's Modrinth/Curseforge page");
                throw new RuntimeException(e);
            }

            DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> SpeechRecognizer::initialize);
        }
    }
}
