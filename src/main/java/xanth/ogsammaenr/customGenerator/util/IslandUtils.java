package xanth.ogsammaenr.customGenerator.util;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.request.AddonRequestBuilder;
import world.bentobox.bentobox.managers.IslandsManager;

import java.util.UUID;

public class IslandUtils {
    private final BentoBox bentoBox;
    private final IslandsManager islandsManager;

    public IslandUtils() {
        this.bentoBox = BentoBox.getInstance();
        this.islandsManager = bentoBox.getIslandsManager();
    }

    /**
     * Returns the level of this player's island in the given world.
     *
     * @param playerUUID UUID of the player, not null.
     * @param worldName  Name of the world (Overworld) the island is in, not null.
     * @return the player's island level or {@code 0L} if the input was invalid or
     * if this player does not have an island in this world.
     */
    public long getIslandLevel(UUID playerUUID, String worldName) {
        return (Long) new AddonRequestBuilder()
                .addon("Level")
                .label("island-level")
                .addMetaData("world-name", worldName)
                .addMetaData("player", playerUUID)
                .request();
    }
    
}
