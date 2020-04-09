package fr.dornacraft.mailbox.inventory.providers;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.dornacraft.devtoolslib.smartinvs.ClickableItem;
import fr.dornacraft.devtoolslib.smartinvs.SmartInventory;
import fr.dornacraft.devtoolslib.smartinvs.SmartInventory.Builder;
import fr.dornacraft.devtoolslib.smartinvs.content.InventoryContents;
import fr.dornacraft.devtoolslib.smartinvs.content.InventoryProvider;
import fr.dornacraft.mailbox.ItemStackBuilder;
import fr.dornacraft.mailbox.Main;

public class MailBoxProvider implements InventoryProvider {
	
	private UUID dataSource;
	
	public MailBoxProvider(UUID dataSource) {
		this.setDataSource(dataSource);
	}

	public static Material LETTER_MENU_MATERIAL = Material.LECTERN;
	public static Material ITEM_MENU_MATERIAL = Material.CHEST;
	
	public static Material SEND_LETTER_MATERIAL = Material.HOPPER;

	@Override
	public void init(Player player, InventoryContents contents) {
		Builder letterInventoryBuilder = LetterContentProvider.getBuilder(this.getDataSource());
		letterInventoryBuilder.parent(contents.inventory());
		SmartInventory letterInventory = letterInventoryBuilder.build();
		
		contents.set(1, 2, ClickableItem.of(new ItemStackBuilder(LETTER_MENU_MATERIAL).setName("�f�lMenu des lettres re�us").build(), e -> {
			letterInventory.open(player);
		}));
		

		Builder itemInventoryBuilder = ItemContentProvider.getBuilder(this.getDataSource());
		itemInventoryBuilder.parent(contents.inventory());
		SmartInventory itemInventory = itemInventoryBuilder.build();
		contents.set(1, 6, ClickableItem.of(new ItemStackBuilder(ITEM_MENU_MATERIAL).setName("�f�lMenu des objets re�us").build(), e ->  {
			itemInventory.open(player);
		}));
		
		contents.set(2, 4, ClickableItem.empty(new ItemStackBuilder(SEND_LETTER_MATERIAL).setName("Envoyer une lettre").build()  ) );//TODO
	}

	@Override
	public void update(Player player, InventoryContents contents) {
		
	}

	private UUID getDataSource() {
		return dataSource;
	}

	private void setDataSource(UUID dataSource) {
		this.dataSource = dataSource;
	}
	
	public static Builder getBuilder(UUID dataSource) {
		return Main.getBuilder()
		        .id("MailBox_Principal")
		        .provider(new MailBoxProvider(dataSource))
		        .size(3, 9)
		        .title("�lMenu Principal");
	}
	
}