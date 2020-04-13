package fr.dornacraft.mailbox.inventory.builders;

import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import fr.dornacraft.devtoolslib.smartinvs.ClickableItem;
import fr.dornacraft.devtoolslib.smartinvs.SmartInventory;
import fr.dornacraft.devtoolslib.smartinvs.SmartInventory.Builder;
import fr.dornacraft.devtoolslib.smartinvs.content.InventoryContents;
import fr.dornacraft.devtoolslib.smartinvs.content.InventoryProvider;
import fr.dornacraft.mailbox.ItemStackBuilder;
import fr.dornacraft.mailbox.Main;

public abstract class InventoryBuilder implements InventoryProvider {
	public static Material GO_BACK_MATERIAL = Material.OAK_SIGN;
	
	private String id;
	private String title;
	private Integer rows;
	private InventoryBuilder parent;
	
	public InventoryBuilder(String id, String title, Integer rows) {
		this.setId(id);
		this.setTitle(title);
		this.setRows(rows);
	}
	
	public InventoryBuilder(String id, String title, Integer rows, InventoryBuilder parent) {
		this(id, title, rows);
		this.setParent(parent);
	}
	
	protected Builder getBuilder() {
		Builder res = Main.getBuilder()
		        .id(this.getId() )
		        .provider(this)
		        .size(this.getRows(), 9)
		        .title(this.getTitle());
		
		return res;
	}
	
	public void openInventory(Player player) {
		SmartInventory inv = this.getBuilder().build();
		inv.open(player);
		
	}
	
	public void returnToParent(Player player) {
		this.getParent().openInventory(player);
		
	}
	
	/**
	 * Ouvre l'inventaire parent si il exist sinon ferme l'inventaire
	 * @param player le joueur cible
	 * @param contents l'inventaire
	 */
	protected Consumer<InventoryClickEvent> getGoBackListener(Player player) {
		Consumer<InventoryClickEvent> res = e -> player.closeInventory();
		
		if(this.getParent() != null ) {
			res =  e -> {
				this.getParent().openInventory(player);
			
			};
			
		} else {
			res = e -> {
				player.closeInventory();
				
			};
		}
		return res;
	}
	
	public ClickableItem goBackItem(Player player) {
		String name = this.getId().contains("principal") ? "§c§lQuitter" : "§cMenu précédent";
		
		return ClickableItem.of(new ItemStackBuilder(GO_BACK_MATERIAL).setName(name).build(), getGoBackListener(player) );
	}

	public String getId() {
		return id;
	}

	protected void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	protected void setTitle(String title) {
		this.title = title;
	}

	public Integer getRows() {
		return rows;
	}

	protected void setRows(Integer rows) {
		this.rows = rows;
	}
	
	public abstract void initializeInventory(Player player, InventoryContents contents);
	public abstract void updateInventory(Player player, InventoryContents contents);
	
	@Override
	public void init(Player p, InventoryContents c) {
		this.initializeInventory(p, c);
		
	}

	@Override
	public void update(Player p, InventoryContents c) {
		this.updateInventory(p, c);
		
	}

	public InventoryBuilder getParent() {
		return parent;
	}

	public void setParent(InventoryBuilder parent) {
		this.parent = parent;
	}
	
}
