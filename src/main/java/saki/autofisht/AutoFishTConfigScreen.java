package saki.autofisht;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class AutoFishTConfigScreen extends Screen {
    private final Screen parent;
    private final AutoFishTConfig cfg = AutoFishTConfig.get();

    public AutoFishTConfigScreen(Screen parent) {
        super(Text.literal("AutoFishT Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int w = 200;
        int h = 20;
        int x = width / 2 - w / 2;
        int y = height / 2 - 40;

        addDrawableChild(ButtonWidget.builder(label("Jump", cfg.jumpEnabled), b -> {
            cfg.jumpEnabled = !cfg.jumpEnabled;
            b.setMessage(label("Jump", cfg.jumpEnabled));
        }).dimensions(x, y, w, h).build());

        addDrawableChild(ButtonWidget.builder(label("Spin 360", cfg.spin360Enabled), b -> {
            cfg.spin360Enabled = !cfg.spin360Enabled;
            b.setMessage(label("Spin 360", cfg.spin360Enabled));
        }).dimensions(x, y + 25, w, h).build());

        addDrawableChild(ButtonWidget.builder(label("No Look", cfg.noLookEnabled), b -> {
            cfg.noLookEnabled = !cfg.noLookEnabled;
            b.setMessage(label("No Look", cfg.noLookEnabled));
        }).dimensions(x, y + 50, w, h).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(x, y + 85, w, h).build());
    }

    private static Text label(String name, boolean on) {
        return Text.literal(name + ": " + (on ? "ON" : "OFF"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 20, 0xFFFFFF);
    }

    @Override
    public void close() {
        cfg.save();
        client.setScreen(parent);
    }
}