package fr.dornacraft.mailbox.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.InventoryView;

import fr.dornacraft.mailbox.Main;
import fr.dornacraft.mailbox.DataManager.Data;
import fr.dornacraft.mailbox.DataManager.LetterData;
import fr.dornacraft.mailbox.DataManager.LetterType;
import fr.dornacraft.mailbox.DataManager.MailBoxController;
import fr.dornacraft.mailbox.DataManager.factories.DataFactory;
import fr.dornacraft.mailbox.DataManager.factories.LetterDataFactory;
import fr.dornacraft.mailbox.inventory.providers.PlayerSelectorInventory;
import fr.dornacraft.mailbox.playerManager.PlayerManager;

public class LetterCreator implements Listener {
	
	private UUID uuid;
	private LetterDataFactory letterFactory;
	private List<String> recipients = new ArrayList<>();
	private Boolean[] steps = new Boolean[] {false, false, false};
	private PlayerSelectorInventory playerSelectorInventory = null;
	
	public LetterCreator(UUID uuid) {
		this.setUuid(uuid);
	}
	
	public void start() {
		Player player = Bukkit.getPlayer(this.getUuid());
		
		if(player != null) {
			Main.getInstance().getServer().getPluginManager().registerEvents(new LetterCreator(this.getUuid()), Main.getInstance());
			player.sendMessage("Vous pouvez a tout moment annuler la création de lettre en envoyant #stop");
			player.sendMessage("Quel est l'objet de la lettre ?");

		}
		
	}
	
	public void stop(){
		AsyncPlayerChatEvent.getHandlerList().unregister(this);
		InventoryCloseEvent.getHandlerList().unregister(this);

	}
	
	private void stepObject(Player player, String msg) {
		if(msg.length() <= 100) {
			letterFactory.setObject(msg);
			player.sendMessage("Objet de la lettre: \"" + msg + "\".");
			player.sendMessage("Quel est votre message ?");
			this.steps[0] = true;
			
		} else {
			player.sendMessage("L'objet précisé est trop long. Longeur max: 100");
			player.sendMessage("Quel est l'objet de la lettre ?");
			
		}
	}
	
	private void stepContent(Player player, String msg) {
		if(msg.length() <= 300 ) {
			letterFactory.setContent(Arrays.asList(new String[] {msg}));
			player.sendMessage("Contenue de la lettre:\n - \"" + msg + "\".");
			
			this.stepRecipients(player);

			this.steps[1] = true;
			
		} else {
			player.sendMessage("Contenue précisé trop long. Longeur max: 300");
			player.sendMessage("Quel est votre message ?");
			
		}
		
	}
	
	private void stepRecipients(Player player) {
		if(this.getPlayerSelectorInventory() == null) {
			this.setPlayerSelectorInventory(new PlayerSelectorInventory(this.getRecipients(), "§lA qui envoyer la lettre ?"));
		}
		this.getPlayerSelectorInventory().openInventory(player);
		
		this.steps[2] = true;
	}
	
	private void stepRecap(Player player) {
		if(this.getRecipients().size() > 1) {
			letterFactory.setLetterType(LetterType.ANNOUNCE);
			
		} else {
			letterFactory.setLetterType(LetterType.STANDARD);
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		sb.append(String.format("§e§lAutheur:§r %s\n", letterFactory.getAuthor()));
		sb.append(String.format("§e§lObjet:§r %s\n", letterFactory.getObject()));
		sb.append(String.format("§e§lMessage:§r\n - %s\n", letterFactory.getContent().toString().replace("[", "").replace("]", "") ) );
		sb.append("\n");
		
		sb.append("§6§oEcrivez \"#send\" pour envoyer la lettre ou \"#stop\" pour tout annuler.");
		player.sendMessage(sb.toString());
		
	}
	
	private void stepSend(Player player) {
		LetterData toSend = letterFactory.clone();
		player.sendMessage("Vous avez envoyer une lettre");
		
		for(String name : this.getRecipients() ) {
			toSend.setUuid(PlayerManager.getInstance().getUUID(name));
			MailBoxController.getInstance().sendLetter(toSend);
		}
		
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	private void onLetterCreation(AsyncPlayerChatEvent event) {
		Player ePlayer = event.getPlayer();
		Player tPlayer = Bukkit.getPlayer(this.getUuid());
		
		if (tPlayer != null && ePlayer.equals(tPlayer) ) {
			event.setCancelled(true);
			String msg = event.getMessage();

			if (msg.equalsIgnoreCase("#stop") ) {
				ePlayer.sendMessage("Arret du mode de création de lettre.");
				this.stop();
				return;
			}
			
			if(letterFactory == null) {
				Data data = new DataFactory(null, ePlayer.getName(), null);
				letterFactory = new LetterDataFactory(data, LetterType.STANDARD, new ArrayList<>(), false);
				
			}
			
			if (!this.steps[0]) {
				this.stepObject(ePlayer, msg);
				
			} else if(!this.steps[1]) {
				this.stepContent(ePlayer, msg);
				
			} else if(!this.steps[2]) {
				this.stepRecipients(ePlayer);
				
			} else if(msg.equals("#send") ) {
				this.stepSend(ePlayer);
				this.stop();
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	private void onInventoryClose(InventoryCloseEvent event) {
		HumanEntity he = event.getPlayer();

		if(he instanceof Player) {
			Player player = (Player) he;
			
			if(this.getUuid().equals(player.getUniqueId()) ) {
				InventoryView view = event.getView();
				
				if(view.getType() == InventoryType.CHEST && view.getTitle().equals("§lA qui envoyer la lettre ?") && this.getPlayerSelectorInventory().getFinalClose() ) {
					if(!this.getRecipients().isEmpty() ) {
						this.stepRecap(player);
						
					} else {
						player.sendMessage("§cVous devez choisir au moins un destinaire.");
						this.getPlayerSelectorInventory().openInventory(player);
						
					}
					
				}
			}
		}
	}
	
	public List<String> getRecipients() {
		return recipients;
	}

	public void setRecipients(List<String> recipients) {
		this.recipients = recipients;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public PlayerSelectorInventory getPlayerSelectorInventory() {
		return playerSelectorInventory;
	}

	public void setPlayerSelectorInventory(PlayerSelectorInventory playerSelectorInventory) {
		this.playerSelectorInventory = playerSelectorInventory;
	}
}
