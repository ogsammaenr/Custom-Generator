package xanth.ogsammaenr.customGenerator.model;

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
}
