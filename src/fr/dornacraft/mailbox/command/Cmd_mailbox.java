package fr.dornacraft.mailbox.command;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;

import fr.dornacraft.devtoolslib.acf.BaseCommand;
import fr.dornacraft.devtoolslib.acf.annotation.CommandAlias;
import fr.dornacraft.devtoolslib.acf.annotation.CommandPermission;
import fr.dornacraft.devtoolslib.acf.annotation.Default;
import fr.dornacraft.devtoolslib.acf.annotation.Subcommand;
import fr.dornacraft.devtoolslib.acf.annotation.Syntax;
import fr.dornacraft.mailbox.DataManager.DataHolder;
import fr.dornacraft.mailbox.DataManager.LetterData;
import fr.dornacraft.mailbox.DataManager.MailBoxController;
import fr.dornacraft.mailbox.inventory.providers.LetterInventory;
import fr.dornacraft.mailbox.inventory.providers.MailBoxInventory;
import fr.dornacraft.mailbox.playerManager.PlayerInfo;
import fr.dornacraft.mailbox.playerManager.PlayerManager;

@CommandAlias("mailbox|mb")
public class Cmd_mailbox extends BaseCommand {
	
	@Default
	@CommandPermission("mailbox.openmenu.remote.self")
	private void onOpenSelf(Player player) {
		MailBoxInventory mailBox = new MailBoxInventory(MailBoxController.getInstance().getDataHolder(player.getUniqueId()) );
		mailBox.openInventory(player);
	}
	
	@Default
	@CommandPermission("mailbox.openmenu.remote.other")
	private void onOpenOther(Player player, String name) {
		UUID pUuid = PlayerManager.getInstance().getUUID(name);
		
		if(pUuid != null) {
			PlayerInfo pi = new PlayerInfo(name, pUuid);
			
			MailBoxInventory mailBox = new MailBoxInventory(MailBoxController.getInstance().getDataHolder(pi.getUuid()) );
			mailBox.openInventory(player);
			
		} else {
			player.sendMessage("Joueur " + name + " inconnu");
		}
	}
	
	@Subcommand("check")
	@CommandPermission("mailbox.check")
	private void onCheckNotifications(Player player) {
			DataHolder pHolder = MailBoxController.getInstance().getDataHolder(player.getUniqueId());
			List<LetterData> letterList = MailBoxController.getInstance().getDataManager().getTypeData(pHolder, LetterData.class);
			Integer nonRead = LetterInventory.filterByReadState(letterList, false).size();
			player.sendMessage("Vous avez " + nonRead + " lettres non lues.");
		
	}
	
	@Subcommand("item send")
	@Syntax("/mailbox item send <joueur> <durée>")
	@CommandPermission("mailbox.send.item")
	private void onItemSend(Player player, String pName, String duration) {
		UUID pUuid = PlayerManager.getInstance().getUUID(pName);
		
		if(pUuid != null) {
			PlayerInfo pi = new PlayerInfo(pName, pUuid);
		
			try {
				String prefix = duration.replace(" ", "").contains("D") ? "P" : "PT";
				String subD = duration.replace("D", "DT");
				String strD = prefix + subD;
				Duration d = Duration.parse(strD);
				MailBoxController.getInstance().sendItem(pi.getName(), player.getInventory().getItemInMainHand(), d );
				player.sendMessage("Vous avez envoyé un objet a " + pi.getName() );
				
			} catch(DateTimeParseException e) {
				player.sendMessage("Durée \""+ duration + "\" impossible.");
			}
			
		} else {
			player.sendMessage("Cible inconnu");
		}
		
	}
	
