package fr.dornacraft.mailbox.inventory.providers;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import fr.dornacraft.devtoolslib.smartinvs.ClickableItem;
import fr.dornacraft.devtoolslib.smartinvs.content.InventoryContents;
import fr.dornacraft.devtoolslib.smartinvs.content.Pagination;
import fr.dornacraft.devtoolslib.smartinvs.content.SlotIterator;
import fr.dornacraft.mailbox.ItemStackBuilder;
import fr.dornacraft.mailbox.inventory.builders.InventoryProviderBuilder;
import fr.dornacraft.mailbox.inventory.providers.utils.AuthorFilter;
import fr.dornacraft.mailbox.listeners.PlayerChatSelector;

public class PlayerSelectorInventory extends InventoryProviderBuilder {
	public static final Material CHOOSE_ALL_MATERIAL = Material.NETHER_STAR;
	public static final Material CHOOSE_FACTION_MATERIAL = Material.MAGENTA_BANNER;
	public static final Material CHOOSE_PRECISE_PLAYER_MATERIAL = Material.PLAYER_HEAD;
	
	private AuthorFilter authorFilter;
	private PlayerChatSelector selector = null;
	private ClickableItem optional;
	
	public PlayerSelectorInventory(AuthorFilter authorFilter, String invTitle) {
		super("MailBox_Player_Selector", invTitle, 3);
		this.setAuthorFilter(authorFilter);
	}
	
	public PlayerSelectorInventory(AuthorFilter authorFilter, String invTitle, InventoryProviderBuilder parent) {
		super("MailBox_Player_Selector", invTitle, 3, parent);
		this.setAuthorFilter(authorFilter);
	}
	
	@Override
	public void initializeInventory(Player player, InventoryContents contents) {
		if(this.getSelector() == null) {
			this.setSelector(new PlayerChatSelector(player, this.getAuthorFilter(), this));
			
		}
		
		Pagination pagination = contents.pagination();
		pagination.setItemsPerPage(27);
		pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0));
		
		contents.set(1, 2, ClickableItem.of(new ItemStackBuilder(CHOOSE_FACTION_MATERIAL).setName("§f§lChoisir faction").build(), e -> {
			
		}));
		
		contents.set(1, 4, ClickableItem.of(new ItemStackBuilder(CHOOSE_PRECISE_PLAYER_MATERIAL).setName("§f§lJoueur précis").build(), e -> {
			this.setFinalClose(false);
			this.getSelector().start();
			player.closeInventory();
			
			
		}));
		
		contents.set(1, 6, ClickableItem.of(new ItemStackBuilder(CHOOSE_ALL_MATERIAL).setName("§f§lJoueur du server").build(), e -> {
			ClickType clickType = e.getClick();
			
			if(clickType == ClickType.LEFT ) {
				this.getAuthorFilter().addAllIdentifiers(Arrays.asList(new String[] {"#online"}) );
				
			} else if (clickType == ClickType.RIGHT ) {
				this.getAuthorFilter().addAllIdentifiers(Arrays.asList(new String[] {"#offline"}));
			}
			
		}));
		
		contents.set(2, 8, this.goBackItem(player) );
		
	}

	@Override
	public void updateInventory(Player player, InventoryContents contents) {
		contents.set(0, 4, ClickableItem.of(new ItemStackBuilder(Material.REDSTONE)
				.setName("§f§lLe filtre contient:")
				.addLore(String.format(" - %s joueurs", this.getAuthorFilter().getList().size() ))
				.addLore("Click droit pour supprimer les filtres")
				.build(), e -> {
					if(e.getClick() == ClickType.RIGHT) {
						this.getAuthorFilter().clear();
					}
				}));
		
		if(this.getOptional() != null) {
			contents.set(2, 4, this.getOptional());
		}
	}

	public PlayerChatSelector getSelector() {
		return selector;
	}

	public void setSelector(PlayerChatSelector selector) {
		this.selector = selector;
	}

	public ClickableItem getOptional() {
		return optional;
	}

	public void setOptional(ClickableItem optional) {
		this.optional = optional;
	}

	public AuthorFilter getAuthorFilter() {
		return authorFilter;
	}
	public void setAuthorFilter(AuthorFilter authorFilter) {
		this.authorFilter = authorFilter;
	}
	
}
