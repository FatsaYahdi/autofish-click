package saki.autofisht;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

public class AutoFishTCommands {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                ClientCommandManager.literal("aft")
                    .executes(AutoFishTCommands::toggleAll)
                    .then(ClientCommandManager.literal("jump").executes(AutoFishTCommands::toggleJump))
                    .then(ClientCommandManager.literal("spin360").executes(AutoFishTCommands::toggleSpin360))
                    .then(ClientCommandManager.literal("nolook").executes(AutoFishTCommands::toggleNoLook))
            );
        });
    }

    private static int toggleAll(CommandContext<FabricClientCommandSource> ctx) {
        boolean enabled = AutoFishTClient.toggleCombos();
        ctx.getSource().sendFeedback(Text.literal(
            "[AutoFishT] Combos " + (enabled ? "ENABLED" : "DISABLED")
        ));
        return 1;
    }

    private static int toggleJump(CommandContext<FabricClientCommandSource> ctx) {
        AutoFishTConfig cfg = AutoFishTConfig.get();
        cfg.jumpEnabled = !cfg.jumpEnabled;
        cfg.save();
        ctx.getSource().sendFeedback(Text.literal(
            "[AutoFishT] Jump combo " + (cfg.jumpEnabled ? "ENABLED" : "DISABLED")
        ));
        return 1;
    }

    private static int toggleSpin360(CommandContext<FabricClientCommandSource> ctx) {
        AutoFishTConfig cfg = AutoFishTConfig.get();
        cfg.spin360Enabled = !cfg.spin360Enabled;
        cfg.save();
        ctx.getSource().sendFeedback(Text.literal(
            "[AutoFishT] Spin360 combo " + (cfg.spin360Enabled ? "ENABLED" : "DISABLED")
        ));
        return 1;
    }

    private static int toggleNoLook(CommandContext<FabricClientCommandSource> ctx) {
        AutoFishTConfig cfg = AutoFishTConfig.get();
        cfg.noLookEnabled = !cfg.noLookEnabled;
        cfg.save();
        ctx.getSource().sendFeedback(Text.literal(
            "[AutoFishT] NoLook combo " + (cfg.noLookEnabled ? "ENABLED" : "DISABLED")
        ));
        return 1;
    }
}