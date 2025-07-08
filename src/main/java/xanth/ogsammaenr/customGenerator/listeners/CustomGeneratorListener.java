package xanth.ogsammaenr.customGenerator.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandsManager;
import xanth.ogsammaenr.customGenerator.CustomGenerator;
import xanth.ogsammaenr.customGenerator.manager.CustomCategoryManager;
import xanth.ogsammaenr.customGenerator.manager.IslandGeneratorManager;
import xanth.ogsammaenr.customGenerator.model.CustomGeneratorCategory;
import xanth.ogsammaenr.customGenerator.model.GeneratorType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class CustomGeneratorListener implements Listener {
    private final CustomGenerator plugin;
    private final CustomCategoryManager customCatManager;
    private final IslandGeneratorManager islandGenManager;
    private final IslandsManager islandsManager;

    public CustomGeneratorListener(CustomGenerator plugin) {
        this.plugin = plugin;
        this.customCatManager = this.plugin.getCustomCategoryManager();
        this.islandGenManager = this.plugin.getIslandGeneratorManager();
        this.islandsManager = BentoBox.getInstance().getIslandsManager();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onFluidFlow(BlockFromToEvent event) {
        Block source = event.getBlock();
        Block target = event.getToBlock();
        Material liquid = source.getType();

        //  sadece sıvıları dinle
        if (!source.isLiquid()) return;

        //  sadece akma eventi
        if (source == target) return;

        //  sadece farklı bir türe akmayı dinle
        if (target.getType().equals(liquid)) return;
        
        // Find the first CustomCategory that matches this situation
        CustomGeneratorCategory matched =
                customCatManager.getAllCategories().stream()
                        .filter(cat -> matchesConditions(cat, source, target))
                        .findFirst()
                        .orElse(null);

        if (matched == null) return; // if no matched let vanilla handle it

        Optional<Island> islandOpt = islandsManager.getIslandAt(target.getLocation());
        if (islandOpt.isEmpty()) return;
        Island island = islandOpt.get();

        GeneratorType generatorType = islandGenManager.getGeneratorType(island.getUniqueId(), matched.getId());
        if (generatorType == null) return;

        Material result = pickResult(generatorType.getBlockChances());
        if (result == null) return;

        target.setType(result, false);
        event.setCancelled(true);
    }


    // ──────────────────────────────────────────────────────────────────────────
    // Matching helpers
    // ──────────────────────────────────────────────────────────────────────────
    private boolean matchesConditions(CustomGeneratorCategory cat,
                                      Block source, Block target) {

        // fluid type
        if (source.getType() != cat.getFluid()) return false;

        // destination block (optional “to”)
        if (cat.getTo() != null && target.getType() != cat.getTo()) return false;

        // Y‑level range
        int y = target.getY();
        if (cat.getMinYLevel() != null && y < cat.getMinYLevel()) return false;
        if (cat.getMaxYLevel() != null && y > cat.getMaxYLevel()) return false;

        // biome restriction
        if (!cat.getBiomes().isEmpty() && !cat.getBiomes().contains(target.getBiome()))
            return false;

        // directional block checks
        EnumMap<CustomGeneratorCategory.Direction, List<Material>> conds = cat.getBlockConditions();
        for (Map.Entry<CustomGeneratorCategory.Direction, List<Material>> e : conds.entrySet()) {
            if (!hasRequiredNeighbor(target, e.getKey(), e.getValue())) return false;
        }
        return true;
    }

    private boolean hasRequiredNeighbor(Block center,
                                        CustomGeneratorCategory.Direction dir,
                                        List<Material> allowed) {

        switch (dir) {
            case SIDES -> {
                for (BlockFace face : List.of(BlockFace.NORTH, BlockFace.EAST,
                        BlockFace.SOUTH, BlockFace.WEST)) {
                    if (allowed.contains(center.getRelative(face).getType())) return true;
                }
                return false;
            }
            case UP -> {
                return allowed.contains(center.getRelative(BlockFace.UP).getType());
            }
            case DOWN -> {
                return allowed.contains(center.getRelative(BlockFace.DOWN).getType());
            }
            default -> {
                return false;
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Utility – weighted random
    // ──────────────────────────────────────────────────────────────────────────
    private Material pickResult(Map<Material, Double> chances) {
        double total = chances.values().stream().mapToDouble(d -> d).sum();
        if (total <= 0) return null;

        double roll = ThreadLocalRandom.current().nextDouble() * total;
        for (Map.Entry<Material, Double> e : chances.entrySet()) {
            roll -= e.getValue();
            if (roll <= 0) return e.getKey();
        }
        return null;
    }
}
