package fr.dornacraft.mailbox.command;

import java.util.Iterator;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.dornacraft.mailbox.DataHolder;
import fr.dornacraft.mailbox.ItemData;
import fr.dornacraft.mailbox.MailBoxController;
import fr.dornacraft.mailbox.sql.ItemDataSQL;

public class Cmd_mailbox implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		Player player = (Player) sender;

		if (args.length >= 1) {
			if (args[0].equalsIgnoreCase("item") && args.length == 2) {
				if (args[1].equalsIgnoreCase("send")) {
					MailBoxController.getInstance().sendItem(player, player.getInventory().getItemInMainHand());

				} else if (args[1].equalsIgnoreCase("getall")) {
					DataHolder holder = MailBoxController.getInstance().getDataManager().getDataHolder(player.getUniqueId());
					List<ItemData> list = MailBoxController.getInstance().getDataManager().getTypeData(holder, ItemData.class);
					Iterator<ItemData> it = list.iterator();
					
					while(it.hasNext() ) {
						ItemData id = it.next();
						if(ItemDataSQL.compareToNow(id.getCreationDate(), id.getDuration()) < 0) {
							ItemDataSQL.getInstance().delete(id);
							holder.removeData(id.getId());
							player.sendMessage("un objet a ete supprimer car il été périmé.");
							
						} else {
							player.getInventory().addItem(id.getItem());
							player.sendMessage("vous avez recuperer " + list.size() + " de la database.");
						}
					}

				}
			} else if (args.length == 1 && args[0].equalsIgnoreCase("letter")) {
				MailBoxController.getInstance().sendLetter(player, player.getInventory().getItemInMainHand());
			}
		}
		return false;

	}
	
}