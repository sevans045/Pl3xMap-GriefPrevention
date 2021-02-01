package net.pl3x.map.griefprevention;

import net.pl3x.map.griefprevention.hook.Pl3xMapHook;
import org.bukkit.plugin.java.JavaPlugin;

public final class Pl3xMapGriefprevention extends JavaPlugin {
    private Pl3xMapHook pl3xmapHook;

    @Override
    public void onEnable() {
        if (!getServer().getPluginManager().isPluginEnabled("GriefPrevention")) {
            getLogger().severe("GriefPrevention not found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!getServer().getPluginManager().isPluginEnabled("Pl3xMap")) {
            getLogger().severe("Pl3xMap not found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        pl3xmapHook = new Pl3xMapHook(this);
    }

    @Override
    public void onDisable() {
        if (pl3xmapHook != null) {
            pl3xmapHook.disable();
        }
    }
}
