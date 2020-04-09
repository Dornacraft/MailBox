package fr.dornacraft.mailbox.DataManager;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
	
	/**
	 * @param player joueyur cible
	 * @return un DataHolder avec les données du joueur cible trouvés directement depuis la base de donnée.
	 */
	public DataHolder getHolderFromDataBase(UUID uuid) {// ajout par rapport au diagramme
		DataHolder res = new DataHolder(new ArrayList<>());
		List<Data> dataList = DataSQL.getInstance().getDataList(uuid);

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
			dataManager.getCache().put(player.getUniqueId(), getHolderFromDataBase(player.getUniqueId()));
		}
	}
	
	/**
	 * trouve le dataHolder associé a l'uuid donnée
	 */
	public DataHolder getDataHolder(UUID uuid) {
		DataHolder res = dataManager.getDataHolder(uuid);
		
		if(res == null) {
			res = getHolderFromDataBase(uuid);
		}
		
		return res;
	}

	public void sendLetter(Player recipient, ItemStack book) { //FIXME changer player par UUID ? TODO modifier parametres -> ajouter lettertype
		if (recipient != null) {
			if (book.getType() == Material.WRITTEN_BOOK && book.hasItemMeta() && book.getItemMeta() instanceof BookMeta) {
				BookMeta bookMeta = (BookMeta) book.getItemMeta();

				String author = bookMeta.getAuthor();
				String object = bookMeta.getTitle();
				List<String> content = bookMeta.getPages();

				Data data = new DataFactory(recipient.getUniqueId(), author, object);
				LetterData letterData = new LetterData(data, LetterType.STANDARD, content, false);
				
				
				DataHolder holder = dataManager.getDataHolder(recipient.getUniqueId());
				if (holder != null) {
					holder.addData(letterData);
				}
				
				LetterDataSQL.getInstance().create(letterData);
				recipient.sendMessage("Vous avez reçu un lettre de la part de " + author);
			}
		}
	}

	public void respondToLetter(Player player, Long id, ItemStack book) {// TODO

	}
	
	/**
	 * Envoie le contenue de la lettre au joueur cible et la marque comme ayant été lu
	 * @param player joueur cible
	 * @param id l iD de la lettre a afficher dans le chat
	 */
	public void readLetter(Player player, LetterData letterData) {
		StringBuilder letter = new StringBuilder();
		letter.append("\n");
		letter.append(String.format("§e§lAuteur(e):§r %s\n", letterData.getAuthor()));
		
		SimpleDateFormat sdf =  new SimpleDateFormat("dd/MM/yyyy à HH:mm:ss");
		
		letter.append(String.format("§e§lDate de réçéption:§r %s\n", sdf.format(letterData.getCreationDate()) ));
		letter.append(String.format("§e§lObjet:§r %s\n", letterData.getObject() ));
		letter.append("§e§lMessage:§r\n");
		
		for(String page : letterData.getContent()) {
			letter.append("§r" + page + "§r");
		}
		
		player.sendMessage(letter.toString());
		
		letterData.setIsRead(true);
		LetterDataSQL.getInstance().update(letterData);
	}

	public void deleteLetter(Player player, Long id) {
		DataHolder pHolder = dataManager.getDataHolder(player.getUniqueId());
		Data data = pHolder.getData(id);

		if (data instanceof LetterData) {
			pHolder.removeData(id);
			LetterDataSQL.getInstance().delete((LetterData) data);
		}

	}
	
	public void deleteData(Player player, Long id) {
		DataHolder pHolder = dataManager.getDataHolder(player.getUniqueId());
		Data data = pHolder.getData(id);
		
		if(data instanceof ItemData) {
			MailBoxController.getInstance().deleteItem(pHolder, data.getId());
			
		} else if (data instanceof LetterData) {
			MailBoxController.getInstance().deleteLetter(player, data.getId());
		}
	}

	public void purgeLetters(Player player) {
		dataManager.purgeData(dataManager.getDataHolder(player.getUniqueId()), LetterData.class);
	}

	public void recoverItem(Player player, Long id) {
		
		if(player.getInventory().firstEmpty() != -1) {
			DataHolder pHolder = dataManager.getDataHolder(player.getUniqueId());
			Data data = pHolder.getData(id);
	
			if (data instanceof ItemData) {
				ItemData itemData = (ItemData) data;
				player.getInventory().addItem(itemData.getItem());
				deleteItem(pHolder, id);
	
			}
		} else {
			//TODO playsound ?
		}
	}
	
	public Boolean isOutOfDate(ItemData itemData) {
		
		LocalDateTime date = itemData.getCreationDate().toLocalDateTime(); // to datetime
		LocalDateTime added = date.plus(itemData.getDuration());
		return added.compareTo(LocalDateTime.now()) < 0 ;

	}
	
	public void deleteItem(DataHolder holder, Long id) {
		Data data = holder.getData(id);

		if (data instanceof ItemData) {
			holder.removeData(id);
			ItemDataSQL.getInstance().delete((ItemData) data);
		}
	}

	public void sendItem(Player recipient, ItemStack itemstack) {
		if (recipient != null) {
			Data data = new DataFactory(recipient.getUniqueId(), recipient.getName(), "object hard code"); //TODO parametrize author
			ItemData itemData = new ItemData(data, itemstack, Duration.ofSeconds(20));// TODO parametrize duration sur les items
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
