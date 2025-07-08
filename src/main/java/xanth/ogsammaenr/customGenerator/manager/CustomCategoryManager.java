package xanth.ogsammaenr.customGenerator.manager;

import org.bukkit.Material;
import xanth.ogsammaenr.customGenerator.model.CustomGeneratorCategory;

import java.util.*;

public class CustomCategoryManager {
    private final Map<String, CustomGeneratorCategory> categoryMap = new HashMap<>();

    private final Set<Material> relevantBlockMaterials = new HashSet<>();

    public void addCategory(CustomGeneratorCategory category) {
        categoryMap.put(category.getId(), category);
    }

    public CustomGeneratorCategory getCategoryById(String id) {
        return categoryMap.get(id);
    }

    public Collection<CustomGeneratorCategory> getAllCategories() {
        return categoryMap.values();
    }

    public Map<String, CustomGeneratorCategory> getCategoryMap() {
        return categoryMap;
    }

    public boolean isGeneratorRelevantBlock(Material material) {
        return relevantBlockMaterials.contains(material);
    }

    public Set<Material> getRelevantBlockMaterials() {
        return relevantBlockMaterials;
    }

    public void clear() {
        categoryMap.clear();
    }
}
