package fr.dornacraft.mailbox.inventory.providers;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.dornacraft.devtoolslib.smartinvs.ClickableItem;
import fr.dornacraft.devtoolslib.smartinvs.content.InventoryContents;
import fr.dornacraft.mailbox.ItemStackBuilder;
import fr.dornacraft.mailbox.DataManager.DataHolder;
import fr.dornacraft.mailbox.inventory.builders.InventoryBuilder;

public class MailBoxInventory extends InventoryBuilder {
	public static Material LETTER_MENU_MATERIAL = Material.LECTERN;
	public static Material ITEM_MENU_MATERIAL = Material.CHEST;
	public static Material SEND_LETTER_MATERIAL = Material.HOPPER;
	
	private DataHolder dataSource;
	
	public MailBoxInventory(DataHolder dataSource) {
		super("MailBox_Principal", "§lMenu Principal", 3);
		this.setDataSource(dataSource);
	}

	@Override
	public void initializeInventory(Player player, InventoryContents contents) {
		
		
		contents.set(1, 2, ClickableItem.of(new ItemStackBuilder(LETTER_MENU_MATERIAL).setName("§f§lMenu des lettres reçus").build(), e -> {
			LetterInventory inv = new LetterInventory(this.getDataSource(), this);
			inv.openInventory(player);
		}));
		
		contents.set(1, 6, ClickableItem.of(new ItemStackBuilder(ITEM_MENU_MATERIAL).setName("§f§lMenu des objets reçus").build(), e ->  {
			ItemInventory inv = new ItemInventory(this.getDataSource(), this);
			inv.openInventory(player);
		}));
		
		contents.set(2, 4, ClickableItem.empty(new ItemStackBuilder(SEND_LETTER_MATERIAL).setName("Envoyer une lettre").build()  ) );//TODO
	}

	@Override
	public void updateInventory(Player player, InventoryContents contents) {
		
	}

	private DataHolder getDataSource() {
		return dataSource;
	}

	private void setDataSource(DataHolder dataSource) {
		this.dataSource = dataSource;
	}
	
}