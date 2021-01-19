package net.pl3x.map.griefprevention.hook;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.pl3x.map.griefprevention.Pl3xmapGriefprevention;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.Collection;
import java.util.UUID;

public class GriefPreventionHook {
    private final Pl3xmapGriefprevention plugin;

    public GriefPreventionHook(Pl3xmapGriefprevention plugin) {
        this.plugin = plugin;
    }

    public boolean isWorldEnabled(UUID uuid) {
        World world = Bukkit.getWorld(uuid);
        return GriefPrevention.instance.claimsEnabledForWorld(world);
    }

    public Collection<Claim> getClaims() {
        return GriefPrevention.instance.dataStore.getClaims();
    }
}
