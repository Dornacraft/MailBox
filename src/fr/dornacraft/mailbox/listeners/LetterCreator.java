package fr.dornacraft.mailbox.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
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
import fr.dornacraft.mailbox.inventory.providers.PlayerSelectorInventory;
import fr.dornacraft.mailbox.inventory.providers.utils.AuthorFilter;
import fr.dornacraft.mailbox.playerManager.PlayerInfo;

public class LetterCreator implements Listener {
	
	private static Map<UUID, LetterCreator> map = new HashMap<>();
	
	private UUID uuid;
	private AuthorFilter recipients = new AuthorFilter();
	private String object;
	private List<String> content;
	
	/* * * * * * * * * * * * * * * * *
	 * * * * constructor(s) * * * * *
	 * * * * * * * * * * * * * * * */
	public LetterCreator(Player player) {
		this.setUuid(player.getUniqueId());
		
	}
	
	
	/* * * * * * * * * * * * * * * * *
	 * * * * * manipulation * * * * *
	 * * * * * * * * * * * * * * * */
	public void startCreation(Player player) {
		this.getMap().put(this.getUuid(), this);
		player.sendMessage("Vous pouvez annuler a tout moment en envoyant #stop");
		Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
		this.next(player);
		
	}
	
	public void stopCreation() {
		this.getMap().remove(this.getUuid());
		AsyncPlayerChatEvent.getHandlerList().unregister(this);
	}
	
	private void next(Player player) {
		Bukkit.getScheduler().runTask(Main.getInstance(), e -> {
			player.chat("###");
		});
	}
	
	private void sendRecap(Player player, Boolean shownextStep) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("§l§n§eRécapitulation:§r\n");
		sb.append("§8Objet:§r " + this.getObject() + "\n");
		sb.append("§8Destinataires:§r " + this.getRecipients().getPreview() + "\n");
		sb.append("§8Message:§r\n" + StringUtils.join(this.getContent(), " ") + "\n");
		
		if(shownextStep ) {
			sb.append("§o§6Pour envoyer votre mail écrivez \"send\".");
		}
		
		player.sendMessage(sb.toString());
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	private void onLetterCreation(AsyncPlayerChatEvent event) {
		event.setCancelled(true);
		Player ePlayer = event.getPlayer();
		String eMessage = event.getMessage();
		
		if(this.isCreatingLetter(ePlayer) ) {
			if(eMessage.contentEquals("#stop")) {
				ePlayer.sendMessage("Vous avez quitté le mode de creation de lettre.");
				this.stopCreation();
				return;
			}
			
			if(eMessage.equals("#recap")) {
				this.sendRecap(ePlayer, false);
				return;
				
			}
			
			if(!eMessage.contains(" ") && eMessage.startsWith("clear#")) {
				eMessage = eMessage.toLowerCase();
				if (eMessage.contains("#1") || eMessage.contains("#objet") || eMessage.contains("#o")) {
					this.reObject(ePlayer);
					return;
	
				} else if (eMessage.contains("#2") || eMessage.contains("#message") || eMessage.contains("#m")) {
					this.reContent(ePlayer);
					return;
	
				} else if (eMessage.contains("#3") || eMessage.contains("#destinataire") || eMessage.contains("#d")) {
					this.reRecipients(ePlayer);
					return;
				}
			}
			
			if(this.getObject() == null) {
				ePlayer.sendMessage("Quel est l'objet de la lettre ?");
				this.setObject("");
				
			} else if(this.getObject().isEmpty() ) {
				if(!eMessage.equals("###")) {
					this.setObject(eMessage);
					ePlayer.sendMessage("l'objet de la lettre seras: " + this.getObject() + "\"." );
					this.next(ePlayer);
				}
				
			} else if (this.getContent() == null ) {
				ePlayer.sendMessage("Quel est votre message ?");
				this.setContent(new ArrayList<>());
				
			} else if (this.getContent().isEmpty() ) {
				if(!eMessage.equals("###")) {
					this.setContent(Arrays.asList(new String[] {eMessage}));
					ePlayer.sendMessage("Le message seras: \"" + this.getContent() + "\"." );
					this.next(ePlayer);
				}
				
			} else if (this.getRecipients().getList().isEmpty() ) {
				PlayerSelectorInventory pci = new PlayerSelectorInventory(this.getRecipients(), "§lChoisissez votre/vos cible");
				pci.setDefinitiveQuit(e -> {
					this.next(ePlayer);
				});
				pci.openInventory(ePlayer);
				
			} else if(eMessage.equals("send") ){	
				Data data = new DataFactory(null, ePlayer.getName(), this.getObject());
				LetterData letterData = new LetterDataFactory(data, LetterType.STANDARD, this.getContent(), false);//FIXME
				
				for(PlayerInfo pi : this.getRecipients().getList() ) {//FIXME si l'envoie echoue, ne pas faire le reste
					LetterData toSend = letterData.clone();
					toSend.setUuid(pi.getUuid() );
					MailBoxController.getInstance().sendLetter(toSend);
				}
				
				ePlayer.sendMessage("Vous avez envoyer une lettre à: " + this.getRecipients().getPreview() );
				this.stopCreation();
				
			} else {
				this.sendRecap(ePlayer, true);
			}
		}
	}
	
	private void reObject(Player player) {
		if(this.getContent() != null && this.getContent().isEmpty() ) {
			this.setContent(null);
		}
		this.setObject(null);
		player.sendMessage("Objet supprimé.");
		this.next(player);
		
	}
	
	private void reContent(Player player) {
		if(this.getObject() != null && this.getObject().isEmpty() ) {
			this.setObject(null);
		}
		
		this.setContent(null);
		
		player.sendMessage("Message supprimé.");
		this.next(player);
	}
	
	private void reRecipients(Player player) {
		if(this.getObject() != null && this.getObject().isEmpty() ) {
			this.setObject(null);
		}
		if(this.getContent() != null && this.getContent().isEmpty() ) {
			this.setContent(null);
		}
		
		this.setRecipients(new AuthorFilter());
		
		player.sendMessage("Destinaire(s) supprimé(s).");
		this.next(player);
	}
	
	public Boolean isCreatingLetter(Player player) {
		return this.getMap().containsKey(player.getUniqueId());
	}
	
	/* * * * * * * * * * * * * * * * 
	 * * * setters * getters * * * *
	 * * * * * * * * * * * * * * * */
	public AuthorFilter getRecipients() {
		return recipients;
	}

	public void setRecipients(AuthorFilter recipients) {
		this.recipients = recipients;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	private Map<UUID, LetterCreator> getMap() {
		return map;
	}


	public String getObject() {
		return object;
	}


	public void setObject(String object) {
		this.object = object;
	}


	public List<String> getContent() {
		return content;
	}


	public void setContent(List<String> content) {
		this.content = content;
	}

}
