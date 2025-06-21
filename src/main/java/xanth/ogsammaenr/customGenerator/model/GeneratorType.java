package xanth.ogsammaenr.customGenerator.model;

import org.bukkit.Material;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GeneratorType {
    private final String id; // Örn: "coal"
    private final String displayName; // Menüde gösterilecek isim
    private final Material icon; // Menüdeki ikon (örn: COAL_ORE)
    private final List<String> lore; // Menü açıklaması
    private final GeneratorCategory generatorCategory; // COBBLESTONE / STONE / BASALT
    private final double price; // Vault ile satın alma bedeli
    private final int requiredIslandLevel; // Gereken ada seviyesi
    private final Map<Material, Double> blockChances; // Blok oranları
    private int priority; // Yükleme sırasına göre atanacak (otomatik)

    public GeneratorType(
            String id,
            String displayName,
            Material icon,
            List<String> lore,
            GeneratorCategory generatorCategory,
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
        this.blockChances = new LinkedHashMap<>(blockChances); // Sıralı tutmak için LinkedHashMap
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public List<String> getLore() {
        return lore;
    }

    public GeneratorCategory getGeneratorCategory() {
        return generatorCategory;
    }

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

    public void setPriority(int priority) {
        this.priority = priority;
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
