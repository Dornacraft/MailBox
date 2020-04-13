package fr.dornacraft.mailbox.inventory.providers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.dornacraft.devtoolslib.smartinvs.ClickableItem;
import fr.dornacraft.devtoolslib.smartinvs.SmartInventory;
import fr.dornacraft.devtoolslib.smartinvs.SmartInventory.Builder;
import fr.dornacraft.devtoolslib.smartinvs.content.InventoryContents;
import fr.dornacraft.devtoolslib.smartinvs.content.Pagination;
import fr.dornacraft.devtoolslib.smartinvs.content.SlotIterator;
import fr.dornacraft.mailbox.ItemStackBuilder;
import fr.dornacraft.mailbox.DataManager.DataHolder;
import fr.dornacraft.mailbox.DataManager.DataManager;
import fr.dornacraft.mailbox.DataManager.LetterData;
import fr.dornacraft.mailbox.DataManager.LetterType;
import fr.dornacraft.mailbox.DataManager.MailBoxController;
import fr.dornacraft.mailbox.inventory.MailBoxInventoryHandler;
import fr.dornacraft.mailbox.inventory.builders.FilterableInventoryBuilder;
import fr.dornacraft.mailbox.inventory.builders.InventoryBuilder;
import fr.dornacraft.mailbox.sql.LetterDataSQL;

public class LetterInventory extends FilterableInventoryBuilder {
	
	//Materials
	public static Material STANDARD_LETTER_MATERIAL = Material.PAPER;
	public static Material SYSTEM_LETTER_MATERIAL = Material.MAP;
	public static Material ANNOUNCE_LETTER_MATERIAL = Material.FILLED_MAP;
	public static Material NO_LETTER_TYPE_MATERIAL = Material.GLASS;
	
	public static Material NON_READ_LETTERS_MATERIAL = Material.BELL;
	public static Material PLAYER_FILTER_MATERIAL = Material.PLAYER_HEAD;
	public static Material DATE_SORT_MATERIAL = Material.REPEATER;
	
	//primary
	private DataHolder dataSource;
	private Player sender;
	
	//secondary
	private LetterType filterType = null;
	private Integer letterTypeIndex = -1;
	private Boolean isSortingByDecreasingDate = true;
	
	private MailBoxInventoryHandler inventoryHandler = MailBoxInventoryHandler.getInstance();
	private DataManager dataManager = MailBoxController.getInstance().getDataManager();
	
	//builder
	public LetterInventory(DataHolder dataSource) {
		super("MailBox_Letters", "§lMenu des lettres", 5);
		
		this.dataSource = dataSource;
		
	}
	
	public LetterInventory(DataHolder dataSource, InventoryBuilder parent) {
		super("MailBox_Letters", "§lMenu des lettres", 5);
		
		this.dataSource = dataSource;
		this.setParent(parent);
	}

	@Override
	public void initializeInventory(Player player, InventoryContents contents) {
		Pagination pagination = contents.pagination();
		pagination.setItemsPerPage(27);
		
		//CONTENT
		this.dynamicContent(player, contents);

		contents.fillRow(3, ClickableItem.empty(new ItemStackBuilder(inventoryHandler.BORDER_MATERIAL).setName(" ").build()) );
		
		//FOOT
		if (!pagination.isFirst()) {
			contents.set(4, 1, inventoryHandler.getPreviousPageItem(player, contents) );
		}
		
		contents.set(4,  2, ClickableItem.of(new ItemStackBuilder(PLAYER_FILTER_MATERIAL)
				.setName(this.getFilterList().isEmpty() ? "§4§lFiltrer par joueur(s)" : "§4§l" + this.getFilterList().size() + " joueurs selectionnés").build(), e -> {
			PlayerSelector selector = new PlayerSelector(this, this);
			selector.setParent(this);
			selector.openInventory(player);
			
			
		}));
		
		contents.set(4,  4, ClickableItem.of(new ItemStackBuilder(inventoryHandler.DELETE_ALL_MATERIAL).setName("§4§lVider la boite").build(), e -> {
			List<LetterData> dataList = dataManager.getTypeData(this.dataSource, LetterData.class);
			List<Long> listDataId = new ArrayList<>();
			for(LetterData data : dataList) {
				listDataId.add(data.getId());
			}
			
			Builder builder = DeletionDatasInventory.builder(this.dataSource, listDataId, "§c§lSupprimer les "+ listDataId.size() +" lettres ?");
			builder.parent(contents.inventory());
			SmartInventory deletionInventory = builder.build();
			deletionInventory.open(player);
		}));
		
		
		if (!pagination.isLast()) {
			contents.set(4, 7, inventoryHandler.getNextPageItem(player, contents) );
		}
		
		contents.set(4, 8, this.goBackItem(player) );
	}

