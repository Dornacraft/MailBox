package fr.dornacraft.mailbox.inventory.providers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import fr.dornacraft.devtoolslib.smartinvs.ClickableItem;
import fr.dornacraft.devtoolslib.smartinvs.SmartInventory;
import fr.dornacraft.devtoolslib.smartinvs.content.InventoryContents;
import fr.dornacraft.devtoolslib.smartinvs.content.Pagination;
import fr.dornacraft.devtoolslib.smartinvs.content.SlotIterator;
import fr.dornacraft.mailbox.ItemStackBuilder;
import fr.dornacraft.mailbox.DataManager.DataHolder;
import fr.dornacraft.mailbox.DataManager.DataManager;
import fr.dornacraft.mailbox.DataManager.ItemData;
import fr.dornacraft.mailbox.DataManager.MailBoxController;
import fr.dornacraft.mailbox.inventory.MailBoxInventoryHandler;
import fr.dornacraft.mailbox.inventory.builders.InventoryProviderBuilder;

public class ItemInventory extends InventoryProviderBuilder {
	
	public static Material RULES_MATERIAL = Material.WRITABLE_BOOK;
	public static Material RECOVER_ALL_MATERIAl = Material.CHEST;
	
	private DataHolder dataSource;
	
	private MailBoxInventoryHandler inventoryHandler = MailBoxInventoryHandler.getInstance();
	private DataManager dataManager = MailBoxController.getInstance().getDataManager();
	
	public ItemInventory(DataHolder dataSource) {
		super("MailBox_Items", "§lMenu des objets", 5);
		this.setDataSource(dataSource);
	}
	
	public ItemInventory(DataHolder dataSource, InventoryProviderBuilder parent) {
		super("MailBox_Items", "§lMenu des objets", 5);
		this.setDataSource(dataSource);
		this.setParent(parent);
		
	}
	
	private void dynamicContent(Player player, InventoryContents contents) {
		List<ItemData> itemList = dataManager.getTypeData(this.getDataSource(), ItemData.class);
		
		itemList.sort(dataManager.ascendingDateComparator().reversed());
		
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
							if(player.getUniqueId().equals(this.getDataSource().getOwnerUuid()) ) {//l'inventaire appartien au joueur en parametre
								if(clickType == ClickType.LEFT ) {
									MailBoxController.getInstance().recoverItem(player, dataId);
									
								}
							} else {
								if (clickType == ClickType.DROP || clickType == ClickType.CONTROL_DROP) {
									DeletionDataInventory inv = new DeletionDataInventory(this.getDataSource(), dataId, "§c§lSupprimer l'objet ?", this);
									inv.openInventory(player);

								}
							}
						});
				
			}
		}
		
		Pagination pagination = contents.pagination();
		pagination.setItems(clickableItems);
		pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0));
	}

	@Override
	public void initializeInventory(Player player, InventoryContents contents) {
		SmartInventory inventory = contents.inventory();
		Pagination pagination = contents.pagination();
		pagination.setItemsPerPage(27);
		
		this.dynamicContent(player, contents);
		
		contents.fillRow(3, ClickableItem.empty(new ItemStackBuilder(MailBoxInventoryHandler.getInstance().BORDER_MATERIAL).setName(" ").build() ) );
		
		if(!pagination.isFirst() ) {
			contents.set(4, 1, ClickableItem.of(new ItemStackBuilder(inventoryHandler.PAGINATION_MATERIAL).setName("§f§lPage précédente").build(), e -> inventory.open(player, pagination.previous().getPage())) );
		}
		
		if(!this.getDataSource().getOwnerUuid().equals(player.getUniqueId())) { //TODO add permission delete all other
			contents.set(4,  4, ClickableItem.of(new ItemStackBuilder(inventoryHandler.DELETE_ALL_MATERIAL).setName("§4§lVider la boîte").build(), e -> {
				List<ItemData> dataList = dataManager.getTypeData(this.dataSource, ItemData.class);
				List<Long> listDataId = new ArrayList<>();
				for(ItemData data : dataList) {
					listDataId.add(data.getId());
				}
				
				DeletionDatasInventory deletionDatasInventory = new DeletionDatasInventory(this.dataSource, listDataId, "§4§lSupprimer les " + listDataId.size() +" objets ?", this);
				deletionDatasInventory.openInventory(player);
			}));
		}
		
		contents.set(4,  3, ClickableItem.empty(new ItemStackBuilder(RULES_MATERIAL).setName("§e§lRappel")
				.addLore("Le stockage de ressources via la boîte")
				.addLore("de réception par l'intermédiaire de")
				.addLore("l'HDV est interdit. Afin de prévenir et")
				.addLore("sanctionner toute tentative, un contrôle")
				.addLore("strict est effectué régulièrement et les")
				.addLore("dispositions nécessaires seront prises")
				.addLore("en cas d'abus (cf. réglement).")
				.build()
				)
			);
		
		contents.set(4, 5, generateRecoverAll(player, contents) );
		
		if(!pagination.isLast()) {
			contents.set(4, 7, ClickableItem.of(new ItemStackBuilder(inventoryHandler.PAGINATION_MATERIAL).setName("Page suivante").build(), e -> inventory.open(player, pagination.next().getPage())) );
		}
		
		contents.set(4, 8, this.goBackItem(player) );
	}

	@Override
	public void updateInventory(Player player, InventoryContents contents) {
		dynamicContent(player, contents);		
	}
	
	private ClickableItem generateRecoverAll(Player player, InventoryContents contents) {
		ItemStackBuilder itemStackBuilder = new ItemStackBuilder(RECOVER_ALL_MATERIAl).setName("§e§lTout récupéré");

		return ClickableItem.of(itemStackBuilder.build(), e -> {
			if(this.getDataSource().getOwnerUuid().equals(player.getUniqueId())) {//TODO permission take all other
				for (ItemData itemData : dataManager.getTypeData(this.getDataSource(), ItemData.class)) {
					if (!MailBoxController.getInstance().recoverItem(player, itemData.getId()) ) {
						break;
					}
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
	
}
