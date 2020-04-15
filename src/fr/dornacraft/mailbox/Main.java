package fr.dornacraft.mailbox;

import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

import fr.dornacraft.devtoolslib.smartinvs.InventoryManager;
import fr.dornacraft.devtoolslib.smartinvs.SmartInventory;
import fr.dornacraft.devtoolslib.smartinvs.SmartInventory.Builder;
import fr.dornacraft.mailbox.DataManager.MailBoxController;
import fr.dornacraft.mailbox.command.Cmd_mailbox;
import fr.dornacraft.mailbox.listeners.JoinListener;
import fr.dornacraft.mailbox.listeners.QuitListener;
import fr.dornacraft.mailbox.playerManager.PlayerManager;
import fr.dornacraft.mailbox.sql.SQLConnection;

public class Main extends JavaPlugin {
	/*
	 * TODO LIST
	 * objet d'un lettre -> max 100 caracteres
	 * contenue d'une lettre -> max 300 caracteres
	 * 
	 * 
	 */
	private static Builder builder;
	public static Builder getBuilder() {
		return builder;
	}
	
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
		
		if(SQLConnection.getInstance().getConnection() != null && SQLConnection.getInstance().isConnected() ) {
		
			InventoryManager manager = new InventoryManager(this);
			manager.init();
			builder = SmartInventory.builder().manager(manager);
			PlayerManager.getInstance().init();
	
			
			this.getCommand("mailbox").setExecutor(new Cmd_mailbox());
			this.registerListeners();
			
			MailBoxController.getInstance().initialize();
			
		} else {
			this.getLogger().log(Level.SEVERE, "Le plugin a besoin d'un connexion une base de donnée pour fonctionner");
		}

	}

	@Override
	public void onDisable() {

	}

	private void registerListeners() {
		this.getServer().getPluginManager().registerEvents(new JoinListener(), this);
		this.getServer().getPluginManager().registerEvents(new QuitListener(), this);
	}

}