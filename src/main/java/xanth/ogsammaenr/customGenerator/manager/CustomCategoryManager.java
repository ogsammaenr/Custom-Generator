package xanth.ogsammaenr.customGenerator.manager;

import xanth.ogsammaenr.customGenerator.model.CustomGeneratorCategory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CustomCategoryManager {
    private final Map<String, CustomGeneratorCategory> categoryMap = new HashMap<>();

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

    public void clear() {
        categoryMap.clear();
    }
}
