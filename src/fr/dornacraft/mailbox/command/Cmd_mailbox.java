package fr.dornacraft.mailbox.command;

import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
import fr.dornacraft.mailbox.sql.ItemDataSQL;

public class Cmd_mailbox implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		Player player = (Player) sender;

		if (args.length > 0) {
			DataManager manager = MailBoxController.getInstance().getDataManager();
			DataHolder holder = manager.getDataHolder(player.getUniqueId());
			
			
			if(args.length == 3) {
				OfflinePlayer recipient = getOfflinePlayer(args[2]);
				
				if(recipient != null) {
					if(args[0].equalsIgnoreCase("item")) {
						if(args[1].equalsIgnoreCase("send")) {
							MailBoxController.getInstance().sendItem(recipient, player.getInventory().getItemInMainHand() );
							player.sendMessage("Vous avez envoyé un objet a " + recipient.getName() );
							
						}
					} else if (args[0].equalsIgnoreCase("letter")) {
						if(args[1].equalsIgnoreCase("send")) {
							MailBoxController.getInstance().sendLetter(recipient, player.getInventory().getItemInMainHand() );
							player.sendMessage("Vous avez envoyé une lettre a " + recipient.getName() );
							
						}
						
					}
				} else {
					player.sendMessage("Joueur inconnu");
				}
			} else if(args.length == 2) {
				if (args[0].equalsIgnoreCase("item")) {
					if (args[1].equalsIgnoreCase("send")) {
						MailBoxController.getInstance().sendItem(player, player.getInventory().getItemInMainHand());
	
					} else if (args[1].equalsIgnoreCase("getall")) {
						
						List<ItemData> list = manager.getTypeData(holder, ItemData.class);
						Iterator<ItemData> it = list.iterator();
						
						while(it.hasNext() ) {
							ItemData id = it.next();
							if(MailBoxController.getInstance().isOutOfDate(id)) {
								ItemDataSQL.getInstance().delete(id);
								MailBoxController.getInstance().getDataManager().getDataHolder(player.getUniqueId()).removeData(id.getId());
								player.sendMessage("un objet a ete supprimer car il été périmé.");
								
							} else {
								player.getInventory().addItem(id.getItem());
								player.sendMessage("vous avez recuperer " + manager.getTypeData(holder, LetterData.class)+ " de la database.");
							}
						}
	
					}
				} else if (args[0].equalsIgnoreCase("letter")) {
					if(args.length == 2) {
						if(args[1].equalsIgnoreCase("send")) {
							MailBoxController.getInstance().sendLetter(player, player.getInventory().getItemInMainHand() );
							
						} else if (args[1].equalsIgnoreCase("size")) {
							String msg = "Vous avez " + manager.getTypeData(holder, LetterData.class).size() + " lettres dans votre boite";
							player.sendMessage(msg);
							
						}
						
	
						
					}
	
				}
			} else if (args.length == 1) {
				OfflinePlayer source = getOfflinePlayer(args[0]);
				
				if(source != null) {
					MailBoxInventory mailBox = new MailBoxInventory(MailBoxController.getInstance().getDataHolder(source.getUniqueId()) );
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
	
	private OfflinePlayer getOfflinePlayer(String name) {
		OfflinePlayer res = null;
		
		for(OfflinePlayer offplayer : Bukkit.getOfflinePlayers() ) {
			if(offplayer.getName().equals(name)) {
				res = offplayer;
			}
		}
		
		return res;
	}
	
}