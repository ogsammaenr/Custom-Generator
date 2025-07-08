package xanth.ogsammaenr.customGenerator.model;

import java.util.ArrayList;
import java.util.List;

public enum GeneratorCategory implements IGeneratorCategory {
    COBBLESTONE(""),
    STONE(""),
    BASALT(""),
    DEEPSLATE(""),
    ;

    private String displayName;

    GeneratorCategory(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getId() {
        return this.name();
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<String> idList() {
        List<String> list = new ArrayList<String>();
        for (GeneratorCategory category : GeneratorCategory.values()) {
            list.add(category.getId());
        }
        return list;
    }
}
