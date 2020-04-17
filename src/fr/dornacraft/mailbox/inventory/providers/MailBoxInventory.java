package fr.dornacraft.mailbox.inventory.providers;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import fr.dornacraft.devtoolslib.smartinvs.ClickableItem;
import fr.dornacraft.devtoolslib.smartinvs.content.InventoryContents;
import fr.dornacraft.mailbox.ItemStackBuilder;
import fr.dornacraft.mailbox.DataManager.DataHolder;
import fr.dornacraft.mailbox.inventory.builders.InventoryProviderBuilder;
import fr.dornacraft.mailbox.listeners.LetterCreator;

public class MailBoxInventory extends InventoryProviderBuilder {
	public static Material LETTER_MENU_MATERIAL = Material.LECTERN;
	public static Material ITEM_MENU_MATERIAL = Material.CHEST;
	public static Material SEND_LETTER_MATERIAL = Material.HOPPER;
	
	private DataHolder dataSource;
	
	public MailBoxInventory(DataHolder dataSource) {
		super("MailBox_Principal", "§lMenu principal", 3);
		this.setDataSource(dataSource);
	}

	@Override
	public void initializeInventory(Player player, InventoryContents contents) {
		
		
		contents.set(1, 2, ClickableItem.of(new ItemStackBuilder(LETTER_MENU_MATERIAL).setName("§f§lMenu des lettres reçues").build(), e -> {
			LetterInventory inv = new LetterInventory(this.getDataSource(), this);
			inv.openInventory(player);
		}));
		
		contents.set(1, 6, ClickableItem.of(new ItemStackBuilder(ITEM_MENU_MATERIAL).setName("§f§lMenu des objets reçues").build(), e ->  {
			ItemInventory inv = new ItemInventory(this.getDataSource(), this);
			inv.openInventory(player);
		}));
		
		contents.set(2, 4, ClickableItem.of(new ItemStackBuilder(SEND_LETTER_MATERIAL).setName("§f§lEnvoyer une lettre").build(), e ->  {
			ClickType click = e.getClick();
			ItemStack cursor = e.getCursor();
			
			if (click == ClickType.LEFT ) {
				if(cursor.getType() == Material.WRITTEN_BOOK && cursor.hasItemMeta() ) { // avancé
					BookMeta meta = (BookMeta) cursor.getItemMeta();
					
					if(meta.getPages() != null && !meta.getPages().isEmpty() ) {
						player.closeInventory();
						LetterCreator creator = new LetterCreator(player);
						creator.setContent(meta.getPages() );
						creator.startCreation(player);
						
					} else {
						
						
					}
				} else {//simple
					player.closeInventory();
					LetterCreator creator = new LetterCreator(player);
					creator.startCreation(player);
				}
				
				
			}
		}));
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