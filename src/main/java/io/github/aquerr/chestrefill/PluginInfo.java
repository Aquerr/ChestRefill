package io.github.aquerr.chestrefill;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

/**
 * Created by Aquerr on 2018-02-09.
 */
public abstract class PluginInfo
{
    public static final String ID = "chestrefill";
    public static final String NAME = "Chest Refill";
    public static final String VERSION = "1.6.0";
    public static final String DESCRIPTION = "Plugin for restoring contents of a container after the specified time.";
    public static final String URL = "https://github.com/Aquerr/ChestRefill";
    public static final Text PLUGIN_PREFIX = Text.of(TextColors.GOLD, "[CR] ");
    public static final Text ERROR_PREFIX = Text.of(TextColors.RED, "[CR] ");
    public static final String AUTHORS = "Aquerr";
}
