package saki.autofisht;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoFishTClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("autofisht");

    private static volatile boolean wasOverlapping = false;

    @Override
    public void onInitializeClient() {
        LOGGER.info("[AutoFishT] Auto-click ready.");
    }

    public static void onTitleUpdate(String title) {
        if (title.indexOf('▲') < 0 || title.indexOf('■') < 0) {
            wasOverlapping = false;
            return;
        }

        int triangleIdx = title.indexOf('▲');
        int zoneStart = title.indexOf('■');
        int zoneEnd = title.lastIndexOf('■');

        boolean overlapping = triangleIdx >= zoneStart && triangleIdx <= zoneEnd;

        if (overlapping && !wasOverlapping) {
            triggerClick();
        }
        wasOverlapping = overlapping;
    }

    private static void triggerClick() {
        KeyBinding.onKeyPressed(InputUtil.Type.MOUSE.createFromCode(0)); // left click only
    }
}
