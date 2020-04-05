package fr.dornacraft.mailbox.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.dornacraft.mailbox.MailBoxController;

public class QuitListener implements Listener {
	
	@EventHandler
	private void onQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		MailBoxController.getInstance().getDataManager().remove(player.getUniqueId());
	}
	
}
