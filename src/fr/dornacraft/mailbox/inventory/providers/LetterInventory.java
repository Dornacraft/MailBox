package fr.dornacraft.mailbox.inventory.providers;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.dornacraft.devtoolslib.smartinvs.ClickableItem;
import fr.dornacraft.devtoolslib.smartinvs.content.InventoryContents;
import fr.dornacraft.devtoolslib.smartinvs.content.Pagination;
import fr.dornacraft.devtoolslib.smartinvs.content.SlotIterator;
import fr.dornacraft.mailbox.ItemStackBuilder;
import fr.dornacraft.mailbox.DataManager.DataHolder;
import fr.dornacraft.mailbox.DataManager.DataManager;
import fr.dornacraft.mailbox.DataManager.LetterData;
import fr.dornacraft.mailbox.DataManager.LetterType;
import fr.dornacraft.mailbox.DataManager.MailBoxController;
import fr.dornacraft.mailbox.DataManager.filters.Filter;
import fr.dornacraft.mailbox.DataManager.filters.FilterOperator;
import fr.dornacraft.mailbox.DataManager.filters.FilterTransformer;
import fr.dornacraft.mailbox.inventory.MailBoxInventoryHandler;
import fr.dornacraft.mailbox.inventory.builders.InventoryProviderBuilder;
import fr.dornacraft.mailbox.sql.LetterDataSQL;

public class LetterInventory extends InventoryProviderBuilder {

	// Materials
	public static Material STANDARD_LETTER_MATERIAL = Material.PAPER;
	public static Material SYSTEM_LETTER_MATERIAL = Material.MAP;
	public static Material ANNOUNCE_LETTER_MATERIAL = Material.FILLED_MAP;
	public static Material NO_LETTER_TYPE_MATERIAL = Material.GLASS;

	public static Material NON_READ_LETTERS_MATERIAL = Material.BELL;
	public static Material PLAYER_FILTER_MATERIAL = Material.PLAYER_HEAD;
	public static Material DATE_SORT_MATERIAL = Material.REPEATER;

	// primary
	private DataHolder dataSource;

	// secondary
	private Filter<String> authorsFilter = new Filter<>();
	private Filter<LetterData> dataFilter = new Filter<>();
	private LetterType showedLetterType = null;
	private Integer letterTypeIndex = -1;
	private Boolean isSortingByDecreasingDate = true;

	private MailBoxInventoryHandler inventoryHandler = MailBoxInventoryHandler.getInstance();
	private DataManager dataManager = MailBoxController.getInstance().getDataManager();

	// builder
	public LetterInventory(DataHolder dataSource) {
		super("MailBox_Letters", "§lMenu des lettres", 5);

		this.setDataSource(dataSource);
		this.setDataFilter(new Filter<LetterData>(dataManager.getTypeData(this.dataSource, LetterData.class)));

	}

	public LetterInventory(DataHolder dataSource, InventoryProviderBuilder parent) {
		super("MailBox_Letters", "§lMenu des lettres", 5);

		this.setDataSource(dataSource);
		this.setDataFilter(new Filter<LetterData>(dataManager.getTypeData(this.dataSource, LetterData.class)));
		this.setParent(parent);
	}

	@Override
	public void initializeInventory(Player player, InventoryContents contents) {
		Pagination pagination = contents.pagination();
		pagination.setItemsPerPage(27);

		// CONTENT
		this.dynamicContent(player, contents);

		contents.fillRow(3,ClickableItem.empty(new ItemStackBuilder(inventoryHandler.BORDER_MATERIAL).setName(" ").build()));

		// FOOT
		if (!pagination.isFirst()) {
			contents.set(4, 1, inventoryHandler.getPreviousPageItem(player, contents));
		}

		contents.set(4, 2,ClickableItem.of(new ItemStackBuilder(PLAYER_FILTER_MATERIAL)
				.setName("§c§7" + (this.getAuthorsFilter().isEmpty() ? "Filtre par joueurs" : this.getAuthorsFilter().size() + " joueurs selectionnés")).build(), e -> {
							PlayerSelectorInventory selector = new PlayerSelectorInventory(this.getAuthorsFilter(), "§lExpéditeurs a affichés:", this);
							selector.openInventory(player);

						}));

		contents.set(4, 4, ClickableItem.of(new ItemStackBuilder(inventoryHandler.DELETE_ALL_MATERIAL)
				.setName("§4§lSupprimer les lettres affichées.").build(), e -> {
					Filter<Long> idList = Filter.transform(new Filter<LetterData>(getDataFilter().applyFilters() ), new FilterTransformer<LetterData, Long>() {

						@Override
						public Long execute(LetterData obj) {
							return obj.getId();
						}
					});

					DeletionDatasContentProvider inv = new DeletionDatasContentProvider(this.dataSource, idList.getEntries(), "§4§lSupprimer les " + idList.size() + " lettres ?", this);
					inv.openInventory(player);
					
				}));

		if (!pagination.isLast()) {
			contents.set(4, 7, inventoryHandler.getNextPageItem(player, contents));
		}

		contents.set(4, 8, this.goBackItem(player));
	}

