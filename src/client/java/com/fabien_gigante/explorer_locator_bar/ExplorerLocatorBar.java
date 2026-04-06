package com.fabien_gigante.explorer_locator_bar;

import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.waypoints.WaypointStyleAsset;
import net.minecraft.world.waypoints.WaypointStyleAssets;
import com.fabien_gigante.explorer_locator_bar.config.ConfigManager;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExplorerLocatorBar implements ClientModInitializer {
	public static final String MOD_ID = "explorer_locator_bar";

	public static final Logger LOGGER = LoggerFactory.getLogger("Explorer Locator Bar");

	public static final ResourceKey<WaypointStyleAsset> LODESTONE_STYLE = style("lodestone");
	public static final ResourceKey<WaypointStyleAsset> DEATH_STYLE = style("death");
	public static final ResourceKey<WaypointStyleAsset> SPAWN_STYLE = style("spawn");
	public static final ResourceKey<WaypointStyleAsset> COMPASS_DIVISION_STYLE = style("compass_division");
	public static final ResourceKey<WaypointStyleAsset> COMPASS_DIVISION_SMALL_STYLE = style("compass_division_small");
	public static final List<ResourceKey<WaypointStyleAsset>> COMPASS_CARDINAL_STYLE = List.of( style("compass_south"), style("compass_west"), style("compass_north"), style("compass_east") );

	public static WaypointTracker waypointTracker = new WaypointTracker();
	public static ClockTracker bedtimeTracker = new ClockTracker();

	@SuppressWarnings("deprecation")
	@Override
	public void onInitializeClient() {
		LOGGER.info("Initializing Locator Lodestones");
		ConfigManager.initConfig(this::reset);
		waypointTracker.init();
		ClientTickEvents.END_CLIENT_TICK.register(this::tick);
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> this.reset());
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
			new SimpleSynchronousResourceReloadListener() {
				@Override
				public Identifier getFabricId() { return id("waypoint_style_assets_listener"); }
				@Override
				public void onResourceManagerReload(ResourceManager manager) { MapWaypointStyleAssets.reload(); }
			});
	}

	public void tick(Minecraft client) {
		waypointTracker.tick(client);
		bedtimeTracker.tick(client);
	}

	public void reset() {
		waypointTracker.reset();
		bedtimeTracker.reset();
	}

	public static void onInventoryChanged() {
		waypointTracker.setChanged();
		bedtimeTracker.setChanged();
	}

	public static ResourceKey<WaypointStyleAsset> style(String path) {
		return ResourceKey.create(WaypointStyleAssets.ROOT_ID, id(path));
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}