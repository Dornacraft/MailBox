package fr.dornacraft.mailbox.inventory.providers;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import fr.dornacraft.devtoolslib.smartinvs.ClickableItem;
import fr.dornacraft.devtoolslib.smartinvs.SmartInventory;
import fr.dornacraft.devtoolslib.smartinvs.SmartInventory.Builder;
import fr.dornacraft.devtoolslib.smartinvs.content.InventoryContents;
import fr.dornacraft.devtoolslib.smartinvs.content.InventoryProvider;
import fr.dornacraft.devtoolslib.smartinvs.content.Pagination;
import fr.dornacraft.devtoolslib.smartinvs.content.SlotIterator;
import fr.dornacraft.mailbox.ItemStackBuilder;
import fr.dornacraft.mailbox.Main;
import fr.dornacraft.mailbox.DataManager.DataHolder;
import fr.dornacraft.mailbox.DataManager.DataManager;
import fr.dornacraft.mailbox.DataManager.ItemData;
import fr.dornacraft.mailbox.DataManager.MailBoxController;
import fr.dornacraft.mailbox.inventory.MailBoxInventoryHandler;

public class ItemContentProvider implements  InventoryProvider {
	
	public static Material RULES_MATERIAL = Material.WRITABLE_BOOK;
	public static Material RECOVER_ALL_MATERIAl = Material.CHEST;
	
	private DataHolder dataSource;
	
	private MailBoxInventoryHandler inventoryHandler = MailBoxInventoryHandler.getInstance();
	private DataManager dataManager = MailBoxController.getInstance().getDataManager();
	
	public ItemContentProvider(DataHolder dataSource) {
		this.setDataSource(dataSource);
	}
	
	private void dynamicContent(Player player, InventoryContents contents) {
		List<ItemData> itemList = dataManager.getTypeData(this.getDataSource(), ItemData.class);
		
		itemList.sort(dataManager.compareByAscendingDate().reversed());
		
		ClickableItem[] clickableItems = new ClickableItem[itemList.size()];
		
		for(Integer index = 0; index < itemList.size(); index ++ ) {
			ItemData tempData = itemList.get(index);
			Long dataId = tempData.getId();
			
			if(MailBoxController.getInstance().isOutOfDate(tempData)) {
				MailBoxController.getInstance().deleteItem(this.getDataSource(), dataId);
				
			} else {
				clickableItems[index] = ClickableItem.of(inventoryHandler.generateItemRepresentation(tempData),
						e -> {
							ClickType clickType = e.getClick();
							if(clickType == ClickType.LEFT && player.getUniqueId().equals(this.getDataSource().getOwner()) ) {//l'inventaire appartien au joueur en parametre
								MailBoxController.getInstance().recoverItem(player, dataId);
								
							} else if(clickType == ClickType.DROP || clickType == ClickType.CONTROL_DROP) {//TODO ajouter permission
								Builder builder = DeletionDataContentProvider.builder(this.getDataSource(), dataId);
								builder.parent(contents.inventory());
								builder.build().open(player);
								
							}
						});
				
			}
		}
		
		Pagination pagination = contents.pagination();
		pagination.setItems(clickableItems);
		pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0));
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		SmartInventory inventory = contents.inventory();
		Pagination pagination = contents.pagination();
		pagination.setItemsPerPage(27);
		
		this.dynamicContent(player, contents);
		
		contents.fillRow(3, ClickableItem.empty(new ItemStackBuilder(MailBoxInventoryHandler.getInstance().BORDER_MATERIAL).setName(" ").build() ) );
		
		MailBoxInventoryHandler handler = MailBoxInventoryHandler.getInstance();
		
		if(!pagination.isFirst() ) {
			contents.set(4, 1, ClickableItem.of(new ItemStackBuilder(handler.PAGINATION_MATERIAL).setName("Page précédente").build(), e -> inventory.open(player, pagination.previous().getPage())) );
		}
		
		contents.set(4, 3, generateRecoverAll(player, contents) );
		
		if(!pagination.isLast()) {
			contents.set(4, 7, ClickableItem.of(new ItemStackBuilder(handler.PAGINATION_MATERIAL).setName("Page suivante").build(), e -> inventory.open(player, pagination.next().getPage())) );
		}
		
		contents.set(4, 8, MailBoxInventoryHandler.getInstance().getGoBackItem(player, contents) );
	}

	@Override
	public void update(Player player, InventoryContents contents) {
		dynamicContent(player, contents);		
	}
	
	private ClickableItem generateRecoverAll(Player player, InventoryContents contents) {
		ItemStackBuilder itemStackBuilder = new ItemStackBuilder(RECOVER_ALL_MATERIAl).setName("§eTout récupérer.");

		return ClickableItem.of(itemStackBuilder.build(), e -> {
			for (ItemData itemData : dataManager.getTypeData(this.getDataSource(), ItemData.class)) {
				if (!MailBoxController.getInstance().recoverItem(player, itemData.getId()) ) {
					break;
				}
			}
		});
	}

	public DataHolder getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataHolder dataSource) {
		this.dataSource = dataSource;
	}
	
	public static Builder getBuilder(DataHolder dataSource) {
		return Main.getBuilder()
		        .id("MailBox_Items")
		        .provider(new ItemContentProvider(dataSource))
		        .size(5, 9)
		        .title("§lMenu des objets");
	}

}
