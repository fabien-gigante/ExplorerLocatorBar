package com.fabien_gigante.explorer_locator_bar;

import com.fabien_gigante.explorer_locator_bar.config.Config;
import com.fabien_gigante.explorer_locator_bar.config.ConfigManager;
import com.fabien_gigante.explorer_locator_bar.waypoints.DialWaypoint;
import com.fabien_gigante.explorer_locator_bar.waypoints.MapWaypoint;
import com.fabien_gigante.explorer_locator_bar.waypoints.NamedWaypoint;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.waypoints.ClientWaypointManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.waypoints.PartialTickSupplier;
import net.minecraft.world.waypoints.TrackedWaypoint;
import net.minecraft.world.waypoints.Waypoint;

public class WaypointRenderer {

    protected static record WaypointMatch(TrackedWaypoint waypoint, double yaw) {}
    protected static WaypointMatch NO_MATCH = new WaypointMatch(null, Double.NaN);
    private static boolean distanceRendered = false;

    protected static WaypointMatch getBestWaypoint(Minecraft client, DeltaTracker tickCounter, Stream<TrackedWaypoint> waypoints) {
        Camera camera = client.gameRenderer.getMainCamera();
        Entity cameraEntity = client.getCameraEntity();
        if (cameraEntity == null) return NO_MATCH;
        Level world = cameraEntity.level();
        PartialTickSupplier entityTickProgress = (tickedEntity) -> tickCounter.getGameTimeDeltaPartialTick(
                !world.tickRateManager().isEntityFrozen(tickedEntity)
        );

        return waypoints
            .map(waypoint -> new WaypointMatch(waypoint, waypoint.yawAngleToCamera(client.level, camera, entityTickProgress)))
            .sorted(Comparator.comparingDouble(match -> Math.abs(match.yaw)))
            .findFirst().orElse(NO_MATCH);
    }

    public static void render(Minecraft client, GuiGraphicsExtractor context, DeltaTracker tickCounter, int centerY) {
        distanceRendered = false;
        if (ConfigManager.getConfig().showDistance() != Config.DistanceType.NEVER)
            renderDistance(client, context, tickCounter, centerY);
        if (ConfigManager.getConfig().tabShowsNames() && client.options.keyPlayerList.isDown())
            renderNames(client, context, tickCounter, centerY);
    }

    protected static Optional<Component> getWaypointName(TrackedWaypoint waypoint) {
        if (waypoint instanceof NamedWaypoint named) return named.getName();
        // Could be a known player
        UUID uuid = waypoint.id().left().orElse(null);
        if (uuid == null) return Optional.empty(); 
        ClientPacketListener client = Minecraft.getInstance().getConnection();
        PlayerInfo playerInfo = client.getPlayerInfo(uuid);
        if (playerInfo == null) return Optional.empty(); 
        Component name = playerInfo.getTabListDisplayName();
        if (name == null) name = Component.literal(playerInfo.getProfile().name());
        return Optional.of(name);
    }

    protected static void renderNames(Minecraft client, GuiGraphicsExtractor context, DeltaTracker tickCounter, int centerY) {
        ClientWaypointManager handler = client.player.connection.getWaypointManager();
        if (!(handler instanceof IWaypointAccessor accessor)) return;
        Stream<TrackedWaypoint> waypoints = accessor.getWaypointsUnsorted().stream().filter(waypoint -> !(waypoint instanceof DialWaypoint));
        WaypointMatch best = getBestWaypoint(client, tickCounter, waypoints);
        if (Math.abs(best.yaw) > 60) return;
        
        Optional<Component> textOptional = getWaypointName(best.waypoint);
        if (!textOptional.isPresent()) return;

        Component text = textOptional.get();
        Font textRenderer = client.font;
        int width = textRenderer.width(text);
        int x = getXFromYaw(context, best.yaw) - width / 2;
        context.fill(x + 5 - 2, centerY - 10 - 2, x + width + 5 + 2, centerY - 10 + 9 + 2, ARGB.color(0.5F, CommonColors.BLACK));
        context.text(textRenderer, text, x + 5, centerY - 10, CommonColors.WHITE);
    }

    private static int getXFromYaw(GuiGraphicsExtractor context, double relativeYaw) {
        return Mth.ceil((context.guiWidth() - 9) / 2.0F) + (int)(relativeYaw * 173.0 / 2.0 / 60.0);
    }

    private static double getDistance(Entity player, TrackedWaypoint waypoint) {
        double d2;
        if (ConfigManager.getConfig().showDistance() == Config.DistanceType.HORIZONTAL && waypoint instanceof TrackedWaypoint.Vec3iWaypoint wp) {
            Vec3 pos = Vec3.atCenterOf(wp.vector);
            double dx = pos.x() - player.getX(), dz = pos.z() - player.getZ();
            d2 = dx*dx + dz*dz;
        } else {
            d2 = waypoint.distanceSquared(player);
        }
        return Math.sqrt(d2);
    }

    protected static void renderDistance(Minecraft client, GuiGraphicsExtractor context, DeltaTracker tickCounter, int centerY) {
        ClientWaypointManager handler = client.player.connection.getWaypointManager();
        if (!(handler instanceof IWaypointAccessor accessor)) return;
        Stream<TrackedWaypoint> waypoints = accessor.getWaypointsUnsorted().stream().filter(waypoint -> !(waypoint instanceof DialWaypoint));
        WaypointMatch best = getBestWaypoint(client, tickCounter, waypoints);
        if (best.waypoint == null || Math.abs(best.yaw) > 10) return;

        double dist = getDistance(client.player, best.waypoint);
        String label = getDistanceShortString(dist);
        if (label.isEmpty()) return;
        
        distanceRendered = true;
        Font textRenderer = client.font;
        int width = textRenderer.width(label);
        int x = Math.round((context.guiWidth() - width) / 2);
        int y = centerY - 10;

        context.text(client.font, label, x-1, y, CommonColors.BLACK, false);
        context.text(client.font, label, x+1, y, CommonColors.BLACK, false);
        context.text(client.font, label, x, y-1, CommonColors.BLACK, false);
        context.text(client.font, label, x, y+1, CommonColors.BLACK, false);
        context.text(client.font, label, x, y, getColor(best.waypoint), false);
    }

    private static String getDistanceShortString(double distance) {
        if (Double.isNaN(distance)) return "";
        if (Double.isInfinite(distance)) return "∞";
        if (distance >= 10_000_000) return Math.round(distance / 1_000_000) + "M";
        if (distance >= 1_000_000) return Math.round(distance / 100_000) / 10f + "M";
        if (distance >= 10_000) return Math.round(distance / 1_000) + "k";
        if (distance >= 1_000) return Math.round(distance / 100) / 10f + "k";
        return String.valueOf(Math.round(distance));
    }

    private static int getColor(TrackedWaypoint waypoint) {
        Waypoint.Icon config = waypoint.icon();
        Optional<Integer> color = config instanceof MapWaypoint.Config mapConfig ? mapConfig.textColor : config.color;
        return color.orElseGet( () -> {
            int hash = waypoint.id().map(uuid -> uuid.hashCode(), name -> name.hashCode());
            return ARGB.setBrightness(ARGB.color(255, hash), 0.9F);
        });
    }

    public static boolean shouldShowExperienceLevel() { return !distanceRendered; }
}
