package com.fabien_gigante.explorer_locator_bar.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import com.fabien_gigante.explorer_locator_bar.config.Config.ColorSettings;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class ConfigMenu implements ModMenuApi {
    private ConfigBuilder builder;
    private ConfigEntryBuilder entries;
    private ConfigCategory category;

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> this.createScreen(parent);
    }

    private Screen createScreen(Screen parent) {
        builder = ConfigBuilder.create().setParentScreen(parent).setTitle(Component.translatable("explorer_locator_bar.config.title"));
        entries = builder.entryBuilder();
        Config cfg = ConfigManager.getConfig();
        // General
        addCategory("general");
        var tabForcesLocatorBar = addToggle("tab_forces_locator_bar", cfg.tabForcesLocatorBar(), Config.DEFAULT.tabForcesLocatorBar());
        var tabShowsNames = addToggle("tab_shows_names", cfg.tabShowsNames(), Config.DEFAULT.tabShowsNames());
        var holdingLocation = addSelector( "holding_location", cfg.holdingLocation(), Config.DEFAULT.holdingLocation());
        var dialResolution = addSlider("dial_resolution", cfg.dialResolution(),0,72, Config.DEFAULT.dialResolution());
        var showDistance = addSelector("show_distance", cfg.showDistance(), Config.DEFAULT.showDistance());
        var showRecovery = addToggle("show_recovery", cfg.showRecovery(), Config.DEFAULT.showRecovery());
        var showSpawn = addToggle("show_spawn", cfg.showSpawn(), Config.DEFAULT.showSpawn());
        var showMaps = addToggle("show_maps", cfg.showMaps(), Config.DEFAULT.showMaps());
        var showInSpectator = addToggle("show_in_spectator", cfg.showInSpectator(), Config.DEFAULT.showInSpectator());
        var holdingBundles = addToggle("holding_bundles", cfg.holdingBundles(), Config.DEFAULT.holdingBundles());
        var clockLocation = addSelector( "clock_location", cfg.clockLocation(), Config.DEFAULT.clockLocation());
        // Colors
        addCategory("colors");
        var colorCustomization = addToggle("colors.enable_customization", cfg.colors().colorCustomization() ,Config.DEFAULT.colors().colorCustomization());
        var lodestoneColor = addColorOrRandom("colors.lodestone", cfg.colors().lodestoneColor(), Config.DEFAULT.colors().lodestoneColor());
        var recoveryColor = addColor("colors.recovery", cfg.colors().recoveryColor(), Config.DEFAULT.colors().recoveryColor());
        var spawnColor = addColor("colors.spawn", cfg.colors().spawnColor(), Config.DEFAULT.colors().spawnColor());
        var dialColor = addColor("colors.dial", cfg.colors().dialColor(), Config.DEFAULT.colors().dialColor());
        // When "Save" is clicked
        builder.setSavingRunnable(() -> {
            ConfigManager.setConfig( new Config(
                tabForcesLocatorBar.get(), tabShowsNames.get(), holdingLocation.get(), showRecovery.get(),
                showSpawn.get(), showMaps.get(), holdingBundles.get(), showInSpectator.get(),
                dialResolution.get(), showDistance.get(), clockLocation.get(), cfg.clockSound(),
                new ColorSettings(
                    colorCustomization.get(),
                    new ColorProvider(lodestoneColor.get()), new ColorProvider(recoveryColor.get()),
                    new ColorProvider(spawnColor.get()), new ColorProvider(dialColor.get())
                )
            ));
        });
        return builder.build();
    }

    private void addCategory(String key) {
        category = builder.getOrCreateCategory(Component.translatable("explorer_locator_bar.config."+key));
    }
    private  AtomicBoolean addToggle(String key, boolean value, boolean def) {
        AtomicBoolean mutable = new AtomicBoolean(value);
        category.addEntry(entries.startBooleanToggle(Component.translatable("explorer_locator_bar.config."+key), value)
            .setDefaultValue(def).setSaveConsumer(mutable::set).build());
        return mutable;
    }
    private <T extends Enum<T>> AtomicReference<T> addSelector(String key, T value, T def) {
        AtomicReference<T> mutable = new AtomicReference<T>(value);
        category.addEntry(entries.startEnumSelector(Component.translatable("explorer_locator_bar.config."+key), value.getDeclaringClass(), value)
            .setEnumNameProvider(v -> Component.translatable("explorer_locator_bar.config.enum."+v.toString().toLowerCase()))
            .setDefaultValue(def).setSaveConsumer(mutable::set).build());
        return mutable;
    }
    private AtomicInteger addSlider(String key, int value, int min, int max, int def) {
        AtomicInteger mutable = new AtomicInteger(value);
        category.addEntry(entries.startIntSlider(Component.translatable("explorer_locator_bar.config."+key), value, min, max)
            .setDefaultValue(def).setSaveConsumer(mutable::set).build());
        return mutable;
    }
    private AtomicInteger addColor(String key, ColorProvider value, ColorProvider def) {
        AtomicInteger mutable = new AtomicInteger(value.color());
        category.addEntry(entries.startColorField(Component.translatable("explorer_locator_bar.config."+key), value.color())
            .setDefaultValue(def.color()).setSaveConsumer(mutable::set).build());
        return mutable;
    }
    private AtomicInteger addColorOrRandom(String key, ColorProvider value, ColorProvider def) {
        AtomicInteger mutable = new AtomicInteger(value.color());
        category.addEntry(entries.startDropdownMenu(Component.translatable("explorer_locator_bar.config."+key), value.asString(), v -> v)
            .setSelections(Stream.of("random", value.asString()).distinct().toList()).setSuggestionMode(true)
            .setDefaultValue(def.asString()).setTooltip(Component.translatable("explorer_locator_bar.config.colors.tooltip"))
            .setErrorSupplier(v -> ColorProvider.isValid(v) ? Optional.empty() : Optional.of(Component.translatable("explorer_locator_bar.config.colors.error")) )
            .setSaveConsumer(v -> mutable.set(ColorProvider.parse(v).color())).build());
        return mutable;
    }          
}