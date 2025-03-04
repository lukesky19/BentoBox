package world.bentobox.bentobox.listeners.flags.settings;

import java.util.Optional;

import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.raid.RaidFinishEvent;
import org.bukkit.event.raid.RaidTriggerEvent;
import org.bukkit.potion.PotionEffectType;

import world.bentobox.bentobox.api.flags.Flag;
import world.bentobox.bentobox.api.flags.FlagListener;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.lists.Flags;
import world.bentobox.bentobox.util.Util;

/**
 * Handles natural mob spawning.
 * @author tastybento
 */
public class MobSpawnListener extends FlagListener {
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onMobSpawnEvent(PreCreatureSpawnEvent event) {
        if(!this.getIWM().inWorld(event.getSpawnLocation())) return;

        switch (event.getReason()) {
            // Natural
            case DEFAULT, DROWNED, JOCKEY, LIGHTNING, MOUNT, NATURAL, NETHER_PORTAL, OCELOT_BABY, PATROL,
                 RAID, REINFORCEMENTS, SILVERFISH_BLOCK, TRAP, VILLAGE_DEFENSE, VILLAGE_INVASION -> {
                boolean cancelNatural = this.shouldCancel(event.getType(),
                        event.getSpawnLocation(),
                        Flags.ANIMAL_NATURAL_SPAWN,
                        Flags.MONSTER_NATURAL_SPAWN);
                event.setCancelled(cancelNatural);
            }

            case SPAWNER -> {
                boolean cancelSpawners = this.shouldCancel(event.getType(),
                        event.getSpawnLocation(),
                        Flags.ANIMAL_SPAWNERS_SPAWN,
                        Flags.MONSTER_SPAWNERS_SPAWN);
                event.setCancelled(cancelSpawners);
            }
        }
    }


    /**
     * This prevents to start a raid if mob spawning rules prevents it.
     * @param event RaidTriggerEvent
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onRaidStartEvent(RaidTriggerEvent event) {
        // If not in the right world exit immediately.
        if(!this.getIWM().inWorld(event.getWorld())) return;

        Optional<Island> island = getIslands().getIslandAt(event.getPlayer().getLocation());

        if(island.map(i -> !i.isAllowed(Flags.MONSTER_NATURAL_SPAWN)).orElseGet(
                () -> !Flags.MONSTER_NATURAL_SPAWN.isSetForWorld(event.getWorld()))) {
            // Monster spawning is disabled on island or world. Cancel the raid.
            event.setCancelled(true);
        }
    }


    /**
     * This removes HERO_OF_THE_VILLAGE from players that cheated victory.
     * @param event RaidFinishEvent
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onRaidFinishEvent(RaidFinishEvent event) {
        // If not in the right world exit immediately.
        if (!this.getIWM().inWorld(event.getWorld())) return;

        Optional<Island> island = getIslands().getIslandAt(event.getRaid().getLocation());

        if(island.map(i -> !i.isAllowed(Flags.MONSTER_NATURAL_SPAWN)).orElseGet(
                () -> !Flags.MONSTER_NATURAL_SPAWN.isSetForWorld(event.getWorld()))) {
            // CHEATERS. PUNISH THEM.
            event.getWinners().forEach(player -> {
                if (player.isOnline())
                {
                    player.removePotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE);
                }
            });
        }
    }

    /**
     * Prevents mobs spawning naturally
     * @param event - event
     */
    void onMobSpawn(PreCreatureSpawnEvent event) {
        boolean result = this.getIWM().inWorld(event.getSpawnLocation());
        if(!result) return;

        switch(event.getReason()) {
            // Natural
            case DEFAULT, DROWNED, JOCKEY, LIGHTNING, MOUNT, NATURAL, NETHER_PORTAL, OCELOT_BABY, PATROL,
                 RAID, REINFORCEMENTS, SILVERFISH_BLOCK, TRAP, VILLAGE_DEFENSE, VILLAGE_INVASION -> {
                boolean cancelNatural = this.shouldCancel(event.getType(),
                        event.getSpawnLocation(),
                        Flags.ANIMAL_NATURAL_SPAWN,
                        Flags.MONSTER_NATURAL_SPAWN);
                event.setCancelled(cancelNatural);
            }

            case SPAWNER -> {
                boolean cancelSpawners = this.shouldCancel(event.getType(),
                        event.getSpawnLocation(),
                        Flags.ANIMAL_SPAWNERS_SPAWN,
                        Flags.MONSTER_SPAWNERS_SPAWN);
                event.setCancelled(cancelSpawners);
            }
        }
    }


    /**
     * This method checks if entity should be cancelled from spawning in given location base on flag values.
     * @param entity Entity that is checked.
     * @param loc location where entity is spawned.
     * @param animalSpawnFlag Animal Spawn Flag.
     * @param monsterSpawnFlag Monster Spawn Flag.
     * @return {@code true} if flag prevents entity to spawn, {@code false} otherwise.
     */
    private boolean shouldCancel(EntityType entity, Location loc, Flag animalSpawnFlag, Flag monsterSpawnFlag) {
        Optional<Island> island = getIslands().getIslandAt(loc);

        if(Util.isHostileEntityType(entity) && !(entity.equals(EntityType.PUFFERFISH))) {
            return island.map(i -> !i.isAllowed(monsterSpawnFlag)).
                    orElseGet(() -> !monsterSpawnFlag.isSetForWorld(loc.getWorld()));
        } else if(Util.isPassiveEntityType(entity) || entity.equals(EntityType.PUFFERFISH)) {
            return island.map(i -> !i.isAllowed(animalSpawnFlag)).
                    orElseGet(() -> !animalSpawnFlag.isSetForWorld(loc.getWorld()));
        } else {
            return false;
        }
    }
}