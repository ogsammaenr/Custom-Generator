package xanth.ogsammaenr.customGenerator.manager;

import xanth.ogsammaenr.customGenerator.CustomGenerator;
import xanth.ogsammaenr.customGenerator.model.GeneratorCategory;
import xanth.ogsammaenr.customGenerator.model.GeneratorType;
import xanth.ogsammaenr.customGenerator.storage.IslandGeneratorDAO;

import java.util.*;

public class IslandGeneratorManager {
    private final CustomGenerator plugin;
    private final IslandGeneratorDAO dao;

    /// Ada ID → Kategori → Aktif jeneratör tipi ID
    private final Map<String, Map<GeneratorCategory, String>> activeGeneratorTypes = new HashMap<>();

    /// Ada ID → Satın alınan jeneratör tipi ID
    private final Map<String, Set<String>> ownedGeneratorTypes = new HashMap<>();

    /// Tanımlı jeneratör tipleri
    private final Map<String, GeneratorType> registeredTypes = new HashMap<>();

    public IslandGeneratorManager(CustomGenerator plugin, IslandGeneratorDAO islandGeneratorDAO) {
        this.plugin = plugin;
        this.dao = islandGeneratorDAO;
    }

    // ==========================
    // = Ada işlemleri =
    // ==========================

    /// Ada için aktif jeneratör tipi ayarla
    public void setGeneratorType(String islandId, String typeId) {
        GeneratorType type = getRegisteredType(typeId);
        if (type == null) {
            plugin.getLogger().warning("Bilinmeyen jeneratör tipi: " + typeId);
            return;
        }

        activeGeneratorTypes.putIfAbsent(islandId, new HashMap<>());
        activeGeneratorTypes.get(islandId).put(type.getGeneratorCategory(), typeId);
    }

    /// Ada için aktif jeneratör tipi ID'sini al
    public String getGeneratorTypeId(String islandId, GeneratorCategory category) {
        return activeGeneratorTypes.getOrDefault(islandId, Collections.emptyMap()).get(category);
    }

    /// Ada için aktif jeneratör tipi nesnesini al
    public GeneratorType getGeneratorType(String islandId, GeneratorCategory category) {
        String typeId = getGeneratorTypeId(islandId, category);
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

    public Map<GeneratorCategory, String> getActiveTypes(String islandId) {
        return activeGeneratorTypes.getOrDefault(islandId, Collections.emptyMap());
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

    public String getRegisteredTypeString() {
        String typeId = "";
        for (GeneratorType type : registeredTypes.values()) {
            typeId += type.getId() + " ";
        }
        return typeId;
    }

    public Map<String, Set<String>> getOwnedGeneratorTypes() {
        return ownedGeneratorTypes;
    }

    public Map<String, Map<GeneratorCategory, String>> getActiveGeneratorTypes() {
        return activeGeneratorTypes;
    }


}
