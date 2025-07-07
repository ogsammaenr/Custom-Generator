package xanth.ogsammaenr.customGenerator.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFertilizeEvent;
import xanth.ogsammaenr.customGenerator.CustomGenerator;

import java.util.*;

public class FertilizeListener implements Listener {
    private final CustomGenerator plugin;

    private final Map<Location, ClickData> mossClickData = new HashMap<>();
    private final Random random = new Random();

    private Set<Material> allowed = Set.of(
            Material.DEEPSLATE,
            Material.TUFF,
            Material.COARSE_DIRT,
            Material.DIRT,
            Material.GRASS_BLOCK,
            Material.ROOTED_DIRT,
            Material.MUD,
            Material.AIR
    );


    public FertilizeListener() {
        plugin = CustomGenerator.getInstance();

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            mossClickData.entrySet().removeIf(entry -> (now - entry.getValue().lastClickTime) > 5000);
        }, 20L, 20L);
    }


    @EventHandler
    public void onFertilize(BlockFertilizeEvent event) {
        Block origin = event.getBlock();
        if (origin.getType() != Material.MOSS_BLOCK) return;

        Location loc = origin.getLocation();
        ClickData data = mossClickData.getOrDefault(loc, new ClickData(0, randomThreshold()));

        data.clickCount++;
        data.lastClickTime = System.currentTimeMillis();

        if (data.clickCount >= data.threshold) {
            // Reset sayacı
            data.clickCount = 0;
            data.threshold = randomThreshold();
            // Yayılmaya izin ver
        } else {
            // Yayılmayı engelle
            event.setCancelled(true);
        }
        mossClickData.put(loc, data);


        List<BlockState> blocks = event.getBlocks();
        Iterator<BlockState> iterator = blocks.iterator();

        while (iterator.hasNext()) {
            BlockState state = iterator.next();
            Block target = state.getBlock();

            if (target.getLocation().distanceSquared(loc) > 4 || !allowed.contains(target.getType()) || random.nextDouble(1) < 0.5) {
                iterator.remove();
            }
        }
        if (blocks.isEmpty()) event.setCancelled(true);
    }

    private int randomThreshold() {
        return 5 + random.nextInt(5);
    }


    private static class ClickData {
        int clickCount;
        int threshold;
        long lastClickTime;

        public ClickData(int clickCount, int threshold) {
            this.clickCount = clickCount;
            this.threshold = threshold;
            this.lastClickTime = System.currentTimeMillis();
        }
    }
}
