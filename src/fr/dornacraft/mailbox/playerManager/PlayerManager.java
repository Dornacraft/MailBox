package fr.dornacraft.mailbox.playerManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.entity.Player;

public class PlayerManager {
	
	private static PlayerManager INSTANCE = new PlayerManager();
	private PlayerInfoSQL playerInfoSql = new PlayerInfoSQL();
	public static PlayerManager getInstance() {
		return INSTANCE;
		
	}
	
	private Map<String, UUID> map = new HashMap<>();
	
	private PlayerManager() {
		
	}
	
	public void init() {
		for(PlayerInfo pi : playerInfoSql.getAll()){
			map.put(pi.getName(), pi.getUuid());
		}
		
	}
	
	public void load(Player player) {
		PlayerInfo pi = playerInfoSql.tryRegister(player);
		this.map.put(pi.getName(), pi.getUuid());
		
	}
	
	public UUID getUUID(String name) {
		return this.map.get(name);
	}
	
	public String getName(UUID uuid) {
		String res = null;
		
		for(Entry<String, UUID> entry : this.map.entrySet()) {
			if(entry.getValue().equals(uuid)) {
				res = entry.getKey();
				break;
			}
			
		}
		
		return res;
	}
	
	public PlayerInfo getPlayerInfo(String name) {
		UUID uuid = this.getUUID(name);
		PlayerInfo res = null;
		
		if(uuid != null) {
			res = new PlayerInfo(name, this.map.get(name));
		}
		
		return res;
	}
	
	public Map<String, UUID> getCache(){
		Map<String, UUID> clone = new HashMap<>();
		clone.putAll(this.map);
		return clone;
	}
}
