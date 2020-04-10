package fr.dornacraft.mailbox.inventory.providers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import fr.dornacraft.devtoolslib.smartinvs.SmartInventory.Builder;
import fr.dornacraft.devtoolslib.smartinvs.content.InventoryContents;
import fr.dornacraft.mailbox.DataManager.Data;
import fr.dornacraft.mailbox.DataManager.DataHolder;
import fr.dornacraft.mailbox.DataManager.ItemData;
import fr.dornacraft.mailbox.DataManager.MailBoxController;
import fr.dornacraft.mailbox.inventory.builders.ConfirmationContentBuilder;

public class DeletionDatasContentProvider extends ConfirmationContentBuilder {
	public static final String INVENTORY_SUB_ID = "deleteItems";
	
	private DataHolder holder;
	private List<Long> dataIdList = new ArrayList<>();
	
	public DeletionDatasContentProvider(DataHolder dataSource, List<Long> listDataId, String inventoryTitle) {
		super(INVENTORY_SUB_ID, inventoryTitle);
		this.setHolder(dataSource);
		this.setDataIdList(listDataId);

	}
	
	public static Builder builder(DataHolder holder, List<Long> listDataId, String title) {
		DeletionDatasContentProvider deletionProvivder = new DeletionDatasContentProvider(holder, listDataId, title);
		Builder res = deletionProvivder.getBuilder();
		res.provider(deletionProvivder);
		
		return res;
	}

	@Override
	public Consumer<InventoryClickEvent> onConfirmation(Player player, InventoryContents contents) {
		return e -> {
			for(Long id : this.getDataIdList() ) {
				MailBoxController.getInstance().deleteData(this.getHolder(), id);
				
			}
			
			contents.inventory().getParent().get().open(player);
			
		};
	}

	@Override
	public Consumer<InventoryClickEvent> onAnnulation(Player player, InventoryContents contents) {
		return null;
	}

	@Override
	public void onUpdate(Player player, InventoryContents contents) {
		Iterator<Long> it = this.getDataIdList().iterator();

		while (it.hasNext()) {
			Long id = it.next();
			Data data = this.getHolder().getData(id);

			if (data != null) {
				if (data instanceof ItemData && MailBoxController.getInstance().isOutOfDate((ItemData) data)) {
					MailBoxController.getInstance().deleteItem(this.getHolder(), data.getId());
					it.remove();

					if (this.getDataIdList().isEmpty()) {
						contents.inventory().getParent().get().open(player);

					}
				}
			} else {
				it.remove();
			}

		}
	}

	public DataHolder getHolder() {
		return holder;
	}

	private void setHolder(DataHolder holder) {
		this.holder = holder;
	}
	
	public List<Long> getDataIdList() {
		return dataIdList;
	}

	public void setDataIdList(List<Long> dataIdList) {
		this.dataIdList = dataIdList;
	}
}
