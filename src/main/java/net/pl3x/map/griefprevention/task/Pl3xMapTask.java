package net.pl3x.map.griefprevention.task;

import me.ryanhamshire.GriefPrevention.Claim;
import net.pl3x.map.api.Key;
import net.pl3x.map.api.MapWorld;
import net.pl3x.map.api.Point;
import net.pl3x.map.api.SimpleLayerProvider;
import net.pl3x.map.api.marker.Marker;
import net.pl3x.map.api.marker.MarkerOptions;
import net.pl3x.map.api.marker.Rectangle;
import net.pl3x.map.griefprevention.Pl3xmapGriefprevention;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.Color;
import java.util.Collection;

public class Pl3xMapTask extends BukkitRunnable {
    private final Pl3xmapGriefprevention plugin;
    private final MapWorld world;
    private final SimpleLayerProvider provider;

    private boolean stop;

    public Pl3xMapTask(Pl3xmapGriefprevention plugin, MapWorld world, SimpleLayerProvider provider) {
        this.plugin = plugin;
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
        Collection<Claim> topLevelClaims = this.plugin.getGPHook().getClaims();
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

        String worldName = min.getWorld().getName();
        String ownerName = claim.getOwnerName();

        Rectangle rect = Marker.rectangle(Point.of(min.getBlockX(), min.getBlockZ()), Point.of(max.getBlockX(), max.getBlockZ()));

        MarkerOptions.Builder options = MarkerOptions.builder()
                .strokeColor(Color.GREEN)
                .fillColor(Color.GREEN)
                .fillOpacity(0.2)
                .clickTooltip("Region owned by<br/>" + ownerName);

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

