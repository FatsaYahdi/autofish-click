package saki.autofisht;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AutoFishTClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("autofisht");

    private enum ComboPhase { JUMP, SPIN360, NOLOOK }

    private static int hitIndex = 0;
    private static boolean wasOverlapping = false;
    private static volatile boolean combosEnabled = true;

    private static final int CLICK_DELAY_TICKS = 2;
    private static int clickDelayTicksRemaining = 0;

    private static int yawStepsRemaining = 0;
    private static final float YAW_STEP_DEGREES = 90f;
    private static final int YAW_STEP_COUNT = 4;

    private static Float originalYaw = null;
    private static boolean barActiveLastUpdate = false;

    // target hex color confirmed from your logs - triangle turns this when in the click-ready zone
    private static final int TARGET_RGB = 0xFACC15;

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

    public static void onTitleUpdate(Text titleText) {
        MinecraftClient client = MinecraftClient.getInstance();
        String title = titleText.getString();
        boolean hasBar = title.indexOf('▲') >= 0 && title.indexOf('■') >= 0;

        if (hasBar) {
            barActiveLastUpdate = true;

            int triangleIdx = title.indexOf('▲');
            Integer triangleRgb = getRgbAtIndex(titleText, triangleIdx);

            boolean overlapping = triangleRgb != null && triangleRgb == TARGET_RGB;

            if (overlapping && !wasOverlapping) {
                if (combosEnabled) {
                    performComboHit(client);
                } else {
                    click();
                }
            }
            wasOverlapping = overlapping;
        } else {
            if (barActiveLastUpdate && originalYaw != null && client.player != null) {
                client.player.setYaw(originalYaw);
                originalYaw = null;
            }
            barActiveLastUpdate = false;
            wasOverlapping = false;
        }
    }

    /** Walks the Text component tree, returns the raw RGB int of the color at the given plain-text character index. */
    private static Integer getRgbAtIndex(Text root, int targetIndex) {
        int[] counted = {0};
        Integer[] result = {null};

        root.visit((style, asString) -> {
            int len = asString.length();
            if (counted[0] <= targetIndex && targetIndex < counted[0] + len) {
                if (style.getColor() != null) {
                    result[0] = style.getColor().getRgb();
                }
                return Optional.of(Boolean.TRUE);
            }
            counted[0] += len;
            return Optional.empty();
        }, Style.EMPTY);

        return result[0];
    }

    private static void performComboHit(MinecraftClient client) {
        if (client.player == null) return;

        List<ComboPhase> phases = activePhases();
        if (phases.isEmpty()) {
            click();
            return;
        }

        if (originalYaw == null) {
            originalYaw = client.player.getYaw();
        }

        ComboPhase phase = phases.get(hitIndex % phases.size());

        switch (phase) {
            case JUMP -> client.player.jump();
            case SPIN360 -> yawStepsRemaining = YAW_STEP_COUNT;
            case NOLOOK -> client.player.setYaw(client.player.getYaw() + 180f);
        }

        clickDelayTicksRemaining = CLICK_DELAY_TICKS;
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
        }

        if (clickDelayTicksRemaining > 0) {
            clickDelayTicksRemaining--;
            if (clickDelayTicksRemaining == 0) {
                click();
            }
        }
    }
}