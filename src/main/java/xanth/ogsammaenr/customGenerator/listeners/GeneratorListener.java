package xanth.ogsammaenr.customGenerator.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import xanth.ogsammaenr.customGenerator.CustomGenerator;

public class GeneratorListener implements Listener {

    @EventHandler
    public void onBlockForm(BlockFormEvent event) {
        Material type = event.getNewState().getType();
        Location loc = event.getBlock().getLocation();

        CustomGenerator.getInstance().getLogger().info("buraya girildi");
        event.setCancelled(true);

        if (type == Material.COBBLESTONE || type == Material.STONE || type == Material.BASALT) {
            CustomGenerator.getInstance().getLogger().info("[GENERATOR] Üretilen blok: " + type + " @ " + loc);
            loc.getBlock().setType(Material.GOLD_BLOCK);
        } else {
            loc.getBlock().setType(Material.DIAMOND_BLOCK);
        }

    }
}
