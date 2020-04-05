package fr.dornacraft.mailbox;

import org.bukkit.plugin.java.JavaPlugin;

import fr.dornacraft.mailbox.DataManager.MailBoxController;
import fr.dornacraft.mailbox.command.Cmd_mailbox;
import fr.dornacraft.mailbox.listeners.JoinListener;
import fr.dornacraft.mailbox.listeners.QuitListener;
import fr.dornacraft.mailbox.sql.SQLConnection;

public class Main extends JavaPlugin {

	private static Main main;

	public static Main getInstance() {
		return main;
	}

	@Override
	public void onEnable() {
		Main.main = this;
		this.saveDefaultConfig();
		SQLConnection.getInstance().connect(SQLConnection.SGBD_TYPE_ROOT, this.getConfig().getString("database.host"),
				this.getConfig().getString("database.database"), this.getConfig().getString("database.user"),
				this.getConfig().getString("database.password"));
		this.getCommand("mailbox").setExecutor(new Cmd_mailbox());
		this.registerListeners();
		
		MailBoxController.getInstance().initialize();

	}

	@Override
	public void onDisable() {

	}

	private void registerListeners() {
		this.getServer().getPluginManager().registerEvents(new JoinListener(), this);
		this.getServer().getPluginManager().registerEvents(new QuitListener(), this);
	}

}