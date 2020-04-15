package fr.dornacraft.mailbox.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import fr.dornacraft.mailbox.Main;
import fr.dornacraft.mailbox.DataManager.filters.Filter;
import fr.dornacraft.mailbox.inventory.builders.InventoryProviderBuilder;
import fr.dornacraft.mailbox.playerManager.PlayerInfo;
import fr.dornacraft.mailbox.playerManager.PlayerManager;

public class PlayerChatSelector implements Listener {
	
	private Player player;
	private Filter<String> authorsFilter;
	private InventoryProviderBuilder parent;
	
	public PlayerChatSelector(Player player, Filter<String> playerFilter, InventoryProviderBuilder parent) {
		this.setPlayer(player);
		this.setAuthorsFilter(playerFilter);
		this.setParent(parent);
	}
	
	public PlayerChatSelector(Player player, Filter<String> playerFilter) {
		this.setPlayer(player);
		this.setAuthorsFilter(playerFilter);
	}
	
	public void start() {
		this.getPlayer().sendMessage("A qui voulez vous envoyez la lettre ?");
		Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
		
	}
	
	public void stop(){
		AsyncPlayerChatEvent.getHandlerList().unregister(this);
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	private void onPlayerChatSelection(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		String msg = event.getMessage();
		
		if(player.equals(this.getPlayer()) ) {
			event.setCancelled(true);
			List<String> splitedMsg = Arrays.asList(msg.replace(" ", "").split(",") );
			Boolean isGood = true;
			String firstNotFound = null;
			List<String> tempList = new ArrayList<>();
			
			for(String name : splitedMsg) {
				PlayerInfo pi = PlayerManager.getInstance().getPlayerInfo(name);
				
				if(pi != null) {
					tempList.add(pi.getName() );
					
				} else {
					firstNotFound = name;
					isGood = false;
					break;
				}
			}
			
			if(isGood) {
				this.getAuthorsFilter().addAllOnce(tempList);
				player.sendMessage("Vous avez choisit de cibler " + tempList.size() + " joueurs.");
				
				this.stop();
				if(this.getParent() != null) {
					this.getParent().openInventory(this.getPlayer() );
					
				}
				
			} else {
				player.sendMessage("Action impossible: le joueur " + firstNotFound + " n'a pas été trouvé, veuillez réessayer.");
			}

		}
		
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public InventoryProviderBuilder getParent() {
		return parent;
	}

	public void setParent(InventoryProviderBuilder parent) {
		this.parent = parent;
	}
	
	public Filter<String> getAuthorsFilter() {
		return authorsFilter;
	}

	public void setAuthorsFilter(Filter<String> authorsFilter) {
		this.authorsFilter = authorsFilter;
	}
}
