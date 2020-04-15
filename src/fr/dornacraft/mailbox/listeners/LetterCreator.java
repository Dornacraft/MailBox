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
import fr.dornacraft.mailbox.DataManager.Data;
import fr.dornacraft.mailbox.DataManager.LetterData;
import fr.dornacraft.mailbox.DataManager.LetterType;
import fr.dornacraft.mailbox.DataManager.MailBoxController;
import fr.dornacraft.mailbox.DataManager.factories.DataFactory;
import fr.dornacraft.mailbox.DataManager.factories.LetterDataFactory;
import fr.dornacraft.mailbox.playerManager.PlayerManager;

public class LetterCreator implements Listener {
	
	private Player player;
	private LetterDataFactory letterFactory;
	private List<String> recipients = new ArrayList<>();
	
	public LetterCreator(Player player) {
		this.setPlayer(player);
	}
	
	public void start() {
		this.getPlayer().sendMessage("Quel est l'objet de la lettre ?");
		Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
		
	}
	
	public void stop(){
		AsyncPlayerChatEvent.getHandlerList().unregister(this);
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	private void onLetterCreation(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();

		if (this.getPlayer() != null) {
			if (this.getPlayer().equals(player)) {
				event.setCancelled(true);
				String msg = event.getMessage();

				if (msg.equalsIgnoreCase("#stop") ) {
					this.stop();
					return;
				}
				
				if(letterFactory == null) {
					Data data = new DataFactory(null, player.getName(), null);
					letterFactory = new LetterDataFactory(data, LetterType.STANDARD, new ArrayList<>(), false);
					
				}
				
				if (letterFactory.getObject() == null) {
					if(msg.length() <= 100) {
						letterFactory.setObject(msg);
						player.sendMessage("Objet de la lettre: \"" + msg + "\".");
						player.sendMessage("Quel est le contenue de la lettre ?");
						
					} else {
						player.sendMessage("L'objet précisé est trop long.");
					}
					
				} else if(letterFactory.getContent() == null) {
					if(msg.length() <= 300 ) {
						letterFactory.setContent(Arrays.asList(new String[] {msg}));
						player.sendMessage("Contenue de la lettre: \n - \"" + msg + "\".");
						//TODO  check for permissions
						player.sendMessage("A qui voulez vous envoyer la lettre ? - #next");
						
						
					} else {
						player.sendMessage("Contenue précisé trop long.");
						
					}
					//TODO -> attributions des destinataires
				} else if(this.getRecipients().isEmpty() ) {
					if(msg.equals("all")) {
						
					} else if (msg.equals("online")) {
						
					} else {
						PlayerChatSelector pcs = new PlayerChatSelector(player, this.getRecipients());
						pcs.start();
					}
					
				} else if(msg.equals("send") ) {//Creation terminé
					if(this.getRecipients().size() > 1) {
						letterFactory.setLetterType(LetterType.ANNOUNCE);
						
					} else {
						letterFactory.setLetterType(LetterType.STANDARD);
					}
					
					for(String name : this.getRecipients() ) {
						LetterData toSend = letterFactory.clone();
						toSend.setUuid(PlayerManager.getInstance().getUUID(name));
						MailBoxController.getInstance().sendLetter(toSend);
					}
					
					this.stop();
					
					player.sendMessage("Vous avez envoyer une lettre");
					
				} else {
					player.sendMessage("Ecrivez send pour envoyer la lettre ou stop pour tout annuler");
				}
			}

		} else {
			this.stop();
			
		}
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public List<String> getRecipients() {
		return recipients;
	}

	public void setRecipients(List<String> recipients) {
		this.recipients = recipients;
	}
}
