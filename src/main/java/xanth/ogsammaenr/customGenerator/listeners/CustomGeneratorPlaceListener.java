package xanth.ogsammaenr.customGenerator.listeners;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
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

public class CustomGeneratorPlaceListener implements Listener {
    private final CustomGenerator plugin;
    private final CustomCategoryManager categoryManager;
    private final IslandsManager islandsManager;
    private final IslandGeneratorManager islandGeneratorManager;

    public CustomGeneratorPlaceListener(CustomGenerator plugin) {
        this.plugin = plugin;
        this.categoryManager = plugin.getCustomCategoryManager();
        this.islandsManager = BentoBox.getInstance().getIslandsManager();
        this.islandGeneratorManager = plugin.getIslandGeneratorManager();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block placedBlock = event.getBlock();
        Material type = placedBlock.getType();

        if (!categoryManager.isGeneratorRelevantBlock(type)) {
            return;
        }

        for (BlockFace face : BlockFace.values()) {
            Block neighbor = placedBlock.getRelative(face);
            if (neighbor.isLiquid()) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> handleGeneratorCheck(neighbor), 1L);
            }
        }
    }

    private void handleGeneratorCheck(Block fluidBlock) {
        Location loc = fluidBlock.getLocation();
        Material fluid = fluidBlock.getType();


        // Tüm custom kategorileri döngüyle kontrol et
        for (CustomGeneratorCategory category : categoryManager.getAllCategories()) {

            // 1. Sıvı tipi uyuşmuyor mu?
            if (category.getFluid() != fluid) continue;

            // 2. Y seviyesi kontrolü
            if (!category.matchesY(loc.getBlockY())) continue;

            // 3. Biome kontrolü (varsa)
            if (category.hasBiomeRestriction()) {
                if (!category.getBiomes().contains(loc.getBlock().getBiome())) continue;
            }

            // 4. "To" bloğu kontrolü (örneğin stone yerine deepslate vs)
            if (category.hasToBlock()) {
                continue;
            }

            // 5. Yön bazlı block condition kontrolü
            EnumMap<CustomGeneratorCategory.Direction, List<Material>> conditions = category.getBlockConditions();
            boolean matches = true;

            for (CustomGeneratorCategory.Direction dir : conditions.keySet()) {
                List<Material> required = conditions.get(dir);
                boolean found = switch (dir) {
                    case SIDES -> checkSides(fluidBlock, required);
                    case UP -> required.contains(fluidBlock.getRelative(BlockFace.UP).getType());
                    case DOWN -> required.contains(fluidBlock.getRelative(BlockFace.DOWN).getType());
                };

                if (!found) {
                    matches = false;
                    break;
                }
            }

            if (!matches) continue;

            Optional<Island> islandOpt = islandsManager.getIslandAt(loc);
            if (islandOpt.isEmpty()) return;
            String islandId = islandOpt.get().getUniqueId();

            GeneratorType activeType = islandGeneratorManager.getGeneratorType(islandId, category.getId());
            if (activeType == null) return;

            Material result = pickResult(activeType.getBlockChances());
            if (result == null || result.isAir()) return;

            fluidBlock.setType(result);
            playEffects(fluidBlock);
            return;
        }
    }

    private boolean checkSides(Block block, List<Material> required) {
        if (required.contains(block.getRelative(BlockFace.NORTH).getType())) return true;
        if (required.contains(block.getRelative(BlockFace.EAST).getType())) return true;
        if (required.contains(block.getRelative(BlockFace.SOUTH).getType())) return true;
        if (required.contains(block.getRelative(BlockFace.WEST).getType())) return true;
        return false;
    }

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

    private void playEffects(Block block) {
        final double blockX = block.getX();
        final double blockY = block.getY();
        final double blockZ = block.getZ();

        // Play sound for spawning block
        block.getWorld().playSound(block.getLocation(),
                Sound.BLOCK_FIRE_EXTINGUISH,
                SoundCategory.BLOCKS,
                0.3F,
                2.6F + (2 - 1) * 0.8F);

        // This spawns 4 large smoke particles.
        for (int counter = 0; counter < 4; ++counter) {
            block.getWorld().spawnParticle(Particle.LARGE_SMOKE,
                    blockX + Math.random(),
                    blockY + 0.5 + Math.random(),
                    blockZ + Math.random(),
                    1,
                    0,
                    0,
                    0,
                    0);
        }

    }
}