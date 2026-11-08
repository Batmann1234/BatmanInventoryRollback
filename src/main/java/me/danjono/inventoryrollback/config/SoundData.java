package me.danjono.inventoryrollback.config;

import com.nuclyon.technicallycoded.inventoryrollback.InventoryRollbackPlus;
import com.tcoded.lightlibs.bukkitversion.BukkitVersion;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class SoundData extends ConfigData {

	private InventoryRollbackPlus main;

	private static Sound teleport;
	private static boolean teleportEnabled;

	private static Sound inventoryRestored;
	private static boolean inventoryRestoreEnabled;

	private static Sound foodRestored;
	private static boolean foodRestoredEnabled;

	private static Sound hungerRestored;
	private static boolean hungerRestoredEnabled;

	private static Sound experienceRestored;
	private static boolean experienceRestoredEnabled;

	// ---- Sonidos del menu principal (configurables en config.yml > sounds.menu) ----
	private static boolean menuEnabled = true;
	private static float menuVolume = 0.8f;
	private static final Map<String, String> menuSounds = new HashMap<>();

	/** clave -> "sonido;tono". El sonido usa el nombre de Minecraft (ej: ui.button.click). */
	private static final String[][] MENU_SOUND_DEFAULTS = {
		{"open", "block.ender_chest.open;1.2"},
		{"close", "block.ender_chest.close;1.0"},
		{"player", "entity.experience_orb.pickup;1.0"},
		{"page", "item.book.page_turn;1.0"},
		{"info", "block.note_block.chime;1.4"},
		{"mode", "ui.button.click;1.2"},
		{"self", "entity.item.pickup;1.1"},
		{"refresh", "block.note_block.hat;1.3"},
		{"save-all", "entity.player.levelup;1.3"},
		{"toggle-on", "block.note_block.pling;1.7"},
		{"toggle-off", "block.note_block.bass;0.6"},
		{"reload", "block.beacon.activate;1.6"},
		{"error", "entity.villager.no;1.0"}
	};

	public SoundData() {
		this.main = InventoryRollbackPlus.getInstance();
	}

	private void loadMenuSounds() {
		menuEnabled = (boolean) getDefaultValue("sounds.menu.enabled", true);
		menuVolume = ((Number) getDefaultValue("sounds.menu.volume", 0.8)).floatValue();
		menuSounds.clear();
		for (String[] entry : MENU_SOUND_DEFAULTS) {
			menuSounds.put(entry[0], String.valueOf(getDefaultValue("sounds.menu." + entry[0], entry[1])));
		}
	}

	/** Reproduce el sonido de un boton del menu (formato configurable: "sonido;tono"). */
	public static void playMenu(Player player, String key) {
		if (!menuEnabled || player == null) return;

		String value = menuSounds.get(key);
		if (value == null || value.trim().isEmpty()) return;

		String[] parts = value.split(";");
		float pitch = 1.0f;
		if (parts.length > 1) {
			try {
				pitch = Float.parseFloat(parts[1].trim());
			} catch (NumberFormatException ignored) {
			}
		}

		try {
			player.playSound(player.getLocation(), parts[0].trim(), menuVolume, pitch);
		} catch (Exception ignored) {
			// Sonido invalido en esta version: se ignora
		}
	}

	public void setSounds() {	    
		loadMenuSounds();

	    //If sounds are invalid they will be disabled.
		try {
			setTeleport(Sound.ENTITY_ENDERMAN_TELEPORT);
		} catch (NoSuchFieldError e) {
            if (this.main.getVersion().lessOrEqThan(BukkitVersion.v1_8_R3)) {
                setTeleport(Sound.valueOf("ENDERMAN_TELEPORT"));
            } else if (this.main.getVersion().isWithin(BukkitVersion.v1_9_R1, BukkitVersion.v1_12_R1)) {
                setTeleport(Sound.valueOf("ENTITY_ENDERMEN_TELEPORT"));
            }
		}
		
		if (teleport != null)
		    setTeleportEnabled((boolean) getDefaultValue("sounds.teleport.enabled", true));
		
		try {
			setInvetoryRestored(Sound.ENTITY_ENDER_DRAGON_FLAP);
		} catch (NoSuchFieldError e) {
            if (this.main.getVersion().lessOrEqThan(BukkitVersion.v1_8_R3)) {
                setInvetoryRestored(Sound.valueOf("ENDERDRAGON_WINGS"));
            } else if (this.main.getVersion().isWithin(BukkitVersion.v1_9_R1, BukkitVersion.v1_12_R1)) {
                setInvetoryRestored(Sound.valueOf("ENTITY_ENDERDRAGON_FLAP"));
            }
		}
		
		if (inventoryRestored != null)
		    setInventoryRestoredEnabled((boolean) getDefaultValue("sounds.inventory.enabled", true));

		try {
			setFoodRestored(Sound.ENTITY_GENERIC_EAT);
		} catch (NoSuchFieldError e) {
            if (this.main.getVersion().lessOrEqThan(BukkitVersion.v1_8_R3)) {
                setFoodRestored(Sound.valueOf("EAT"));
            } else if (this.main.getVersion().isWithin(BukkitVersion.v1_9_R1, BukkitVersion.v1_12_R1)) {
                setFoodRestored(Sound.valueOf("ENTITY_GENERIC_EAT"));
            }
		}
		
		if (foodRestored != null)
		    setFoodRestoredEnabled((boolean) getDefaultValue("sounds.food.enabled", true));

		try {
			setHungerRestored(Sound.ENTITY_HORSE_EAT);
		} catch (NoSuchFieldError e) {
            if (this.main.getVersion().lessOrEqThan(BukkitVersion.v1_8_R3)) {
                setHungerRestored(Sound.valueOf("HORSE_IDLE"));
            } else if (this.main.getVersion().isWithin(BukkitVersion.v1_9_R1, BukkitVersion.v1_12_R1)) {
                setHungerRestored(Sound.valueOf("ENTITY_HORSE_EAT"));
            }
		}
		
		if (hungerRestored != null)
		    setHungerRestoredEnabled((boolean) getDefaultValue("sounds.hunger.enabled", true));

		try {
			setExperienceSound(Sound.ENTITY_PLAYER_LEVELUP);
		} catch (NoSuchFieldError e) {
            if (this.main.getVersion().lessOrEqThan(BukkitVersion.v1_8_R3)) {
                setExperienceSound(Sound.valueOf("LEVEL_UP"));
            } else if (this.main.getVersion().isWithin(BukkitVersion.v1_9_R1, BukkitVersion.v1_12_R1)) {
                setExperienceSound(Sound.valueOf("ENTITY_PLAYER_LEVELUP"));
            }
		}
		
		if (experienceRestored != null)
		    setExperienceRestoredEnabled((boolean) getDefaultValue("sounds.xp.enabled", true));
		
	}
	
	public static void setTeleport(Sound value) {
        teleport = value;
    }
    
    public static void setTeleportEnabled(boolean value) {
        teleportEnabled = value;
    }
    
    public static void setInvetoryRestored(Sound value) {
        inventoryRestored = value;
    }
    
    public static void setInventoryRestoredEnabled(boolean value) {
        inventoryRestoreEnabled = value;
    }
    
    public static void setFoodRestored(Sound value) {
        foodRestored = value;
    }
    
    public static void setFoodRestoredEnabled(boolean value) {
        foodRestoredEnabled = value;
    }
    
    public static void setHungerRestored(Sound value) {
        hungerRestored = value;
    }
    
    public static void setHungerRestoredEnabled(boolean value) {
        hungerRestoredEnabled = value;
    }
    
    public static void setExperienceSound(Sound value) {
        experienceRestored = value;
    }
    
    public static void setExperienceRestoredEnabled(boolean value) {
        experienceRestoredEnabled = value;
    }
	
	public static Sound getTeleport() {
	    return teleport;
	}
	
	public static boolean isTeleportEnabled() {
	    return teleportEnabled;
	}
	
	public static Sound getInventoryRestored() {
	    return inventoryRestored;
	}
	
	public static boolean isInventoryRestoreEnabled() {
	    return inventoryRestoreEnabled;
	}
	
	public static Sound getFoodRestored() {
	    return foodRestored;
	}
	
	public static boolean isFoodRestoredEnabled() {
        return foodRestoredEnabled;
    }
	
	public static Sound getHungerRestored() {
	    return hungerRestored;
	}
	
	public static boolean isHungerRestoredEnabled() {
        return hungerRestoredEnabled;
    }
	
	public static Sound getExperienceSound() {
	    return experienceRestored;
	}
	
	public static boolean isExperienceRestoredEnabled() {
        return experienceRestoredEnabled;
    }

}
