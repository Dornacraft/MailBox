package fr.dornacraft.mailbox;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

	private static Main main;

	public static Main getInstance() {
		return main;
	}
	
	@Override
	public void onEnable() {
		Main.main = this;
		this.saveDefaultConfig();
		
	}
	
	@Override
	public void onDisable() {
		
	}

}