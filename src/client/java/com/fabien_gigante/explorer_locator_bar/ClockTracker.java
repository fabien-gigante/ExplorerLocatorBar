package com.fabien_gigante.explorer_locator_bar;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.fabien_gigante.explorer_locator_bar.config.ConfigManager;

public class ClockTracker extends AbstractTracker {
    private boolean hasClock = false;
    private boolean isNight = false;

    @Override
    public void reset() {
        super.reset();
        hasClock = false;   
    }

    @Override
    public void tick(Minecraft client) {
        super.tick(client);
        if (client.level == null || client.player == null) return;
        boolean isNight = client.level.isDarkOutside();
        if (hasClock && isNight && !this.isNight) {
            ExplorerLocatorBar.LOGGER.info("It's night time!");
            client.player.makeSound(ConfigManager.getConfig().clockSound());
        }
        this.isNight = isNight;
    }

    @Override
    public void update(Minecraft client) {
        List<ItemStack> stacks = getPlayerStacks(client.player, ConfigManager.getConfig().clockLocation());
        hasClock = stacks.stream().anyMatch(stack -> stack.is(Items.CLOCK));
    }
}