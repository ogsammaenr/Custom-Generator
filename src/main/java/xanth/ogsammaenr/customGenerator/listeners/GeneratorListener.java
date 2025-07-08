package xanth.ogsammaenr.customGenerator.listeners;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
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

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GeneratorListener implements Listener {
    private final CustomGenerator plugin;
    private final IslandGeneratorManager generatorManager;
    private final CustomCategoryManager categoryManager;
    private final IslandsManager islandsManager;
    private final Random rand = new Random();

    public GeneratorListener(CustomGenerator plugin) {
        this.plugin = plugin;
        this.generatorManager = plugin.getIslandGeneratorManager();
        this.categoryManager = plugin.getCustomCategoryManager();
        this.islandsManager = BentoBox.getInstance().getIslandsManager();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFlow(BlockFromToEvent event) {
        Block source = event.getBlock();
        Block target = event.getToBlock();
        Material liquid = source.getType();

        //  Liquid Check
        if (!source.isLiquid()) return;
        if (source == target) return;
        if (target.getType().equals(liquid)) return;

        //  Get Island
        Optional<Island> islandOpt = islandsManager.getIslandAt(event.getBlock().getLocation());
        if (islandOpt.isEmpty()) return;
        Island island = islandOpt.get();

        //  Custom Category
        CustomGeneratorCategory matched = categoryManager.getAllCategories().stream()
                .filter(cat -> matchesConditions(cat, source, target))
                .findFirst()
                .orElse(null);

        if (matched != null) {
            GeneratorType generator = generatorManager.getGeneratorType(island.getUniqueId(), matched.getId());
            if (generator != null) {
                Material result = pickResult(generator.getBlockChances());
                if (result != null) {
                    target.setType(result, false);
                    playEffects(target);
                    event.setCancelled(true);
                    return;
                }
            }
        }

        //  Vanilla Category
        Material type = null;
        Block blockToChange = target;

        // taş oluşup oluşmayacağını kontrol et
        if (canGenerateStone(liquid, target)) {
            type = Material.STONE;
        }

        // lav akmasıyla kırıktaş oluşup oluşmayacağını kontrol et
        else if (liquid == Material.LAVA && canLavaGenerateCobblestone(target, event.getFace())) {
            type = Material.COBBLESTONE;
        }

        // lav akmasıyla Bazalt oluşup oluşmayacağını kontrol et
        else if (liquid == Material.LAVA && canGenerateBasalt(target)) {
            type = Material.BASALT;
        }

        // su akmasıyla Kırıktaş oluşup oluşmayacağını Kontrol et
        else if (liquid == Material.WATER) {
            Block lavaBlock = getWaterGeneratedCobblestone(target, event.getFace());
            if (lavaBlock != null) {
                type = Material.COBBLESTONE;
                blockToChange = lavaBlock;
            }
        }

        if (type == null) return;

        GeneratorType generatorType = null;
        if (type == Material.STONE
            && blockToChange.getLocation().getBlockY() < 0
            && generatorManager.getGeneratorType(island.getUniqueId(), "DEEPSLATE") != null) {
            generatorType = generatorManager.getGeneratorType(island.getUniqueId(), "DEEPSLATE");
        }
        generatorType = generatorType == null
                ? generatorManager.getGeneratorType(island.getUniqueId(), type.name())
                : generatorType;

        //  Jeneratör Tipi Boş ise işlem yapma
        if (generatorType == null) {
            return;
        }

        event.setCancelled(true);

        Material result = pickResult(generatorType.getBlockChances());
        blockToChange.setType(result, false);
        playEffects(blockToChange);

    }


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

    /**
     * Bu metod, hedef bloğun taş bloğu üretebilir olup olmadığını döner. (Taş jeneratörleri için)
     * <p>
     * Taş sadece lav suyun üzerine aktığında üretilebilir. Taş her zaman suyun bulunduğu blokta oluşur.
     *
     * @param liquid      Akan sıvı.
     * @param targetBlock Sıvının akacağı blok.
     * @return true, eğer hedef bloğun altında su varsa ve sıvı lav ise ya da hedef bloğun üstünde lav varsa ve sıvı su ise.
     */
    private boolean canGenerateStone(Material liquid, Block targetBlock) {
        // Adding check if water flows under lava increases generation speed, as in next
        // ticks it will detect lava flow anyway.
        // Also it improves performance a bit, as it will run 1 event less.
        return liquid.equals(Material.LAVA) && targetBlock.getType().equals(Material.WATER);
    }


    /**
     * Bu metod, lavın kırıktaş (cobblestone) üretebilip üretemeyeceğini kontrol eder.
     * <p>
     * Lav, aktığı sonraki blok (hava bloğu) su içeren bloklarla bitişik ise kırıktaş üretir. Eğer bu doğruysa,
     * hava bloğu kırıktaş ile değiştirilir.
     *
     * @param airBlock      Kırıktaş ile değiştirilecek hava bloğu.
     * @param flowDirection Lavın akış yönü.
     * @return true, eğer lav kırıktaş üretebilecekse.
     */
    private boolean canLavaGenerateCobblestone(Block airBlock, BlockFace flowDirection) {
        return switch (flowDirection) {
            case NORTH, EAST, SOUTH, WEST ->
                // Since waterlogged blocks, it is also necessary to check block above.
                // Check if block in flow direction is water
                // Check if block on the left side is water
                // Check if block on the right side is water

                    GeneratorListener.containsWater(airBlock.getRelative(BlockFace.UP)) ||
                    GeneratorListener.containsWater(airBlock.getRelative(flowDirection)) ||
                    GeneratorListener.containsWater(airBlock
                            .getRelative(GeneratorListener.getClockwiseDirection(flowDirection))) ||
                    GeneratorListener.containsWater(airBlock
                            .getRelative(GeneratorListener.getCounterClockwiseDirection(flowDirection)));
            case DOWN ->
                // If lava flows down then we should search for water in horizontally adjacent blocks.

                    GeneratorListener.containsWater(airBlock.getRelative(BlockFace.NORTH)) ||
                    GeneratorListener.containsWater(airBlock.getRelative(BlockFace.EAST)) ||
                    GeneratorListener.containsWater(airBlock.getRelative(BlockFace.SOUTH)) ||
                    GeneratorListener.containsWater(airBlock.getRelative(BlockFace.WEST));
            default -> false;
        };
    }

    /**
     * Bu metod, bir lav bloğunun bazalt üretip üretemeyeceğini kontrol eder.
     * <p>
     * Minecraft'ın doğal jeneratör kurallarına göre bazalt, lav bloğu <b>soul soil</b>'in üzerinde
     * yer aldığında ve lav bloğuna bitişik herhangi bir yönde (kuzey, güney, doğu, batı veya üstünde) <b>blue ice</b> bulunduğunda üretilir.
     * <p>
     * Bu kontrol, bazalt jeneratörlerinin doğru çalışmasını sağlamak için gereklidir.
     *
     * @param lavaBlock Kontrol edilecek lav bloğu.
     * @return true, eğer lav bloğu bazalt üretmeye uygunsa.
     */
    private boolean canGenerateBasalt(Block lavaBlock) {
        // Lavın altındaki blok soul soil değilse zaten bazalt oluşmaz
        if (!lavaBlock.getRelative(BlockFace.DOWN).getType().equals(Material.SOUL_SOIL)) return false;

        // Lavın etrafında veya üstünde blue ice varsa bazalt oluşur
        return lavaBlock.getRelative(BlockFace.NORTH).getType() == Material.BLUE_ICE ||
               lavaBlock.getRelative(BlockFace.EAST).getType() == Material.BLUE_ICE ||
               lavaBlock.getRelative(BlockFace.SOUTH).getType() == Material.BLUE_ICE ||
               lavaBlock.getRelative(BlockFace.WEST).getType() == Material.BLUE_ICE ||
               lavaBlock.getRelative(BlockFace.UP).getType() == Material.BLUE_ICE;
    }


    /**
     * Bu metod, suyun kırıktaş üretebilip üretemeyeceğini kontrol eder.
     * <p>
     * Su, kırıktaş üretimini doğrudan lav bloğuna bitişik olduğu durumlarda yapar ve bu blok kaynak bloğu değildir.
     * <p>
     * Not: Minecraft’ın varsayılan mantığında, lav sadece su lavın üzerine akmaya çalışıyorsa kırıktaş veya obsidiyen ile değiştirilir.
     * Bu sayede lav ve su maksimum akış mesafesinde birbirine çarptığında kırıktaş oluşmaz.
     * Bu metod bu davranışı geçersiz kılar çünkü hava bloğuna komşu blokları kontrol eder. Bu kasıtlı yapılmıştır!
     *
     * @param airBlock      Su ile değiştirilecek hava bloğu.
     * @param flowDirection Suyun akış yönü.
     * @return Suya yakın lav bloğu (değiştirilecek) ya da eğer yakın lav yoksa null.
     */
    private Block getWaterGeneratedCobblestone(Block airBlock, BlockFace flowDirection) {
        Block checkBlock;

        switch (flowDirection) {
            case NORTH:
            case EAST:
            case SOUTH:
            case WEST:
                // Check if block in flow direction after airBlock is lava
                // Check if block on the left side of airBlock is lava
                // Check if block on the right side of airBlock is lava
                // If lava level is 0, then it will be transformed to obsidian. Not processed by current listener.

                checkBlock = airBlock.getRelative(flowDirection);

                if (GeneratorListener.isFlowingLavaBlock(checkBlock)) {
                    return checkBlock;
                }

                checkBlock = airBlock.getRelative(GeneratorListener.getClockwiseDirection(flowDirection));

                if (GeneratorListener.isFlowingLavaBlock(checkBlock)) {
                    return checkBlock;
                }

                checkBlock = airBlock.getRelative(GeneratorListener.getCounterClockwiseDirection(flowDirection));

                if (GeneratorListener.isFlowingLavaBlock(checkBlock)) {
                    return checkBlock;
                }

                return null;
            case DOWN:
                // If lava water flows down then we should search for lava under it and in horizontally adjacent blocks.
                // If lava level is 0, then it will be transformed to obsidian. Not processed by current listener.

                checkBlock = airBlock.getRelative(flowDirection);

                if (GeneratorListener.isFlowingLavaBlock(checkBlock)) {
                    return checkBlock;
                }

                checkBlock = airBlock.getRelative(BlockFace.NORTH);

                if (GeneratorListener.isFlowingLavaBlock(checkBlock)) {
                    return checkBlock;
                }

                checkBlock = airBlock.getRelative(BlockFace.EAST);

                if (GeneratorListener.isFlowingLavaBlock(checkBlock)) {
                    return checkBlock;
                }

                checkBlock = airBlock.getRelative(BlockFace.SOUTH);

                if (GeneratorListener.isFlowingLavaBlock(checkBlock)) {
                    return checkBlock;
                }

                checkBlock = airBlock.getRelative(BlockFace.WEST);

                if (GeneratorListener.isFlowingLavaBlock(checkBlock)) {
                    return checkBlock;
                }

                return null;
            default:
                return null;
        }
    }

    /* --------------------------------------------------------------------- */
    /* Section: Static methods                                               */
    /* --------------------------------------------------------------------- */


    /**
     * This method returns if target block is water block or contains water.
     *
     * @param block Block that must be checked.
     * @return true if block type is water or it is waterlogged.
     */
    private static boolean containsWater(Block block) {
        // Block Data contains information about the water logged status.
        // If block type is not water, we need to check if it is waterlogged.
        return block.getType().equals(Material.WATER) ||
               block.getBlockData().getAsString().contains("waterlogged=true");
    }


    /**
     * This method returns if given block contains lava and is not a source block.
     *
     * @param block Block that must be checked.
     * @return true if block contains lava and is not source block.
     */
    private static boolean isFlowingLavaBlock(Block block) {
        // Block Data contains information about the liquid level.
        // In our situation, we need to check if the level is not 0 when it is counted as
        // a source block.
        return block.getType().equals(Material.LAVA) &&
               !block.getBlockData().getAsString().contains("level=0");
    }


    /**
     * This method transforms input block face to next BlockFace by 90 degree in clockwise direction. Only on horizontal
     * pane for NORTH,EAST,SOUTH,WEST directions.
     *
     * @param face input BlockFace
     * @return Output BlockFace that is 90 degree from input BlockFace in clockwise direction
     */
    private static BlockFace getClockwiseDirection(BlockFace face) {
        return switch (face) {
            case NORTH -> BlockFace.EAST;
            case EAST -> BlockFace.SOUTH;
            case SOUTH -> BlockFace.WEST;
            case WEST -> BlockFace.NORTH;
            default ->
                // Not interested in other directions
                    face;
        };
    }


    /**
     * This method transforms input block face to next BlockFace by 90 degree in counter clockwise direction. Only on
     * horizontal pane for NORTH,EAST,SOUTH,WEST directions.
     *
     * @param face input BlockFace
     * @return Output BlockFace that is 90 degree from input BlockFace in counter clockwise direction
     */
    private static BlockFace getCounterClockwiseDirection(BlockFace face) {
        return switch (face) {
            case NORTH -> BlockFace.WEST;
            case EAST -> BlockFace.NORTH;
            case SOUTH -> BlockFace.EAST;
            case WEST -> BlockFace.SOUTH;
            default ->
                // Not interested in other directions
                    face;
        };
    }
}
