package fr.dornacraft.mailbox.listeners;

import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import fr.dornacraft.mailbox.Main;
import fr.dornacraft.mailbox.inventory.builders.InventoryProviderBuilder;
import fr.dornacraft.mailbox.inventory.providers.utils.AuthorFilter;

public class PlayerChatSelector implements Listener {
	
	private Player player;
	private AuthorFilter authorFilter;
	private InventoryProviderBuilder parent;
	
	public PlayerChatSelector(Player player, AuthorFilter authorFilter, InventoryProviderBuilder parent) {
		this.setPlayer(player);
		this.setAuthorFilter(authorFilter);
		this.setParent(parent);
	}
	
	public PlayerChatSelector(Player player, AuthorFilter authorFilter) {
		this.setPlayer(player);
		this.setAuthorFilter(authorFilter);
	}
	
	public void start() {
		this.getPlayer().sendMessage("A qui voulez vous envoyez la lettre ?");
		Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
		
	}
	
	public void stop(){
		AsyncPlayerChatEvent.getHandlerList().unregister(this);
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	private void onPlayerChatSelection(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		String msg = event.getMessage();
		
		if(player.equals(this.getPlayer()) ) {
			event.setCancelled(true);
			List<String> splitedMsg = Arrays.asList(msg.split(",") );
			String wrongName = this.getAuthorFilter().addAllIdentifiers(splitedMsg);
			
			if(wrongName == null) {
				player.sendMessage("Vous avez choisit de cibler " + splitedMsg.size() + " joueurs.");

				if(this.getParent() != null) {
					this.getParent().openInventory(this.getPlayer() );
					
				}
				
				this.stop();
			} else {
				player.sendMessage("Action impossible: le joueur " + wrongName + " n'a pas été trouvé, veuillez réessayer.");
				
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

	public AuthorFilter getAuthorFilter() {
		return authorFilter;
	}

	private void setAuthorFilter(AuthorFilter authorFilter) {
		this.authorFilter = authorFilter;
	}
}