	/*
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		Player player = (Player) sender;

		if (args.length > 0) {
			DataManager manager = MailBoxController.getInstance().getDataManager();
			DataHolder holder = manager.getDataHolder(player.getUniqueId());
			
			if (args.length == 4) {
				if(args[0].equalsIgnoreCase("item")) {
					if(args[1].equalsIgnoreCase("send")) {
						UUID pUuid = PlayerManager.getInstance().getUUID(args[2]);
						
						if(pUuid != null) {
							PlayerInfo pi = new PlayerInfo(args[2], pUuid);
						
							try {
								String prefix = args[3].contains("D") ? "P" : "PT";
								String subD = args[3].replace("D", "DT");
								String strD = prefix + subD;
								System.out.println(strD);
								Duration d = Duration.parse(strD);
								MailBoxController.getInstance().sendItem(pi.getName(), player.getInventory().getItemInMainHand(), d );
								player.sendMessage("Vous avez envoyé un objet a " + pi.getName() );
								
							} catch(DateTimeParseException e) {
								player.sendMessage("wrong duration");
							}								
						} else {
							player.sendMessage("Cible inconnu");
						}
					}
				}
				
			} else if(args.length == 3) {
				UUID pUuid = PlayerManager.getInstance().getUUID(args[2]);
				
				if(pUuid != null) {
					PlayerInfo pi = new PlayerInfo(args[2], pUuid);
					
					if(args.length == 3) {
						if(args[0].equalsIgnoreCase("item")) {
							if(args[1].equalsIgnoreCase("send")) {
								MailBoxController.getInstance().sendItem(pi.getName(), player.getInventory().getItemInMainHand(), Duration.ofSeconds(20) );
								player.sendMessage("Vous avez envoyé un objet a " + pi.getName() );
								
							}
						} else if (args[0].equalsIgnoreCase("letter")) {
							if(args[1].equalsIgnoreCase("send")) {
								MailBoxController.getInstance().sendLetter(pi.getUuid(), player.getInventory().getItemInMainHand() );
								player.sendMessage("Vous avez envoyé une lettre a " + pi.getName() );
								
							}
							
						}
					}
					
				} else {
					player.sendMessage("Joueur inconnu");
				}
			} else if(args.length == 2) {
				if (args[0].equalsIgnoreCase("item")) {
					if (args[1].equalsIgnoreCase("send")) {
						MailBoxController.getInstance().sendItem(player.getName(), player.getInventory().getItemInMainHand(), Duration.ofSeconds(20) );
	
					} else if (args[1].equalsIgnoreCase("getall")) {
						
						List<ItemData> list = manager.getTypeData(holder, ItemData.class);
						Iterator<ItemData> it = list.iterator();
						
						while(it.hasNext() ) {
							ItemData id = it.next();
							if(MailBoxController.getInstance().isOutOfDate(id)) {
								ItemDataSQL.getInstance().delete(id);
								MailBoxController.getInstance().getDataManager().getDataHolder(player.getUniqueId()).removeData(id.getId());
								player.sendMessage("un objet a été supprimer car il été périmé.");
								
							} else {
								player.getInventory().addItem(id.getItem());
								player.sendMessage("vous avez récupéré " + manager.getTypeData(holder, LetterData.class)+ " de la database.");
							}
						}
					}
				} else if (args[0].equalsIgnoreCase("letter")) {
					if(args.length == 2) {
						if(args[1].equalsIgnoreCase("send")) {
							MailBoxController.getInstance().sendLetter(player.getUniqueId(), player.getInventory().getItemInMainHand() );
							
						} else if (args[1].equalsIgnoreCase("size")) {
							String msg = "Vous avez " + manager.getTypeData(holder, LetterData.class).size() + " lettres dans votre boite";
							player.sendMessage(msg);
							
						}
						
	
						
					}
				}
			} else if (args.length == 1) {
				UUID pUuid = PlayerManager.getInstance().getUUID(args[0]);
				
				if(pUuid != null) {
					PlayerInfo pi = new PlayerInfo(args[0], pUuid);
					
					MailBoxInventory mailBox = new MailBoxInventory(MailBoxController.getInstance().getDataHolder(pi.getUuid()) );
					mailBox.openInventory(player);
					
				} else {
					player.sendMessage("Joueur " + args[0] + " inconnu");
				}
			}
		} else {
			MailBoxInventory mailBox = new MailBoxInventory(MailBoxController.getInstance().getDataHolder(player.getUniqueId()) );
			mailBox.openInventory(player);
			
		}
		
		return false;
	}
	
	*/
}