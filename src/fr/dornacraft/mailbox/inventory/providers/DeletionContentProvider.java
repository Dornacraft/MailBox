package fr.dornacraft.mailbox.inventory.providers;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.dornacraft.devtoolslib.smartinvs.ClickableItem;
import fr.dornacraft.devtoolslib.smartinvs.SmartInventory.Builder;
import fr.dornacraft.devtoolslib.smartinvs.content.InventoryContents;
import fr.dornacraft.devtoolslib.smartinvs.content.InventoryProvider;
import fr.dornacraft.mailbox.ItemStackBuilder;
import fr.dornacraft.mailbox.Main;
import fr.dornacraft.mailbox.DataManager.Data;
import fr.dornacraft.mailbox.DataManager.ItemData;
import fr.dornacraft.mailbox.DataManager.LetterData;
import fr.dornacraft.mailbox.DataManager.MailBoxController;
import fr.dornacraft.mailbox.inventory.MailBoxInventoryHandler;

public class DeletionContentProvider implements InventoryProvider {
	public static Material CONFIRMATION_MATERIAL = Material.RED_TERRACOTTA;
	public static Material ANNULATION_MATERIAL = Material.GREEN_TERRACOTTA;
	private Data data;
	
	public DeletionContentProvider(UUID dataSource, Data data) {
		this.setData(data);

	}

	private Data getData() {
		return data;
	}

	private void setData(Data data) {
		this.data = data;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		contents.set(1, 2, ClickableItem.of(new ItemStackBuilder(ANNULATION_MATERIAL).setName("§f§lAnnuler").build(), MailBoxInventoryHandler.getInstance().getGoBackListener(player, contents)) );
		
		contents.set(1, 4, ClickableItem.empty(MailBoxInventoryHandler.getInstance().generateItemRepresentation(this.getData())));
		
		contents.set(1, 6, ClickableItem.of(new ItemStackBuilder(CONFIRMATION_MATERIAL).setName("§c§lSupprimer").build(), e -> {
			if(this.getData() != null) {
				MailBoxController.getInstance().deleteData(player, this.getData().getId());
				
			}
			contents.inventory().getParent().get().open(player);
		}));
	}

	@Override
	public void update(Player player, InventoryContents contents) {
		if(this.getData() instanceof ItemData) {
			ItemData itemData = (ItemData) this.getData();
			
			if(MailBoxController.getInstance().isOutOfDate(itemData)) {
				//MailBoxController.getInstance().deleteItem(player.getUniqueId(), itemData.getId());
				contents.inventory().getParent().get().open(player);
			}
		}		
	}
	
	public static Builder getBuilder(UUID dataSource, Data data) {
		String formatTitle = "§4§lSupprimer %s?";
		String title = "";
		
		if(data instanceof ItemData) {
			title = String.format(formatTitle, "l'objet");
			
		} else if (data instanceof LetterData) {
			title = String.format(formatTitle, "la lettre");
		}
		
		
		return Main.getBuilder()
		        .id("customInventory")
		        .provider(new DeletionContentProvider(dataSource, data))
		        .size(3, 9)
		        .title(title);
	}

}
