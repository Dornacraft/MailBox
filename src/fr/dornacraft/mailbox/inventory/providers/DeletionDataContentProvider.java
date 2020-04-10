package fr.dornacraft.mailbox.inventory.providers;

import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import fr.dornacraft.devtoolslib.smartinvs.SmartInventory.Builder;
import fr.dornacraft.devtoolslib.smartinvs.content.InventoryContents;
import fr.dornacraft.mailbox.DataManager.Data;
import fr.dornacraft.mailbox.DataManager.DataHolder;
import fr.dornacraft.mailbox.DataManager.ItemData;
import fr.dornacraft.mailbox.DataManager.LetterData;
import fr.dornacraft.mailbox.DataManager.MailBoxController;

public class DeletionDataContentProvider extends ConfirmationContentBuilder {
	public static final String INVENTORY_SUB_ID = "deleteItem";
	
	private DataHolder holder;
	private Long dataId;
	
	public DeletionDataContentProvider(DataHolder dataSource, Long dataId, String InventoryTitle) {
		super(INVENTORY_SUB_ID, InventoryTitle);
		this.setHolder(dataSource);
		this.setDataId(dataId);
		
		// ClickableItem.empty(MailBoxInventoryHandler.getInstance().generateItemRepresentation(this.getHolder().getData(this.getDataId())) )

	}
	
	public static Builder builder(DataHolder holder, Long dataId) {
		Data data = holder.getData(dataId);
		String obj = "";
		
		if(data instanceof ItemData) {
			obj = "l'objet";
		} else if (data instanceof LetterData) {
			obj = "la lettre";
		}
		
		DeletionDataContentProvider deletionProvivder = new DeletionDataContentProvider(holder, dataId, String.format("�c�lSupprimer %s?", obj));
		Builder res = deletionProvivder.getBuilder();
		res.provider(deletionProvivder);
		
		return res;
	}

	@Override
	public Consumer<InventoryClickEvent> onConfirmation(Player player, InventoryContents contents) {
		return e -> {
			
			MailBoxController.getInstance().deleteData(this.getHolder(), this.getDataId());
			contents.inventory().getParent().get().open(player);
			
		};
	}

	@Override
	public Consumer<InventoryClickEvent> onAnnulation(Player player, InventoryContents contents) {
		return null;
	}

	@Override
	public void onUpdate(Player player, InventoryContents contents) {
		Data data = this.getHolder().getData(this.getDataId());
		
		if(data instanceof ItemData) {
			if(MailBoxController.getInstance().isOutOfDate((ItemData) data)) {
				MailBoxController.getInstance().deleteItem(this.getHolder(), data.getId());
				contents.inventory().getParent().get().open(player);
			}
		}
	}

	public DataHolder getHolder() {
		return holder;
	}

	private void setHolder(DataHolder holder) {
		this.holder = holder;
	}
	public Long getDataId() {
		return dataId;
	}
	
	private void setDataId(Long dataId) {
		this.dataId = dataId;
	}

}