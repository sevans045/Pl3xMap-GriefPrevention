package net.pl3x.map.griefprevention.task;

import me.ryanhamshire.GriefPrevention.Claim;
import net.pl3x.map.api.Key;
import net.pl3x.map.api.MapWorld;
import net.pl3x.map.api.Point;
import net.pl3x.map.api.SimpleLayerProvider;
import net.pl3x.map.api.marker.Marker;
import net.pl3x.map.api.marker.MarkerOptions;
import net.pl3x.map.api.marker.Rectangle;
import net.pl3x.map.griefprevention.configuration.Config;
import net.pl3x.map.griefprevention.hook.GPHook;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class Pl3xMapTask extends BukkitRunnable {
    private final MapWorld world;
    private final SimpleLayerProvider provider;

    private boolean stop;

    public Pl3xMapTask(MapWorld world, SimpleLayerProvider provider) {
        this.world = world;
        this.provider = provider;
    }

    @Override
    public void run() {
        if (stop) {
            cancel();
        }
        updateClaims();
    }

    void updateClaims() {
        provider.clearMarkers(); // TODO track markers instead of clearing them
        Collection<Claim> topLevelClaims = GPHook.getClaims();
        if (topLevelClaims != null) {
            topLevelClaims.stream()
                    .filter(claim -> claim.getGreaterBoundaryCorner().getWorld().getUID().equals(this.world.uuid()))
                    .filter(claim -> claim.parent == null)
                    .forEach(this::handleClaim);
        }
    }

    private void handleClaim(Claim claim) {
        Location min = claim.getLesserBoundaryCorner();
        Location max = claim.getGreaterBoundaryCorner();
        if (min == null) {
            return;
        }

        Rectangle rect = Marker.rectangle(Point.of(min.getBlockX(), min.getBlockZ()), Point.of(max.getBlockX() + 1, max.getBlockZ() + 1));

        ArrayList<String> builders = new ArrayList<>();
        ArrayList<String> containers = new ArrayList<>();
        ArrayList<String> accessors = new ArrayList<>();
        ArrayList<String> managers = new ArrayList<>();
        claim.getPermissions(builders, containers, accessors, managers);

        String worldName = min.getWorld().getName();

        MarkerOptions.Builder options = MarkerOptions.builder()
                .strokeColor(Config.STROKE_COLOR)
                .strokeWeight(Config.STROKE_WEIGHT)
                .strokeOpacity(Config.STROKE_OPACITY)
                .fillColor(Config.FILL_COLOR)
                .fillOpacity(Config.FILL_OPACITY)
                .clickTooltip((claim.isAdminClaim() ? Config.ADMIN_CLAIM_TOOLTIP : Config.CLAIM_TOOLTIP)
                        .replace("{world}", worldName)
                        .replace("{id}", Long.toString(claim.getID()))
                        .replace("{owner}", claim.getOwnerName())
                        .replace("{managers}", managers.stream().map(uuid -> Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName()).collect(Collectors.joining(", ")))
                        .replace("{builders}", builders.stream().map(uuid -> Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName()).collect(Collectors.joining(", ")))
                        .replace("{containers}", containers.stream().map(uuid -> Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName()).collect(Collectors.joining(", ")))
                        .replace("{accessors}", accessors.stream().map(uuid -> Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName()).collect(Collectors.joining(", ")))
                        .replace("{area}", Integer.toString(claim.getArea()))
                        .replace("{width}", Integer.toString(claim.getWidth()))
                        .replace("{height}", Integer.toString(claim.getHeight()))
                );

        if (claim.isAdminClaim()) {
            options.strokeColor(Color.BLUE).fillColor(Color.BLUE);
        }

        rect.markerOptions(options);

        String markerid = "griefprevention_" + worldName + "_region_" + Long.toHexString(claim.getID());
        this.provider.addMarker(Key.of(markerid), rect);
    }

    public void disable() {
        cancel();
        this.stop = true;
        this.provider.clearMarkers();
    }
}

