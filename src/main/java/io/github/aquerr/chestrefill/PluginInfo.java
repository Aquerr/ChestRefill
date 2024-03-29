package io.github.aquerr.chestrefill;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public final class PluginInfo
{
    public static final String ID = "chestrefill";
    public static final String NAME = "Chest Refill";
    public static final String VERSION = "2.0.1";
    public static final String PLUGIN_PREFIX_PLAIN = "[CR] ";
    public static final TextComponent PLUGIN_PREFIX = Component.text(PLUGIN_PREFIX_PLAIN, NamedTextColor.GOLD);
    public static final TextComponent ERROR_PREFIX = Component.text(PLUGIN_PREFIX_PLAIN, NamedTextColor.RED);

    private PluginInfo()
    {

    }
}