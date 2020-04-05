package fr.dornacraft.mailbox;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import fr.dornacraft.mailbox.sql.DataSQL;
import fr.dornacraft.mailbox.sql.ItemDataSQL;
import fr.dornacraft.mailbox.sql.LetterDataSQL;

public class MailBoxController {

	private static MailBoxController INSTANCE = new MailBoxController();
	private DataManager dataManager = new DataManager();

	private MailBoxController() {
	}

	public static MailBoxController getInstance() {
		return INSTANCE;
	}

	public DataHolder getHolderFromDataBase(Player player) {// ajout par rapport au diagramme
		DataHolder res = new DataHolder(new ArrayList<>());
		List<Data> dataList = DataSQL.getInstance().getDataList(player);

		for (Data data : dataList) {
			ItemData itemData = ItemDataSQL.getInstance().find(data.getId());
			LetterData letterData = LetterDataSQL.getInstance().find(data.getId());

			if (itemData != null) {
				res.addData(itemData);

			} else if (letterData != null) {
				res.addData(letterData);
			}
		}

		return res;
	}

	// utile lors des reload
	public void initialize() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			dataManager.getCache().put(player.getUniqueId(), getHolderFromDataBase(player));
		}
	}

	public void sendLetter(Player recipient, ItemStack book) { // FIXME changer player par UUID ? TODO modifier parametres -> ajouter lettertype
		if (recipient != null) {
			if (book.getType() == Material.WRITTEN_BOOK && book.hasItemMeta() && book.getItemMeta() instanceof BookMeta) {
				BookMeta bookMeta = (BookMeta) book.getItemMeta();

				String author = bookMeta.getAuthor();
				String object = bookMeta.getTitle();
				List<String> content = bookMeta.getPages();

				Data data = new DataFactory(recipient.getUniqueId(), author, object);
				LetterData letterData = new LetterData(data, LetterType.STANDARD, content, false);
				
				
				DataManager dataManager = MailBoxController.getInstance().getDataManager();
				DataHolder holder = dataManager.getDataHolder(recipient.getUniqueId());

				if (holder != null) {
					holder.addData(data);
				}
				
				LetterDataSQL.getInstance().create(letterData);
				recipient.sendMessage("Vous avez reçu un lettre de la part de " + author);
			}
		}
	}

	public void respondToLetter(Player player, Long id, ItemStack book) {// TODO

	}

	public void readLetter(Player player, Long id) {// TODO

	}

	public void deleteLetter(Player player, Long id) {
		DataHolder pHolder = dataManager.getDataHolder(player.getUniqueId());
		Data data = pHolder.getData(id);

		if (data instanceof LetterData) {
			pHolder.removeData(id);
			LetterDataSQL.getInstance().delete((LetterData) data);
		}

	}

	public void purgeLetters(Player player) {
		dataManager.purgeData(dataManager.getDataHolder(player.getUniqueId()), LetterData.class);
	}

	public void recoverItem(Player player, Long id) {
		DataHolder pHolder = dataManager.getDataHolder(player.getUniqueId());
		Data data = pHolder.getData(id);

		if (data instanceof ItemData) {
			ItemData itemData = (ItemData) data;
			player.getInventory().addItem(itemData.getItem());
			deleteItem(player, id);

		}
	}

	public void deleteItem(Player player, Long id) {
		DataHolder pHolder = dataManager.getDataHolder(player.getUniqueId());
		ItemData itemData = (ItemData) pHolder.getData(id);
		pHolder.removeData(id);
		ItemDataSQL.getInstance().delete(itemData);
	}

	public void sendItem(Player recipient, ItemStack itemstack) {
		if (recipient != null) {
			Data data = new DataFactory(recipient.getUniqueId(), recipient.getName(), "object hard code"); //TODO parametrize author
			ItemData itemData = new ItemData(data, itemstack, Duration.ofSeconds(60));// TODO parametrize duration sur les items
			ItemDataSQL.getInstance().create(itemData);
			
			DataHolder pHolder = getDataManager().getDataHolder(recipient.getUniqueId());
			if(pHolder == null) {
				getDataManager().getCache().put(recipient.getUniqueId(), new DataHolder(new ArrayList<Data>()));
				pHolder = getDataManager().getDataHolder(recipient.getUniqueId());
			}
			
			pHolder.addData(itemData);
			recipient.sendMessage("Vous avez reçu un objet de la part de " + recipient.getName());
		}
	}

	public DataManager getDataManager() {
		return dataManager;
	}
}
