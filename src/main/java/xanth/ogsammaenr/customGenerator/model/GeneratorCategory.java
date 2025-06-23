package xanth.ogsammaenr.customGenerator.model;

public enum GeneratorCategory {
    COBBLESTONE(""),
    STONE(""),
    BASALT(""),
    DEEPSLATE(""),
    ;

    private String displayName;

    GeneratorCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
