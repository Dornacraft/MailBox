package fr.dornacraft.mailbox.inventory.builders;

import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.scheduler.BukkitTask;

import fr.dornacraft.devtoolslib.smartinvs.ClickableItem;
import fr.dornacraft.devtoolslib.smartinvs.SmartInventory;
import fr.dornacraft.devtoolslib.smartinvs.SmartInventory.Builder;
import fr.dornacraft.devtoolslib.smartinvs.content.InventoryContents;
import fr.dornacraft.devtoolslib.smartinvs.content.InventoryProvider;
import fr.dornacraft.mailbox.ItemStackBuilder;
import fr.dornacraft.mailbox.Main;

public abstract class InventoryProviderBuilder implements InventoryProvider {
	public static Material GO_BACK_MATERIAL = Material.OAK_SIGN;
	
	private UUID uuid;
	private String id;
	private String title;
	private Integer rows;
	private InventoryProviderBuilder parent;
	private Consumer<BukkitTask> onQuitTask;
	private Boolean finalClose = true;
	private Listener listener;
	
	public InventoryProviderBuilder(String id, String title, Integer rows) {
		this.setId(id);
		this.setTitle(title);
		this.setRows(rows);
		
	}
	
	public InventoryProviderBuilder(String id, String title, Integer rows, InventoryProviderBuilder parent) {
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
		Bukkit.getScheduler().runTask(Main.getInstance(), e -> {
			this.setFinalClose(true);
			this.setUuid(player.getUniqueId());
			SmartInventory inv = this.getBuilder().build();
			inv.open(player);
			
			if(this.getListener() == null && this.getOnQuitTask() != null) {
				this.setListener(new Listener() {

					@EventHandler(priority = EventPriority.NORMAL)
					private void onCloseSelectorInventory(InventoryCloseEvent event) {
						HumanEntity HEntity = event.getPlayer();
						
						if(HEntity instanceof Player) {
							Player ePlayer = (Player) HEntity;
							
							if(ePlayer.getUniqueId().equals(getUuid())) {
								InventoryView view = event.getView();
								
								if(getFinalClose() ) {
									if (view.getType() == InventoryType.CHEST && view.getTitle().equals(getTitle()) && view.getTopInventory().getSize() == getRows() * 9) {
										if(getOnQuitTask() != null ) {
											Main.getInstance().getServer().getScheduler().runTask(Main.getInstance(), getOnQuitTask());
										}
										
										InventoryCloseEvent.getHandlerList().unregister(this);
									}
								}
							}
						}
					}
				});
				Main.getInstance().getServer().getPluginManager().registerEvents(this.getListener(), Main.getInstance());	
			}
			
		});
	}
	
	public void returnToParent(Player player) {
		if(this.getParent() != null) {
			this.getParent().openInventory(player);
			
		} else {
			player.closeInventory();
		}
		
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
		String name = this.getId().contains("principal") ? "§4§lQuitter" : "§c§lMenu précédent";
		
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

	public InventoryProviderBuilder getParent() {
		return parent;
	}

	public void setParent(InventoryProviderBuilder parent) {
		this.parent = parent;
	}

	public Boolean getFinalClose() {
		return finalClose;
	}

	public void setFinalClose(Boolean finalClose) {
		this.finalClose = finalClose;
	}

	public Consumer<BukkitTask> getOnQuitTask() {
		return onQuitTask;
	}

	public void setDefinitiveQuit(Consumer<BukkitTask> onQuitTask) {
		this.onQuitTask = onQuitTask;
	}

	public Listener getListener() {
		return listener;
	}

	public void setListener(Listener listener) {
		this.listener = listener;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
	
}
