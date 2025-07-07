package xanth.ogsammaenr.customGenerator.model;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomGeneratorCategory implements IGeneratorCategory {
    private final String categoryId;
    private final String displayName;

    private final Material fluid;
    private final @Nullable Material to;

    private final EnumMap<Direction, List<Material>> blockConditions;

    private final Integer minYLevel;
    private final Integer maxYLevel;
    private final Set<Biome> biomes;

    public CustomGeneratorCategory(String categoryId,
                                   String displayName,
                                   Material fluid,
                                   Material to,
                                   EnumMap<Direction, List<Material>> blockConditions,
                                   Integer minYLevel,
                                   Integer maxYLevel,
                                   Set<Biome> biomes
    ) {
        this.categoryId = categoryId;
        this.displayName = displayName;
        this.fluid = fluid;
        this.to = to;
        this.blockConditions = blockConditions != null ? blockConditions : new EnumMap<>(Direction.class);
        this.minYLevel = minYLevel;
        this.maxYLevel = maxYLevel;
        this.biomes = biomes != null ? biomes : new HashSet<>();
    }

    /**
     * @return true eğer to değişkeni atanmışsa
     */
    public boolean hasToBlock() {
        return to != null;
    }

    /**
     * @return true eğer yleveli geçerliyse
     */
    public boolean matchesY(int y) {
        if (minYLevel != null && y < minYLevel) return false;
        if (maxYLevel != null && y > maxYLevel) return false;
        return true;
    }

    /**
     * @return true eğer biome atanmışsa
     */
    public boolean hasBiomeRestriction() {
        return !biomes.isEmpty();
    }

    /*  ==========  GETTERS  ========== */

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getId() {
        return categoryId;
    }

    public EnumMap<Direction, List<Material>> getBlockConditions() {
        return blockConditions;
    }

    public Integer getMaxYLevel() {
        return maxYLevel;
    }

    public Integer getMinYLevel() {
        return minYLevel;
    }

    public Material getFluid() {
        return fluid;
    }

    public @Nullable Material getTo() {
        return to;
    }

    public Set<Biome> getBiomes() {
        return biomes;
    }

    public enum Direction {
        SIDES,
        UP,
        DOWN
    }
}
