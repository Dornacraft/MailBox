package fr.dornacraft.mailbox.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import fr.dornacraft.mailbox.DataManager.MailBoxController;

public class JoinListener implements Listener {

	@EventHandler
	private void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		MailBoxController.getInstance().getDataManager().getCache().put(player.getUniqueId(), MailBoxController.getInstance().getHolderFromDataBase(player));

	}

}
