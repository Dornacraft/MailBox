package fr.dornacraft.mailbox.inventory.providers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import fr.dornacraft.devtoolslib.smartinvs.ClickableItem;
import fr.dornacraft.devtoolslib.smartinvs.content.InventoryContents;
import fr.dornacraft.devtoolslib.smartinvs.content.Pagination;
import fr.dornacraft.devtoolslib.smartinvs.content.SlotIterator;
import fr.dornacraft.mailbox.ItemStackBuilder;
import fr.dornacraft.mailbox.inventory.builders.FilterableInventoryBuilder;
import fr.dornacraft.mailbox.inventory.builders.InventoryBuilder;

public class PlayerSelector extends InventoryBuilder {
	public static Material CHOOSE_FACTION_MATERIAL = Material.MAGENTA_WALL_BANNER;
	
	private FilterableInventoryBuilder filterable;
	
	public PlayerSelector(FilterableInventoryBuilder filter, InventoryBuilder parent) {
		super("MailBox_Player_Selector", "§lChoisir la cible", 3, parent);
		this.setFilterable(filter);
	}
	
	//FIXME uuid des joueurs online est different du meme joueur offline
	private List<OfflinePlayer> getPlayers(Boolean containingOffline) {
		List<OfflinePlayer> res = new ArrayList<>();
		
		for(OfflinePlayer offplayer : Bukkit.getOfflinePlayers() ) {
			if(!res.contains(offplayer)) {
				if(containingOffline ) {
					res.add(offplayer);
					
				} else if (offplayer.getPlayer() != null){
					res.add(offplayer);
				}
			}
		}
		
		return res;
	}

	@Override
	public void initializeInventory(Player player, InventoryContents contents) {
		Pagination pagination = contents.pagination();
		pagination.setItemsPerPage(27);
		pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0));
		
		contents.set(1, 2, ClickableItem.empty(new ItemStackBuilder(CHOOSE_FACTION_MATERIAL).setName("Choisir faction").build() ) );
		
		contents.set(1, 4, ClickableItem.of(new ItemStackBuilder(Material.PLAYER_HEAD).setName("Joueur précis").build(), e -> {
			
		}));
		
		contents.set(1, 6, ClickableItem.of(new ItemStackBuilder(Material.PLAYER_HEAD).setName("Joueur du server").build(), e -> {
			ClickType clickType = e.getClick();
			
			if(clickType == ClickType.LEFT ) {
				FilterableInventoryBuilder filterable = this.getFilterable();
				filterable.setFilterList(getPlayers(false) );
				this.returnToParent(player);
				
			} else if (clickType == ClickType.RIGHT ) {
				FilterableInventoryBuilder filterable = this.getFilterable();
				filterable.setFilterList(getPlayers(true) );
				this.returnToParent(player);
				
			}
			
		}));
		
	}

	@Override
	public void updateInventory(Player player, InventoryContents contents) {
		
	}

	private FilterableInventoryBuilder getFilterable() {
		return filterable;
	}

	private void setFilterable(FilterableInventoryBuilder filterable) {
		this.filterable = filterable;
	}
	

}
