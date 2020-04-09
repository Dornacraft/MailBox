package fr.dornacraft.mailbox.inventory.providers;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.dornacraft.devtoolslib.smartinvs.ClickableItem;
import fr.dornacraft.devtoolslib.smartinvs.SmartInventory.Builder;
import fr.dornacraft.devtoolslib.smartinvs.content.InventoryContents;
import fr.dornacraft.devtoolslib.smartinvs.content.InventoryProvider;
import fr.dornacraft.mailbox.ItemStackBuilder;
import fr.dornacraft.mailbox.Main;
import fr.dornacraft.mailbox.DataManager.Data;
import fr.dornacraft.mailbox.DataManager.DataHolder;
import fr.dornacraft.mailbox.DataManager.ItemData;
import fr.dornacraft.mailbox.DataManager.LetterData;
import fr.dornacraft.mailbox.DataManager.MailBoxController;
import fr.dornacraft.mailbox.inventory.MailBoxInventoryHandler;

public class DeletionContentProvider implements InventoryProvider {
	public static Material CONFIRMATION_MATERIAL = Material.RED_TERRACOTTA;
	public static Material ANNULATION_MATERIAL = Material.GREEN_TERRACOTTA;
	private DataHolder holder;
	private Data data;
	private List<? extends Data> dataList;
	
	public DeletionContentProvider(DataHolder dataSource, Data data) {
		this.setData(data);
		this.setHolder(dataSource);

	}
	
	public DeletionContentProvider(DataHolder dataSource, List<? extends Data> dataList) {
		this.setHolder(dataSource);
		this.setDataList(dataList);

	}

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		contents.set(1, 2, MailBoxInventoryHandler.getInstance().getGoBackItem(player, contents) );
		
		
		ItemStack item = null;
		if(this.getData() != null) {
			item = MailBoxInventoryHandler.getInstance().generateItemRepresentation(this.getData());
			
		} else if(this.getDataList() != null){
			item =  new ItemStackBuilder(Material.BOOK)
					.setName("§l§4Vous allez supprimer " + this.getDataList().size() + " objets!")
					.build();
		}
		
		contents.set(1, 4, ClickableItem.empty(item));
		
		contents.set(1, 6, ClickableItem.of(new ItemStackBuilder(CONFIRMATION_MATERIAL).setName("§lSupprimer").build(), e -> {
			if(this.getData() != null) {
				MailBoxController.getInstance().deleteData(this.getHolder(), this.getData().getId());
				
			} else if (this.getDataList() != null) {
				for(Data data : this.getDataList()) {
					MailBoxController.getInstance().deleteData(this.getHolder(), data.getId());
				}
			}
			contents.inventory().getParent().get().open(player);
		}));
		
	}

	@Override
	public void update(Player player, InventoryContents contents) {
		if(this.getData() instanceof ItemData) {
			ItemData itemData = (ItemData) this.getData();
			
			if(MailBoxController.getInstance().isOutOfDate(itemData)) {
				MailBoxController.getInstance().deleteItem(this.getHolder(), itemData.getId());
				contents.inventory().getParent().get().open(player);
			}
		}
		
	}

	public List<? extends Data> getDataList() {
		return dataList;
	}

	public void setDataList(List<? extends Data>dataList) {
		this.dataList = dataList;
	}

	public DataHolder getHolder() {
		return holder;
	}

	public void setHolder(DataHolder holder) {
		this.holder = holder;
	}
	
	public static Builder getBuilder(DataHolder holder, Data data) {
		String formatTitle = "§4§lSupprimer %s?";
		String title = "";
		
		if(data instanceof ItemData) {
			title = String.format(formatTitle, "l'objet");
			
		} else if (data instanceof LetterData) {
			title = String.format(formatTitle, "la lettre");
		}
		
		
		return Main.getBuilder()
		        .id("MailBox_Deletion")
		        .provider(new DeletionContentProvider(holder, data))
		        .size(3, 9)
		        .title(title);
	}
	
	public static Builder getBuilder(DataHolder holder, List<? extends Data> dataList) {
		String title = String.format("§4§lSupprimer les %s objets?", dataList.size());
		
		return Main.getBuilder()
		        .id("MailBox_Deletion")
		        .provider(new DeletionContentProvider(holder, dataList))
		        .size(3, 9)
		        .title(title);
	}

}