	@Override
	public void updateInventory(Player player, InventoryContents contents) {
		this.dynamicContent(player, contents);
	}

	private void dynamicContent(Player player, InventoryContents contents) {
		if (this.getShowedLetterType() != null) {
			this.getDataFilter().putFilterOperator(LETTER_OPERATOR_TYPE );

		} else {
			this.getDataFilter().removeFilterOperator(LETTER_OPERATOR_TYPE );

		}

		if (!this.getAuthorsFilter().isEmpty()) {
			this.getDataFilter().putFilterOperator(LETTER_OERATOR_AUTHORS );
			
		}

		if (this.getIsSortingByDecreasingDate()) {
			this.getDataFilter().sort(dataManager.ascendingDateComparator().reversed());

		} else {
			this.getDataFilter().sort(dataManager.ascendingDateComparator());

		}
		
		Filter<LetterData> afterFfiltering = this.getDataFilter().applyFilters();
		
		ClickableItem[] clickableItems = new ClickableItem[afterFfiltering.size()];

		for (Integer index = 0; index < afterFfiltering.size(); index++) {
			LetterData tempData = afterFfiltering.get(index);

			clickableItems[index] = ClickableItem.of(inventoryHandler.generateItemRepresentation(tempData), e -> {
				ClickType clickType = e.getClick();
				ItemStack cursor = e.getCursor();

				if (clickType == ClickType.LEFT) {
					if (cursor.getType() == Material.WRITTEN_BOOK) {
						System.out.println("OK");

					} else {// lecture dans le chat
						MailBoxController.getInstance().readLetter(player, tempData);
						player.closeInventory();

					}

				} else if (clickType == ClickType.RIGHT && player.getUniqueId().equals(tempData.getUuid())) {//Toggle read state
					tempData.setIsRead(!tempData.getIsRead());
					LetterDataSQL.getInstance().update(tempData);

				} else if (clickType == ClickType.CONTROL_DROP) {// supprimer
					DeletionDataContentProvider inv = new DeletionDataContentProvider(this.getDataSource(), tempData.getId(), "§c§lSupprimer la lettre ?", this);
					inv.openInventory(player);

				}

			});
		}

		Pagination pagination = contents.pagination();
		pagination.setItems(clickableItems);
		pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0));
		contents.set(4, 3, generateCycleFilters());
		contents.set(4, 5, generateSortByDateItem());
		contents.set(4, 6, generateNonReadLettersItem(player));
	}

	public final FilterOperator<LetterData> LETTER_OERATOR_AUTHORS = new FilterOperator<LetterData>("LETTER_OERATOR_AUTHORS") {

		@Override
		public Boolean checker(LetterData letterData) {
			return getAuthorsFilter().containsEntry(letterData.getAuthor() );
		}
	};
	
	public final FilterOperator<LetterData> LETTER_NOT_YET_READ = new FilterOperator<LetterData>("LETTER_NOT_YET_READ") {

		@Override
		public Boolean checker(LetterData letterData) {
			return letterData.getIsRead().equals(false);
		}
	};
	public final FilterOperator<LetterData> LETTER_OPERATOR_TYPE = new FilterOperator<LetterData>("LETTER_OPERATOR_TYPE") {

		@Override
		public Boolean checker(LetterData letterData) {
			return letterData.getLetterType().equals(getShowedLetterType());
		}
	};

	// generate items
	private ClickableItem generateNonReadLettersItem(Player player) {
		List<LetterData> list = Filter.filter(this.getDataFilter().getEntries(), LETTER_NOT_YET_READ);
		
		ItemStack itemStack = new ItemStackBuilder(NON_READ_LETTERS_MATERIAL)
				.setName(String.format("§l§eVous avez %s lettres non lues.", list.size()))
				.addLore("clique pour toutes les")
				.addLore("marquée comme lues.").build();

		return ClickableItem.of(itemStack, e -> {
			for (LetterData letterData : this.getDataFilter() ) {
				letterData.setIsRead(true);
				LetterDataSQL.getInstance().update(letterData);
			}
		});
	}

	private ClickableItem generateSortByDateItem() {
		ItemStackBuilder itemStackBuilder = new ItemStackBuilder(DATE_SORT_MATERIAL).setName("§7§lAffichage:");

		if (this.getIsSortingByDecreasingDate()) {

			itemStackBuilder.addLore(" -> du plus récent au plus ancien")
					.addLore("clique pour toutes les triées")
					.addLore("du plus ancien au plus récent.");
		} else {
			itemStackBuilder.addLore(" -> du plus ancien au plus récent")
					.addLore("clique pour toutes les triées")
					.addLore("du plus récent au plus ancien.");

		}

		return ClickableItem.of(itemStackBuilder.build(), e -> {
			this.setIsSortingByDecreasingDate(!this.getIsSortingByDecreasingDate());
		});

	}

	private ClickableItem generateCycleFilters() {
		ItemStackBuilder itemStackBuilder = new ItemStackBuilder(
				inventoryHandler.getLetterTypeRepresentation(this.getShowedLetterType()))
						.addLore("droit / gauche: choisir filtre")
						.addLore("Drop pour supprimer le filtre");

		if (this.letterTypeIndex < 0) {
			this.setShowedLetterType(null);

		} else {
			this.setShowedLetterType(LetterType.values()[this.letterTypeIndex]);
		}

		String filter = this.getShowedLetterType() == null ? "aucun" : this.getShowedLetterType().name().toLowerCase();
		itemStackBuilder.setName("§f§lFiltre par type: " + filter);

		return ClickableItem.of(itemStackBuilder.build(), e -> {
			ClickType click = e.getClick();

			if (click == ClickType.RIGHT) {
				this.cycleAddIndex(true);

			} else if (click == ClickType.LEFT) {
				this.cycleAddIndex(false);

			} else if (click == ClickType.DROP) {
				this.setLetterTypeIndex(-1);
			}
		});

	}

	// manipulation
	private void cycleAddIndex(Boolean b) {
		Integer index = this.letterTypeIndex;
		Integer minIndex = -1;
		Integer maxIndex = LetterType.values().length - 1;

		if (b) {
			index = index + 1;

		} else {
			index = index - 1;
		}

		if (index < minIndex) {
			index = maxIndex;

		} else if (index > maxIndex) {
			index = minIndex;

		}

		this.setLetterTypeIndex(index);

	}
	
	// getters setters
	public void setLetterTypeIndex(Integer letterTypeIndex) {
		this.letterTypeIndex = letterTypeIndex;
	}

	public Filter<LetterData> getDataFilter() {
		return dataFilter;
	}

	public void setDataFilter(Filter<LetterData> dataFilter) {
		this.dataFilter = dataFilter;
	}

	public DataHolder getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataHolder dataSource) {
		this.dataSource = dataSource;
	}

	public LetterType getShowedLetterType() {
		return showedLetterType;
	}

	public void setShowedLetterType(LetterType showedLetterType) {
		this.showedLetterType = showedLetterType;
	}

	public Boolean getIsSortingByDecreasingDate() {
		return isSortingByDecreasingDate;
	}

	public void setIsSortingByDecreasingDate(Boolean isSortingByDecreasingDate) {
		this.isSortingByDecreasingDate = isSortingByDecreasingDate;
	}

	public Filter<String> getAuthorsFilter() {
		return authorsFilter;
	}

	public void setAuthorsFilter(Filter<String> authorsFilter) {
		this.authorsFilter = authorsFilter;
	}
}
