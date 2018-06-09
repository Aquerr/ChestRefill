package io.github.aquerr.chestrefill;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

/**
 * Created by Aquerr on 2018-02-09.
 */
public abstract class PluginInfo
{
    public static final String Id = "chestrefill";
    public static final String Name = "Chest Refill";
    public static final String Version = "1.3.0";
    public static final String Description = "Plugin for restoring contents of a container after the specified time.";
    public static final String Url = "https://github.com/Aquerr/ChestRefill";
    public static final Text PluginPrefix = Text.of(TextColors.GOLD, "[CR] ");
    public static final String Authors = "Aquerr";
}
