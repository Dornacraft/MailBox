package fr.dornacraft.mailbox.inventory.providers;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.dornacraft.devtoolslib.smartinvs.ClickableItem;
import fr.dornacraft.devtoolslib.smartinvs.SmartInventory;
import fr.dornacraft.devtoolslib.smartinvs.SmartInventory.Builder;
import fr.dornacraft.devtoolslib.smartinvs.content.InventoryContents;
import fr.dornacraft.devtoolslib.smartinvs.content.InventoryProvider;
import fr.dornacraft.devtoolslib.smartinvs.content.Pagination;
import fr.dornacraft.devtoolslib.smartinvs.content.SlotIterator;
import fr.dornacraft.mailbox.ItemStackBuilder;
import fr.dornacraft.mailbox.Main;
import fr.dornacraft.mailbox.DataManager.Data;
import fr.dornacraft.mailbox.DataManager.DataHolder;
import fr.dornacraft.mailbox.DataManager.DataManager;
import fr.dornacraft.mailbox.DataManager.LetterData;
import fr.dornacraft.mailbox.DataManager.LetterType;
import fr.dornacraft.mailbox.DataManager.MailBoxController;
import fr.dornacraft.mailbox.inventory.MailBoxInventoryHandler;
import fr.dornacraft.mailbox.sql.LetterDataSQL;

public class LetterContentProvider implements InventoryProvider {
	
	//Materials
	public static Material STANDARD_LETTER_MATERIAL = Material.PAPER;
	public static Material SYSTEM_LETTER_MATERIAL = Material.MAP;
	public static Material ANNOUNCE_LETTER_MATERIAL = Material.FILLED_MAP;
	public static Material NO_LETTER_TYPE_MATERIAL = Material.GLASS;
	
	public static Material MARK_AS_READ = Material.GLASS;
	public static Material NON_READ_LETTERS_MATERIAL = Material.BELL;
	
	public static Material PLAYER_FILTER_MATERIAL = Material.HOPPER;
	public static Material LETTER_NO_FILTER_MATERIAL = Material.PUMPKIN_PIE;
	public static Material DATE_SORT_MATERIAL = Material.REPEATER;
	
	//primary
	private DataHolder dataSource;
	private List<LetterData> letterList = new ArrayList<>();
	
	//secondary
	private LetterType filterType = null;
	private Integer letterTypeIndex = -1;
	private String filterPlayer = null;
	private Boolean isSortingByDecreasingDate = true;
	
	private MailBoxInventoryHandler inventoryHandler = MailBoxInventoryHandler.getInstance();
	private DataManager manager = MailBoxController.getInstance().getDataManager();
	
	//builder
	private LetterContentProvider(DataHolder dataSource) {
		this.dataSource = dataSource;
		
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		Pagination pagination = contents.pagination();
		pagination.setItemsPerPage(27);
		
		//CONTENT
		this.dynamicContent(player, contents);

		contents.fillRow(3, ClickableItem.empty(new ItemStackBuilder(inventoryHandler.BORDER_MATERIAL).setName(" ").build()) );
		
		//FOOT
		if (!pagination.isFirst()) {
			contents.set(4, 1, inventoryHandler.getPreviousPageItem(player, contents) );
		}
		
		if (!pagination.isLast()) {
			contents.set(4, 7, inventoryHandler.getNextPageItem(player, contents) );
		}
		
		contents.set(4, 8, inventoryHandler.getGoBackItem(player, contents));
	}

	@Override
	public void update(Player player, InventoryContents contents) {
		this.dynamicContent(player, contents);
	}
	
	private void dynamicContent(Player player, InventoryContents contents) {
		List<LetterData> letterList = manager.getTypeData(this.dataSource, LetterData.class);
		
		if(this.getFilterType() != null) {
			letterList = filterByType(letterList, this.getFilterType());
		}
		
		if(this.isSortingByDecreasingDate ) {
			letterList.sort(compareByAscendingDate().reversed());
			
		} else {
			letterList.sort(compareByAscendingDate());
		}
		
		ClickableItem[] clickableItems = new ClickableItem[letterList.size()];
		
		for(Integer index = 0; index < letterList.size(); index ++) {//TODO filters compatible with other filters
			LetterData tempData = letterList.get(index);
			
			clickableItems[index] = ClickableItem.of(inventoryHandler.generateItemRepresentation(tempData),
					e -> {
						ClickType clickType = e.getClick();
						ItemStack cursor = e.getCurrentItem();
						
						if (clickType == ClickType.LEFT ) {
							if(cursor != null && cursor.getType() == Material.WRITTEN_BOOK) {
								System.out.println("OK");
								
							} else {//lecture dans le chat
								MailBoxController.getInstance().readLetter(player, tempData);
								player.closeInventory();
								
							}
							
						} else if(clickType == ClickType.RIGHT) {//r�ponse rapide

							
						} else if(clickType == ClickType.RIGHT) {//marquer comme lu
							tempData.setIsRead(!tempData.getIsRead());
							LetterDataSQL.getInstance().update(tempData);
							
						} else if(clickType == ClickType.CONTROL_DROP) {//supprimer
							Builder builder = DeletionContentProvider.getBuilder(this.dataSource, tempData);
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
		contents.set(4, 6, generateNonReadLettersItem(player, filterLetterByReadState(manager.getTypeData(this.dataSource, LetterData.class), false)) );
	}
	
	//generate items
	private ClickableItem generateNonReadLettersItem(Player player, List<LetterData> dataList) {
		ItemStack itemStack = new ItemStackBuilder(NON_READ_LETTERS_MATERIAL).setName(String.format("�eVous avez %s lettres non lues", dataList.size() ))
				.addLore("clique pour toutes les")
				.addLore("marqu�e comme lues.")
				.build();
		
		return ClickableItem.of(itemStack, e -> {
			for(LetterData letterData : dataList) {
				letterData.setIsRead(true);
				LetterDataSQL.getInstance().update(letterData);
			}
		});
	}
	
	private ClickableItem generateSortByDateItem() {
		ItemStackBuilder itemStackBuilder = new ItemStackBuilder(DATE_SORT_MATERIAL).setName("�e�lAffichage:");
		
		if (this.isSortingByDecreasingDate) {

			itemStackBuilder.addLore(" -> du plus r�cent au plus ancien").addLore("clique pour toutes les tri�es")
					.addLore("du plus ancien au plus r�cent.");
		} else {
			itemStackBuilder.addLore(" -> du plus ancien au plus r�cent").addLore("clique pour toutes les tri�es")
					.addLore("du plus r�cent au plus ancien.");

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
		itemStackBuilder.setName("�f�lFiltre par type: " + filter);
		
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
	
	//sorters
	private Comparator<Data> compareByAscendingDate(){
		return new Comparator<Data>() {

			@Override
			public int compare(Data data1, Data data2) {
				Timestamp date1 = data1.getCreationDate();
				Timestamp date2 = data2.getCreationDate();
				
				return date1.compareTo(date2);
			}
		};
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
	
	//static
	public static Builder getBuilder(DataHolder dataSource) {
		return Main.getBuilder()
		        .id("MailBox_Letters")
		        .provider(new LetterContentProvider(dataSource))
		        .size(5, 9)
		        .title("�lMenu des lettres");
	}
	
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

	public List<LetterData> getLetterList() {
		return letterList;
	}

	public void setLetterList(List<LetterData> letterList) {
		this.letterList = letterList;
	}
	
}