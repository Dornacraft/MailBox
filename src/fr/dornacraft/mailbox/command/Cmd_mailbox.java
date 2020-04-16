package fr.dornacraft.mailbox.command;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Iterator;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.dornacraft.mailbox.DataManager.DataHolder;
import fr.dornacraft.mailbox.DataManager.DataManager;
import fr.dornacraft.mailbox.DataManager.ItemData;
import fr.dornacraft.mailbox.DataManager.LetterData;
import fr.dornacraft.mailbox.DataManager.MailBoxController;
import fr.dornacraft.mailbox.inventory.providers.MailBoxInventory;
import fr.dornacraft.mailbox.playerManager.PlayerInfo;
import fr.dornacraft.mailbox.playerManager.PlayerManager;
import fr.dornacraft.mailbox.sql.ItemDataSQL;

public class Cmd_mailbox implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		Player player = (Player) sender;

		if (args.length > 0) {
			DataManager manager = MailBoxController.getInstance().getDataManager();
			DataHolder holder = manager.getDataHolder(player.getUniqueId());
			
			if (args.length == 4) {
				if(args[0].equalsIgnoreCase("item")) {
					if(args[1].equalsIgnoreCase("send")) {
						PlayerInfo rPlayerInfo = PlayerManager.getInstance().getPlayerInfo(args[2]);
						
						try {
							String prefix = args[3].contains("D") ? "P" : "PT";
							String subD = args[3].replace("D", "DT");
							String strD = prefix + subD;
							System.out.println(strD);
							Duration d = Duration.parse(strD);
							MailBoxController.getInstance().sendItem(rPlayerInfo.getName(), player.getInventory().getItemInMainHand(), d );
							player.sendMessage("Vous avez envoyé un objet a " + rPlayerInfo.getName() );
							
						} catch(DateTimeParseException e) {
							player.sendMessage("wrong duration");
						}								
					}
				}
				
			} else if(args.length == 3) {
				PlayerInfo rPlayerInfo = PlayerManager.getInstance().getPlayerInfo(args[2]);
				
				if(rPlayerInfo != null) {
					
					if(args.length == 3) {
						if(args[0].equalsIgnoreCase("item")) {
							if(args[1].equalsIgnoreCase("send")) {
								MailBoxController.getInstance().sendItem(rPlayerInfo.getName(), player.getInventory().getItemInMainHand(), Duration.ofSeconds(20) );
								player.sendMessage("Vous avez envoyé un objet a " + rPlayerInfo.getName() );
								
							}
						} else if (args[0].equalsIgnoreCase("letter")) {
							if(args[1].equalsIgnoreCase("send")) {
								MailBoxController.getInstance().sendLetter(rPlayerInfo.getUuid(), player.getInventory().getItemInMainHand() );
								player.sendMessage("Vous avez envoyé une lettre a " + rPlayerInfo.getName() );
								
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
				PlayerInfo source = PlayerManager.getInstance().getPlayerInfo(args[0]);
				
				if(source != null) {
					MailBoxInventory mailBox = new MailBoxInventory(MailBoxController.getInstance().getDataHolder(source.getUuid()) );
					mailBox.openInventory(player);
					
				} else {
					player.sendMessage("Joueur " + args[0] + " inconnu");
				}
			}
		} else {
			MailBoxInventory mailBox = new MailBoxInventory(MailBoxController.getInstance().getDataHolder(player.getUniqueId()));
			mailBox.openInventory(player);
			
		}
		return false;

	}
	
}