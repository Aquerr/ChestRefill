package io.github.aquerr.chestrefill.commands.arguments;

import com.google.common.base.Strings;
import io.github.aquerr.chestrefill.util.LootTableHelper;
import net.kyori.adventure.text.Component;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class LootTableArgument
{
    private LootTableArgument()
    {

    }

    public static final class ValueParser implements org.spongepowered.api.command.parameter.managed.ValueParser<String>
    {
        private final LootTableHelper lootTableHelper;

        public ValueParser(final LootTableHelper lootTableHelper)
        {
            this.lootTableHelper = lootTableHelper;
        }

        @Override
        public Optional<? extends String> parseValue(Parameter.Key<? super String> parameterKey, ArgumentReader.Mutable reader, CommandContext.Builder context) throws ArgumentParseException
        {
            if(!reader.canRead())
                throw reader.createException(Component.text("Argument is not a valid loot table"));
            final String lootTableName = reader.parseUnquotedString();
            if (Strings.isNullOrEmpty(lootTableName))
                throw reader.createException(Component.text("Argument is not a valid loot table"));
            if (getAllLootTablesNames().stream().noneMatch(name -> name.equals(lootTableName)))
                throw reader.createException(Component.text("Argument is not a valid loot table"));

            return Optional.of(lootTableName);
        }

        private List<String> getAllLootTablesNames()
        {
            List<String> lootTablesName = new ArrayList<>();

            lootTablesName.addAll(ServerLifecycleHooks.getCurrentServer().getLootTables().getIds()
                    .stream()
                    .map(ResourceLocation::toString)
                    .collect(Collectors.toList()));

            lootTablesName.addAll(lootTableHelper.getAllChestRefillLootTablesNames());
            return lootTablesName;
        }
    }

    public static final class Completer implements ValueCompleter
    {
        private final LootTableHelper lootTableHelper;

        public Completer(final LootTableHelper lootTableHelper)
        {
            this.lootTableHelper = lootTableHelper;
        }

        @Override
        public List<CommandCompletion> complete(CommandContext context, String currentInput)
        {
            List<String> lootTablesNames = getAllLootTablesNames();

            String charSequence = currentInput.toLowerCase();

            return lootTablesNames.stream()
                    .map(String::toLowerCase)
                    .filter(kitName -> kitName.contains(charSequence))
                    .map(CommandCompletion::of)
                    .collect(Collectors.toList());
        }

        private List<String> getAllLootTablesNames()
        {
            List<String> lootTablesName = new ArrayList<>();

            lootTablesName.addAll(ServerLifecycleHooks.getCurrentServer().getLootTables().getIds()
                    .stream()
                    .map(ResourceLocation::toString)
                    .collect(Collectors.toList()));

            lootTablesName.addAll(lootTableHelper.getAllChestRefillLootTablesNames());
            return lootTablesName;
        }
    }
}
