package xanth.ogsammaenr.customGenerator.listeners;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFertilizeEvent;
import xanth.ogsammaenr.customGenerator.CustomGenerator;

import java.util.Iterator;
import java.util.Random;

public class FertilizeListener implements Listener {
    private final CustomGenerator plugin;


    public FertilizeListener() {
        plugin = CustomGenerator.getInstance();
    }


    @EventHandler
    public void onFertilize(BlockFertilizeEvent event) {
        if (event.getBlock().getType() != Material.MOSS_BLOCK) {
            return;
        }

        // Şansa göre bazı blokları yayılmadan çıkar
        Random random = new Random();
        Iterator<BlockState> iterator = event.getBlocks().iterator();

        while (iterator.hasNext()) {
            BlockState state = iterator.next();

            if (!(state.getBlock().getLocation().distance(event.getBlock().getLocation()) < 1)) {
                iterator.remove();
            }
        }
    }
}
