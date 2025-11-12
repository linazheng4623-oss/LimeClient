package me.alpha432.oyvey.features.modules.hud;

import com.google.common.eventbus.Subscribe;
import me.alpha432.oyvey.event.impl.Render2DEvent;
import me.alpha432.oyvey.features.modules.client.HudModule;
import me.alpha432.oyvey.features.settings.Setting;
import me.alpha432.oyvey.OyVey;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HackList extends HudModule {
    public Setting<Boolean> alignRight = bool("AlignRight", false);
    public Setting<Boolean> vertical = bool("Vertical", true);
    public Setting<Boolean> sortAlphabetical = bool("SortAlphabetical", true);

    public HackList() {
        super("HackList", "Shows active modules", 150, 20);
    }

    @Override
    @Subscribe
    public void onRender2D(Render2DEvent e) {
        super.onRender2DHud(e);

        if (nullCheck()) return;

        // Get all enabled modules except this one
        List<String> enabledModules = new ArrayList<>();
        for (me.alpha432.oyvey.features.modules.Module module : OyVey.moduleManager.getModules()) {
            if (module.isEnabled() && module != this) {
                enabledModules.add(module.getName());
            }
        }

        // Sort modules
        if (sortAlphabetical.getValue()) {
            enabledModules.sort(Comparator.naturalOrder());
        }

        if (enabledModules.isEmpty()) {
            setWidth(0);
            setHeight(0);
            return;
        }

        if (vertical.getValue()) {
            // Vertical layout - one module per line
            int maxWidth = 0;
            int currentY = (int) getY();

            for (String moduleName : enabledModules) {
                int textWidth = mc.textRenderer.getWidth(moduleName);
                int xPos = (int) getX();

                if (alignRight.getValue() && mc.getWindow() != null) {
                    xPos = mc.getWindow().getScaledWidth() - textWidth - 2;
                }

                e.getContext().drawTextWithShadow(mc.textRenderer, moduleName, xPos, currentY, -1);
                currentY += mc.textRenderer.fontHeight + 1;

                if (textWidth > maxWidth) {
                    maxWidth = textWidth;
                }
            }

            setWidth(maxWidth);
            setHeight((mc.textRenderer.fontHeight + 1) * enabledModules.size());
        } else {
            // Horizontal layout - all modules in one line
            StringBuilder displayText = new StringBuilder();
            for (int i = 0; i < enabledModules.size(); i++) {
                if (i > 0) {
                    displayText.append(" ");
                }
                displayText.append(enabledModules.get(i));
            }

            String text = displayText.toString();
            int xPos = (int) getX();

            if (alignRight.getValue() && mc.getWindow() != null) {
                int textWidth = mc.textRenderer.getWidth(text);
                int screenWidth = mc.getWindow().getScaledWidth();
                xPos = screenWidth - textWidth - 2;
            }

            e.getContext().drawTextWithShadow(mc.textRenderer, text, xPos, (int) getY(), -1);

            setWidth(mc.textRenderer.getWidth(text));
            setHeight(mc.textRenderer.fontHeight);
        }
    }
}