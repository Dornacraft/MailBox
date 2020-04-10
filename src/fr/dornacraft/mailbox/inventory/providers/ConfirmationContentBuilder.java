package fr.dornacraft.mailbox.inventory.providers;

import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import fr.dornacraft.devtoolslib.smartinvs.ClickableItem;
import fr.dornacraft.devtoolslib.smartinvs.SmartInventory.Builder;
import fr.dornacraft.devtoolslib.smartinvs.content.InventoryContents;
import fr.dornacraft.devtoolslib.smartinvs.content.InventoryProvider;
import fr.dornacraft.mailbox.ItemStackBuilder;
import fr.dornacraft.mailbox.Main;
import fr.dornacraft.mailbox.inventory.MailBoxInventoryHandler;

public abstract class ConfirmationContentBuilder implements InventoryProvider {
	public static Material CONFIRMATION_MATERIAL = Material.RED_TERRACOTTA;
	public static Material ANNULATION_MATERIAL = Material.GREEN_TERRACOTTA;
	
	private ClickableItem preview = null;
	private String subId = null;
	private String title = null;
	
	public ConfirmationContentBuilder(String subId, String title, ClickableItem preview) {
		this.setSubId(subId);
		this.setTitle(title);
		this.setPreview(preview);
		
	}
	
	public ConfirmationContentBuilder(String subId, String title) {
		this.setSubId(subId);
		this.setTitle(title);
		
	}
	
	@Override
	public void init(Player player, InventoryContents contents) {
		Consumer<InventoryClickEvent> consumer = MailBoxInventoryHandler.getInstance().getGoBackListener(player, contents);
		if(this.onAnnulation(player, contents) != null) {
			consumer = this.onAnnulation(player, contents);
		}
		
		contents.set(1, 2, ClickableItem.of(new ItemStackBuilder(ANNULATION_MATERIAL).setName("§f§lAnnuler").build(), consumer) );
		
		if(this.getPreview() != null) {
			contents.set(1, 4, this.getPreview() );
		}
		
		contents.set(1, 6, ClickableItem.of(new ItemStackBuilder(CONFIRMATION_MATERIAL).setName("§4§lConfirmer").build(), onConfirmation(player, contents)) );
		
	}
	
	public abstract Consumer<InventoryClickEvent> onConfirmation(Player player, InventoryContents contents);
	public abstract Consumer<InventoryClickEvent> onAnnulation(Player player, InventoryContents contents);
	public abstract void onUpdate(Player player, InventoryContents contents);

	@Override
	public void update(Player player, InventoryContents contents) {
		this.onUpdate(player, contents);
	}
	
	public String getSubId() {
		return subId;
	}

	public void setSubId(String subId) {
		this.subId = subId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public ClickableItem getPreview() {
		return preview;
	}

	public void setPreview(ClickableItem preview) {
		this.preview = preview;
	}
	
	public Builder getBuilder() {
		return Main.getBuilder()
		        .id("MailBox_Confirmation_" + this.getSubId())
		        .size(3, 9)
		        .title(this.getTitle());
	}
	
	
}
