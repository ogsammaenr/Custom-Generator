package xanth.ogsammaenr.customGenerator.manager;

import xanth.ogsammaenr.customGenerator.CustomGenerator;
import xanth.ogsammaenr.customGenerator.model.GeneratorType;

import java.util.*;

public class IslandGeneratorManager {
    private final CustomGenerator plugin;

    /// Ada ID → Aktif jeneratör tipi ID (ör: "coal")
    private final Map<String, String> activeGeneratorTypes = new HashMap<>();

    /// Ada ID → Satın alınmış jeneratör tipi ID'leri (ör: "coal", "iron")
    private final Map<String, Set<String>> ownedGeneratorTypes = new HashMap<>();

    /// Plugin'de tanımlanmış jeneratör tipleri
    private final Map<String, GeneratorType> registeredTypes = new HashMap<>();

    public IslandGeneratorManager(CustomGenerator plugin) {
        this.plugin = plugin;
    }

    // ==========================
    // = Ada işlemleri =
    // ==========================

    /// Ada için aktif jeneratör tipi ayarla
    public void setGeneratorType(String islandId, String typeId) {
        if (!isRegistered(typeId)) {
            plugin.getLogger().warning("Bilinmeyen jeneratör tipi: " + typeId);
            return;
        }

        activeGeneratorTypes.put(islandId, typeId);
    }

    /// Ada için aktif jeneratör tipi ID'sini al
    public String getGeneratorTypeId(String islandId) {
        return activeGeneratorTypes.get(islandId);
    }

    /// Ada için aktif jeneratör tipi nesnesini al
    public GeneratorType getGeneratorType(String islandId) {
        String typeId = getGeneratorTypeId(islandId);
        return typeId == null ? null : registeredTypes.get(typeId);
    }

    /// Ada belirli bir jeneratör tipine sahip mi?
    public boolean islandOwnsType(String islandId, String typeId) {
        return ownedGeneratorTypes.containsKey(islandId)
                && ownedGeneratorTypes.get(islandId).contains(typeId);
    }

    /// Adanın sahip olduğu jeneratör tiplerini getir
    public Set<String> getOwnedTypes(String islandId) {
        return ownedGeneratorTypes.getOrDefault(islandId, Collections.emptySet());
    }

    /// Adanın jeneratör tipi satın almasını kaydet
    public void addOwnedType(String islandId, String typeId) {
        if (!isRegistered(typeId)) {
            plugin.getLogger().warning("Bilinmeyen jeneratör tipi satın alındı: " + typeId);
            return;
        }

        ownedGeneratorTypes.putIfAbsent(islandId, new HashSet<>());
        ownedGeneratorTypes.get(islandId).add(typeId);
    }

    // ==========================
    // = Jeneratör tipi işlemleri =
    // ==========================

    public void registerGeneratorType(GeneratorType type) {
        registeredTypes.put(type.getId(), type);
    }

    public boolean isRegistered(String typeId) {
        return registeredTypes.containsKey(typeId);
    }

    public GeneratorType getRegisteredType(String typeId) {
        return registeredTypes.get(typeId);
    }

    public Map<String, GeneratorType> getAllRegisteredTypes() {
        return registeredTypes;
    }

    // ==========================
    // = Veri yükleme / kaydetme =
    // ==========================

    public void loadIslandData() {
        // TODO: Ada jeneratör verilerini diskten yükle
    }

    public void saveIslandData() {
        // TODO: Ada jeneratör verilerini diske kaydet
    }
}
