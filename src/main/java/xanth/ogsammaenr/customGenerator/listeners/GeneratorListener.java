package xanth.ogsammaenr.customGenerator.listeners;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;
import xanth.ogsammaenr.customGenerator.CustomGenerator;
import xanth.ogsammaenr.customGenerator.manager.IslandGeneratorManager;
import xanth.ogsammaenr.customGenerator.model.GeneratorCategory;
import xanth.ogsammaenr.customGenerator.model.GeneratorType;

import java.util.Map;
import java.util.Random;

public class GeneratorListener implements Listener {

    private CustomGenerator plugin;
    private IslandGeneratorManager islandGeneratorManager;

    private Random rand = new Random();

    public GeneratorListener() {
        this.plugin = CustomGenerator.getInstance();
        this.islandGeneratorManager = plugin.getIslandGeneratorManager();
    }


    @EventHandler
    public void onBlockForm(BlockFormEvent event) {
        Material type = event.getNewState().getType();

        if (!(type == Material.COBBLESTONE || type == Material.STONE || type == Material.BASALT)) {
            return;
        }

        Block block = event.getBlock();
        Location loc = block.getLocation();
        World world = block.getWorld();


        Island island = BentoBox.getInstance().getIslandsManager().getIslandAt(loc).get();
        if (island == null) {
            return;
        }

        GeneratorType generatorType = null;
        if (type == Material.STONE
            && loc.getBlockY() < 0
            && islandGeneratorManager.getGeneratorType(island.getUniqueId(), GeneratorCategory.DEEPSLATE) != null) {
            generatorType = islandGeneratorManager.getGeneratorType(island.getUniqueId(), GeneratorCategory.DEEPSLATE);
        }

        generatorType = generatorType == null ? islandGeneratorManager.getGeneratorType(island.getUniqueId(), GeneratorCategory.valueOf(type.name())) : generatorType;
        if (generatorType == null) {
            return;
        }

        event.setCancelled(true);

        // Blok listesinden rastgele bir blok seç
        Material generated = getRandomBlock(generatorType.getBlockChances());
        block.setType(generated);
        playEffects(block);
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

    private Material getRandomBlock(Map<Material, Double> blockChances) {
        double totalWeight = 0.0;

        for (double weight : blockChances.values()) {
            totalWeight += weight;
        }

        double random = rand.nextDouble() * totalWeight;
        double current = 0.0;

        for (Map.Entry<Material, Double> entry : blockChances.entrySet()) {
            current += entry.getValue();
            if (random <= current) {
                return entry.getKey();
            }
        }

        // Yedek değer (teorik olarak buraya düşmemeli)
        return Material.BARRIER;
    }
}
