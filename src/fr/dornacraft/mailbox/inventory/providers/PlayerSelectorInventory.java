package fr.dornacraft.mailbox.inventory.providers;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

import fr.dornacraft.devtoolslib.smartinvs.ClickableItem;
import fr.dornacraft.devtoolslib.smartinvs.content.InventoryContents;
import fr.dornacraft.devtoolslib.smartinvs.content.Pagination;
import fr.dornacraft.devtoolslib.smartinvs.content.SlotIterator;
import fr.dornacraft.mailbox.ItemStackBuilder;
import fr.dornacraft.mailbox.inventory.builders.InventoryProviderBuilder;
import fr.dornacraft.mailbox.listeners.PlayerChatSelector;

public class PlayerSelectorInventory extends InventoryProviderBuilder {
	public static final Material CHOOSE_ALL_MATERIAL = Material.NETHER_STAR;
	public static final Material CHOOSE_FACTION_MATERIAL = Material.MAGENTA_BANNER;
	public static final Material CHOOSE_PRECISE_PLAYER_MATERIAL = Material.PLAYER_HEAD;
	
	private List<String> showedAuthors;
	private PlayerChatSelector selector = null;
	
	public PlayerSelectorInventory(List<String> authorsFilter, String invTitle) {
		super("MailBox_Player_Selector", invTitle, 3);
		this.showedAuthors = authorsFilter;
	}
	
	public PlayerSelectorInventory(List<String> authorsFilter, String invTitle, InventoryProviderBuilder parent) {
		super("MailBox_Player_Selector", invTitle, 3, parent);
		this.showedAuthors = authorsFilter;
	}
	
	@Override
	public void initializeInventory(Player player, InventoryContents contents) {
		Pagination pagination = contents.pagination();
		pagination.setItemsPerPage(27);
		pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0));
		
		contents.set(1, 2, ClickableItem.of(new ItemStackBuilder(CHOOSE_FACTION_MATERIAL).setName("§f§lChoisir faction").build(), e -> {
			
		}));
		
		contents.set(1, 4, ClickableItem.of(new ItemStackBuilder(CHOOSE_PRECISE_PLAYER_MATERIAL).setName("§f§lJoueur précis").build(), e -> {
			this.setFinalClose(false);
			
			if(this.getSelector() == null) {
				this.setSelector(new PlayerChatSelector(player, this.getShowedAuthors(), this));
				
			}
			
			player.closeInventory();
			this.getSelector().start();
			
		}));
		
		contents.set(1, 6, ClickableItem.of(new ItemStackBuilder(CHOOSE_ALL_MATERIAL).setName("§f§lJoueur du server").build(), e -> {
			ClickType clickType = e.getClick();
			
			if(clickType == ClickType.LEFT ) {
				List<String> tempList = Bukkit.getOnlinePlayers().stream()
		                .map(Player::getName)
		                .collect(Collectors.toList());
				this.getShowedAuthors().addAll(tempList);
				
			} else if (clickType == ClickType.RIGHT ) {
				this.getShowedAuthors().clear();
			}
			
		}));
		
		contents.set(2, 8, this.goBackItem(player) );
		
	}
	
	public Boolean isThis(Inventory inv) {
		return null;
	}

	@Override
	public void updateInventory(Player player, InventoryContents contents) {
		contents.set(0, 4, ClickableItem.of(new ItemStackBuilder(Material.REDSTONE)
				.setName("§f§lLe filtre contient:")
				.addLore(String.format(" - %s joueurs", this.getShowedAuthors().size() ))
				.addLore("Click droit pour supprimer les filtres")
				.build(), e -> {
					if(e.getClick() == ClickType.RIGHT) {
						this.getShowedAuthors().clear();
					}
				}));
	}
	
	public List<String> getShowedAuthors() {
		return showedAuthors;
	}

	public PlayerChatSelector getSelector() {
		return selector;
	}

	public void setSelector(PlayerChatSelector selector) {
		this.selector = selector;
	}
}
