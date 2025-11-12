package me.alpha432.oyvey.features.modules.hud;

import com.google.common.eventbus.Subscribe;
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.event.impl.Render2DEvent;
import me.alpha432.oyvey.features.modules.client.HudModule;
import me.alpha432.oyvey.util.TextUtil;

public class Watermark extends HudModule {

    public Watermark() {
        super("Watermark", "Display watermark", 100, 10);
    }

    @Override
    @Subscribe
    public void onRender2D(Render2DEvent e) {
        super.onRender2DHud(e);

        String watermarkString = "LimeClient " + OyVey.VERSION;

        e.getContext().drawTextWithShadow(mc.textRenderer,
                watermarkString,
                (int) getX(), (int) getY(), -1);

        setWidth(mc.textRenderer.getWidth(watermarkString));
        setHeight(mc.textRenderer.fontHeight);
    }
}