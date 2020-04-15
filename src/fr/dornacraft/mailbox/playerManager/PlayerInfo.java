package fr.dornacraft.mailbox.playerManager;

import java.util.UUID;

import org.bukkit.entity.Player;

public class PlayerInfo {
	
	private UUID uuid;
	private String name;
	
	public PlayerInfo(String name, UUID uuid) {
		this.setName(name);
		this.setUuid(uuid);
	}
	
	public PlayerInfo(Player player) {
		this.setName(player.getName());
		this.setUuid(player.getUniqueId());
	}

	public UUID getUuid() {
		return this.uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}