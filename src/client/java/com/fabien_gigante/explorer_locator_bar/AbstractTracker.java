package com.fabien_gigante.explorer_locator_bar;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import com.fabien_gigante.explorer_locator_bar.config.Config;
import com.fabien_gigante.explorer_locator_bar.config.ConfigManager;

public abstract class AbstractTracker {
    protected static int UPDATE_COOLDOWN = 10;
    private boolean hasChanged = false;
    private long lastUpdateTime = 0;

    public void init() {}

    public void setChanged() {
        hasChanged = true;
    }

    public void reset() {
        lastUpdateTime = 0; 
        setChanged();
    }
    
    public void tick(Minecraft client) { 
        LocalPlayer player = client.player;
        if (!hasChanged || player == null) return;
        if (!player.connection.hasClientLoaded()) return;
        if (lastUpdateTime <= player.tickCount && player.tickCount < lastUpdateTime + UPDATE_COOLDOWN) return;
        lastUpdateTime = player.tickCount;
        hasChanged = false;
        update(client);
    }

    public abstract void update(Minecraft client);

    protected static List<ItemStack> getPlayerStacks(Player player, Config.HoldingLocation location) {
        List<ItemStack> stacks = new ArrayList<>();

        // Compute stacks based on specified location
        switch(location) {
            case Config.HoldingLocation.NEVER:
                return stacks;
            case Config.HoldingLocation.HANDS:
                stacks.add(player.getInventory().getSelectedItem());
                break;
            case Config.HoldingLocation.HOTBAR: 
                for (int slot = 0; slot < Inventory.getSelectionSize(); slot++)
                    stacks.add( player.getInventory().getItem(slot));
                break;
            case Config.HoldingLocation.INVENTORY:
            default:
                stacks.addAll(player.getInventory().getNonEquipmentItems());
        }
        stacks.add(player.getOffhandItem());

        // If configured (and location not NONE), include contents of bundles
        if (ConfigManager.getConfig().holdingBundles()) {
            ListIterator<ItemStack> it = stacks.listIterator();
            while (it.hasNext()) {
                BundleContents contentsComponent = it.next().get(DataComponents.BUNDLE_CONTENTS);
                if (contentsComponent != null) contentsComponent.itemCopyStream().forEach(it::add);
            }
        }
        return stacks;
    }    
}