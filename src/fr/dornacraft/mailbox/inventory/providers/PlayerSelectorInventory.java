package fr.dornacraft.mailbox.inventory.providers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import fr.dornacraft.devtoolslib.smartinvs.ClickableItem;
import fr.dornacraft.devtoolslib.smartinvs.content.InventoryContents;
import fr.dornacraft.devtoolslib.smartinvs.content.Pagination;
import fr.dornacraft.devtoolslib.smartinvs.content.SlotIterator;
import fr.dornacraft.mailbox.ItemStackBuilder;
import fr.dornacraft.mailbox.DataManager.filters.Filter;
import fr.dornacraft.mailbox.DataManager.filters.FilterOperator;
import fr.dornacraft.mailbox.inventory.builders.InventoryProviderBuilder;
import fr.dornacraft.mailbox.listeners.PlayerChatSelector;

public class PlayerSelectorInventory extends InventoryProviderBuilder {
	public static final Material CHOOSE_ALL_MATERIAL = Material.NETHER_STAR;
	public static final Material CHOOSE_FACTION_MATERIAL = Material.MAGENTA_BANNER;
	public static final Material CHOOSE_PRECISE_PLAYER_MATERIAL = Material.PLAYER_HEAD;
	
	private Filter<String> authorsFilter;
	
	public PlayerSelectorInventory(Filter<String> filter, String invTitle) {
		super("MailBox_Player_Selector", invTitle, 3);
		this.setAuthorsFilter(filter);
	}
	
	public PlayerSelectorInventory(Filter<String> filter, String invTitle, InventoryProviderBuilder parent) {
		super("MailBox_Player_Selector", invTitle, 3, parent);
		this.setAuthorsFilter(filter);
	}
	
	public final FilterOperator<String> PLAYER_NAME_ONLINE = new FilterOperator<String>("PLAYER_NAME_ONLINE") {

		@Override
		public Boolean checker(String obj) {
			return Bukkit.getPlayer(obj) != null;
		}
	};

	@Override
	public void initializeInventory(Player player, InventoryContents contents) {
		Pagination pagination = contents.pagination();
		pagination.setItemsPerPage(27);
		pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0));
		
		contents.set(1, 2, ClickableItem.of(new ItemStackBuilder(CHOOSE_FACTION_MATERIAL).setName("§f§lChoisir faction").build(), e -> {
			
		}));
		
		contents.set(1, 4, ClickableItem.of(new ItemStackBuilder(CHOOSE_PRECISE_PLAYER_MATERIAL).setName("§f§lJoueur précis").build(), e -> {
			player.closeInventory();
			PlayerChatSelector pcs = new PlayerChatSelector(player, this.getAuthorsFilter(), this);
			pcs.start();
			
		}));
		
		contents.set(1, 6, ClickableItem.of(new ItemStackBuilder(CHOOSE_ALL_MATERIAL).setName("§f§lJoueur du server").build(), e -> {
			ClickType clickType = e.getClick();
			
			if(clickType == ClickType.LEFT ) {
				this.getAuthorsFilter().putFilterOperator(PLAYER_NAME_ONLINE );
				this.getAuthorsFilter().applyFilters();
				this.returnToParent(player);
				
			} else if (clickType == ClickType.RIGHT ) {
				this.getAuthorsFilter().removeFilterOperator(PLAYER_NAME_ONLINE );
				
			}
			
		}));
		
		contents.set(2, 8, this.goBackItem(player) );
		
	}

	@Override
	public void updateInventory(Player player, InventoryContents contents) {
		contents.set(0, 4, ClickableItem.of(new ItemStackBuilder(Material.REDSTONE)
				.setName("§f§lLe filtre contient:")
				.addLore(String.format(" - %s joueurs", this.getAuthorsFilter().applyFilters().size() ))
				.addLore("Click droit pour supprimer les filtres")
				.build(), e -> {
					if(e.getClick() == ClickType.RIGHT) {
						this.getAuthorsFilter().purgeEntries();
					}
				}));
	}

	public Filter<String> getAuthorsFilter() {
		return authorsFilter;
	}

	public void setAuthorsFilter(Filter<String> authorsFilter) {
		this.authorsFilter = authorsFilter;
	}

}
