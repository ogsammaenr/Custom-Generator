package xanth.ogsammaenr.customGenerator.model;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xanth.ogsammaenr.customGenerator.util.ItemBuilder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GeneratorType {
    private final String id;
    private final String displayName;
    private final Material icon;
    private final List<String> lore;
    private final IGeneratorCategory generatorCategory;
    private final double price;
    private final int requiredIslandLevel;
    private final Map<Material, Double> blockChances;
    private int priority;

    public GeneratorType(
            String id,
            String displayName,
            Material icon,
            List<String> lore,
            IGeneratorCategory generatorCategory,
            double price,
            int requiredIslandLevel,
            Map<Material, Double> blockChances
    ) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.lore = lore;
        this.generatorCategory = generatorCategory;
        this.price = price;
        this.requiredIslandLevel = requiredIslandLevel;
        this.blockChances = new LinkedHashMap<>(blockChances);
    }

    /*/ Getters /*/


    /**
     * @return Generator Id
     */
    public String getId() {
        return id;
    }

    /**
     * @return Generator Display Name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return Display Material
     */
    public Material getIcon() {
        return icon;
    }

    /**
     * @return Display Item Lore
     */
    public List<String> getLore() {
        return lore;
    }

    /**
     * @return Generator Category (Enum)
     */
    public IGeneratorCategory getGeneratorCategory() {
        return generatorCategory;
    }

    /**
     * @return Generator Type Price
     */
    public double getPrice() {
        return price;
    }

    public int getRequiredIslandLevel() {
        return requiredIslandLevel;
    }

    public Map<Material, Double> getBlockChances() {
        return blockChances;
    }

    public int getPriority() {
        return priority;
    }

    /// ------
    public void setPriority(int priority) {
        this.priority = priority;
    }

    public ItemStack buildItem() {
        return new ItemBuilder(icon)
                .setDisplayName(displayName)
                .setLore(lore)
                .setNBT("generator_id", id)
                .build();
    }

    @Override
    public String toString() {
        return "GeneratorType{" +
               "id='" + id + '\'' +
               ", displayName='" + displayName + '\'' +
               ", icon=" + icon +
               ", lore=" + lore +
               ", generatorCategory=" + generatorCategory +
               ", price=" + price +
               ", requiredIslandLevel=" + requiredIslandLevel +
               ", blockChances=" + blockChances +
               ", priority=" + priority +
               '}';
    }
}
