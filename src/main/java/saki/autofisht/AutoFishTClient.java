package saki.autofisht;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class AutoFishTClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("autofisht");

    private enum ComboPhase { JUMP, SPIN360, NOLOOK }

    private static int hitIndex = 0;
    private static boolean wasOverlapping = false;
    private static volatile boolean combosEnabled = true;

    // universal delay before click fires, after any combo action starts
    private static final int CLICK_DELAY_TICKS = 2; // ~100ms at 20 TPS
    private static int clickDelayTicksRemaining = 0;

    // yaw motion still steps over time, independent of when click fires
    private static int yawStepsRemaining = 0;
    private static final float YAW_STEP_DEGREES = 90f;
    private static final int YAW_STEP_COUNT = 4;

    private static Float originalYaw = null;
    private static boolean barActiveLastUpdate = false;

    @Override
    public void onInitializeClient() {
        LOGGER.info("[AutoFishT] Auto-click ready.");
        AutoFishTConfig.get();
        ClientTickEvents.END_CLIENT_TICK.register(AutoFishTClient::onTick);
        AutoFishTCommands.register();
    }

    public static boolean toggleCombos() {
        combosEnabled = !combosEnabled;
        LOGGER.info("[AutoFishT] combosEnabled={}", combosEnabled);
        return combosEnabled;
    }

    private static List<ComboPhase> activePhases() {
        AutoFishTConfig cfg = AutoFishTConfig.get();
        List<ComboPhase> phases = new ArrayList<>();
        if (cfg.jumpEnabled) phases.add(ComboPhase.JUMP);
        if (cfg.spin360Enabled) phases.add(ComboPhase.SPIN360);
        if (cfg.noLookEnabled) phases.add(ComboPhase.NOLOOK);
        return phases;
    }

    public static void onTitleUpdate(String title) {
        MinecraftClient client = MinecraftClient.getInstance();
        boolean hasBar = title.indexOf('▲') >= 0 && title.indexOf('■') >= 0;

        if (hasBar) {
            barActiveLastUpdate = true;

            int triangleIdx = title.indexOf('▲');
            int zoneStart = title.indexOf('■');
            int zoneEnd = title.lastIndexOf('■');
            boolean overlapping = triangleIdx >= zoneStart && triangleIdx <= zoneEnd;

            if (overlapping && !wasOverlapping) {
                if (combosEnabled) {
                    performComboHit();
                } else {
                    click();
                }
            }
            wasOverlapping = overlapping;
        } else {
            if (barActiveLastUpdate && originalYaw != null && client.player != null) {
                LOGGER.info("[AutoFishT] Fishing finished, restoring yaw={}", originalYaw);
                client.player.setYaw(originalYaw);
                originalYaw = null;
            }
            barActiveLastUpdate = false;
            wasOverlapping = false;
        }
    }

    private static void performComboHit() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        List<ComboPhase> phases = activePhases();
        if (phases.isEmpty()) {
            click();
            return;
        }

        if (originalYaw == null) {
            originalYaw = client.player.getYaw();
            LOGGER.info("[AutoFishT] Saved original yaw={}", originalYaw);
        }

        ComboPhase phase = phases.get(hitIndex % phases.size());
        LOGGER.info("[AutoFishT] Hit #{} phase={}", hitIndex, phase);

        switch (phase) {
            case JUMP -> client.player.jump();
            case SPIN360 -> yawStepsRemaining = YAW_STEP_COUNT; // keeps stepping in background
            case NOLOOK -> client.player.setYaw(client.player.getYaw() + 180f);
        }

        clickDelayTicksRemaining = CLICK_DELAY_TICKS; // click fires 100ms after any of the above
        hitIndex++;
    }

    private static void click() {
        KeyBinding.onKeyPressed(InputUtil.Type.MOUSE.createFromCode(0));
    }

    private static void onTick(MinecraftClient client) {
        if (client.player == null) return;

        if (yawStepsRemaining > 0) {
            float newYaw = client.player.getYaw() + YAW_STEP_DEGREES;
            client.player.setYaw(newYaw);
            yawStepsRemaining--;
            LOGGER.info("[AutoFishT] Yaw step, remaining={}, yaw={}", yawStepsRemaining, newYaw);
        }

        if (clickDelayTicksRemaining > 0) {
            clickDelayTicksRemaining--;
            if (clickDelayTicksRemaining == 0) {
                click();
            }
        }
    }
}