	@Override
	public void updateInventory(Player player, InventoryContents contents) {
		this.dynamicContent(player, contents);
	}
	
	private void dynamicContent(Player player, InventoryContents contents) {
		List<LetterData> letterList = dataManager.getTypeData(this.dataSource, LetterData.class);
		
		if(this.getFilterType() != null) {
			letterList = filterByType(letterList, this.getFilterType());
		}
		
		if(!this.getFilterList().isEmpty() ) {
			letterList = applyPlayerFilter(letterList);
		}
		
		if(this.isSortingByDecreasingDate ) {
			letterList.sort(dataManager.compareByAscendingDate().reversed());
			
		} else {
			letterList.sort(dataManager.compareByAscendingDate());
		}
		
		ClickableItem[] clickableItems = new ClickableItem[letterList.size()];
		
		for(Integer index = 0; index < letterList.size(); index ++) {
			LetterData tempData = letterList.get(index);
			
			clickableItems[index] = ClickableItem.of(inventoryHandler.generateItemRepresentation(tempData),
					e -> {
						ClickType clickType = e.getClick();
						ItemStack cursor = e.getCursor();
						
						if (clickType == ClickType.LEFT ) {
							if(cursor.getType() == Material.WRITTEN_BOOK) {
								System.out.println("OK");
								
							} else {//lecture dans le chat
								MailBoxController.getInstance().readLetter(player, tempData);
								player.closeInventory();
								
							}
							
						} else if(clickType == ClickType.RIGHT && player.getUniqueId().equals(tempData.getUuid()) ) {//marquer comme lu
							tempData.setIsRead(!tempData.getIsRead());
							LetterDataSQL.getInstance().update(tempData);
							
						} else if(clickType == ClickType.CONTROL_DROP) {//supprimer
							Builder builder = DeletionDataInventory.builder(this.dataSource, tempData.getId() );
							builder.parent(contents.inventory());
							SmartInventory deletionInventory = builder.build();
							deletionInventory.open(player);
							
						}
						
					});
		}
		
		Pagination pagination = contents.pagination();
		pagination.setItems(clickableItems);
		pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0));
		contents.set(4, 3, generateCycleFilters() );
		contents.set(4, 5, generateSortByDateItem() );
		contents.set(4, 6, generateNonReadLettersItem(player, filterLetterByReadState(dataManager.getTypeData(this.dataSource, LetterData.class), false)) );
	}
	
	//generate items
	private ClickableItem generateNonReadLettersItem(Player player, List<LetterData> dataList) {
		ItemStack itemStack = new ItemStackBuilder(NON_READ_LETTERS_MATERIAL).setName(String.format("§eVous avez %s lettres non lues", dataList.size() ))
				.addLore("clique pour toutes les")
				.addLore("marquée comme lues.")
				.build();
		
		return ClickableItem.of(itemStack, e -> {
			for(LetterData letterData : dataList) {
				letterData.setIsRead(true);
				LetterDataSQL.getInstance().update(letterData);
			}
		});
	}
	
	private ClickableItem generateSortByDateItem() {
		ItemStackBuilder itemStackBuilder = new ItemStackBuilder(DATE_SORT_MATERIAL).setName("§e§lAffichage:");
		
		if (this.isSortingByDecreasingDate) {

			itemStackBuilder.addLore(" -> du plus récent au plus ancien").addLore("clique pour toutes les triées")
					.addLore("du plus ancien au plus récent.");
		} else {
			itemStackBuilder.addLore(" -> du plus ancien au plus récent").addLore("clique pour toutes les triées")
					.addLore("du plus récent au plus ancien.");

		}
		
		return ClickableItem.of(itemStackBuilder.build(), e -> {
			this.isSortingByDecreasingDate = !this.isSortingByDecreasingDate;
			}
		);
		
	}
	
	private ClickableItem generateCycleFilters() {
		ItemStackBuilder itemStackBuilder = new ItemStackBuilder(inventoryHandler.getLetterTypeRepresentation(this.getFilterType()))
				.addLore("droit / gauche: choisir filtre")
				.addLore("Drop pour supprimer le filtre");
		
		if (this.letterTypeIndex < 0) {
			this.setFilterType(null);
			
		} else {
			this.setFilterType(LetterType.values()[this.letterTypeIndex]);
		}
		
		String filter = this.getFilterType() == null ? "aucun" : this.getFilterType().name().toLowerCase();
		itemStackBuilder.setName("§f§lFiltre par type: " + filter);
		
		return ClickableItem.of(itemStackBuilder.build(), e -> {
			ClickType click = e.getClick();
			
			if(click == ClickType.RIGHT) {
				this.cycleAddIndex(true);
				
			} else if (click == ClickType.LEFT) {
				this.cycleAddIndex(false);
				
			} else if (click == ClickType.DROP) {
				this.setLetterTypeIndex(-1);
			}
		});
		
	}
	
	//filters
	private List<LetterData> filterByType(List<LetterData> dataList, LetterType type){
		List<LetterData> res = new ArrayList<>();
		
		for(LetterData letterData : dataList) {
			if(letterData.getLetterType() == type) {
				res.add(letterData);
			}
		}
		
		
		return res;
	}
	
	private List<LetterData> filterLetterByReadState(List<LetterData> dataList, Boolean isRead){
		List<LetterData> res = new ArrayList<>();
		
		for(LetterData letterData : dataList) {
			if(letterData.getIsRead().equals(isRead)) {
				res.add(letterData);
			}
		}
		
		
		return res;
	}
	
	private List<LetterData> applyPlayerFilter(List<LetterData> dataList){
		List<LetterData> res = new ArrayList<>();
		
		for(LetterData letterData : dataList) {
			for(OfflinePlayer offplayer : this.getFilterList()) {
				if(offplayer.getName().contentEquals(letterData.getAuthor()) ) {
					res.add(letterData);
				}
			}
		}
		
		
		return res;
	}
	
	//manipulation
	private void cycleAddIndex(Boolean b) {
		Integer index = this.letterTypeIndex;
		Integer minIndex = -1;
		Integer maxIndex = LetterType.values().length -1;
		
		if(b) {
			index = index + 1;
			
		} else {
			index = index - 1;
		}
		
		if(index < minIndex) {
			index = maxIndex;
			
		} else if(index > maxIndex){
			index = minIndex;
			
		}
		
		this.setLetterTypeIndex(index);
		
	}
	/*
	private List<LetterData> applyPlayerFilters(List<LetterData> letterList){
		List<LetterData> res = new ArrayList<>();
		
		if(!this.filterPlayer.isEmpty() ) {
			for(LetterData letterData : letterList) {
				if(offplayer.getName().equals(letterData.getAuthor())) {
					
					
				}
			}
			
		}
		
		
		
		return res;
	}
	*/
	
	// getters setters
	public LetterType getFilterType() {
		return filterType;
	}

	public void setFilterType(LetterType filterType) {
		this.filterType = filterType;
	}

	public void setLetterTypeIndex(Integer letterTypeIndex) {
		this.letterTypeIndex = letterTypeIndex;
	}
/*
	public PlayerSelector getSelector() {
		
		if(this.selector == null) {
			this.setSelector(new PlayerSelector(this.getSender()) );
		}
		
		return this.selector;
	}*/

	public Player getSender() {
		return sender;
	}

	public void setSender(Player sender) {
		this.sender = sender;
	}
	
}
