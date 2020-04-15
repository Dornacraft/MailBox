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

import fr.dornacraft.mailbox.ItemStackBuilder;
import fr.dornacraft.mailbox.DataManager.factories.DataFactory;
import fr.dornacraft.mailbox.playerManager.PlayerInfo;
import fr.dornacraft.mailbox.playerManager.PlayerManager;
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
	 * @return un DataHolder avec les donn�es du joueur cible trouv�s directement depuis la base de donn�e.
	 */
	private DataHolder getHolderFromDataBase(UUID uuid) {// ajout par rapport au diagramme
		DataHolder res = new DataHolder(uuid, new ArrayList<>());
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
	
	//charge en m�moire les don�e de associé a l'uuid en parametre
	public void load(UUID uuid) {
		dataManager.getCache().put(uuid, getHolderFromDataBase(uuid) );
	}
	
	// utile lors des reload
	public void initialize() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			load(player.getUniqueId());
		}
	}
	
	/**
	 * trouve le dataHolder associ� a l'uuid donn�e
	 */
	public DataHolder getDataHolder(UUID uuid) {
		DataHolder res = dataManager.getDataHolder(uuid);
		
		if(res == null) {
			res = getHolderFromDataBase(uuid);
		}
		
		return res;
	}
	
	public void sendLetter(LetterData letterData) {
		LetterData temp = LetterDataSQL.getInstance().create(letterData);
		
		if(temp != null) {
			DataHolder holder = dataManager.getDataHolder(temp.getUuid());
			if (holder != null) {
				holder.addData(temp);
			}
			
			//notification
			Player recipient = Bukkit.getPlayer(temp.getUuid() );
			
			if(recipient != null) {
				recipient.getPlayer().sendMessage("Vous avez reçu un lettre de la part de " + letterData.getAuthor() );
			}
		} else {
			//TODO null pointer (erreur d'acces a la BDD
		}
		
	}
	
	public void sendLetter(UUID recipientUuid, ItemStack book) {//FIXME changer player par UUID ? TODO modifier parametres -> ajouter lettertype
		if (recipientUuid != null) {
			if (book.getType() == Material.WRITTEN_BOOK && book.hasItemMeta() && book.getItemMeta() instanceof BookMeta) {
				BookMeta bookMeta = (BookMeta) book.getItemMeta();

				String author = bookMeta.getAuthor();
				String object = bookMeta.getTitle();
				List<String> content = bookMeta.getPages();

				Data data = new DataFactory(recipientUuid, author, object);
				LetterData letterData = new LetterData(data, LetterType.STANDARD, content, false);
				
				sendLetter(letterData);
			}
		}
	}

	public void respondToLetter(Player player, Long id, ItemStack book) {// TODO

	}
	
	public ItemStack getBookView(LetterData letterData) {
		StringBuilder letterHead = new StringBuilder();
		letterHead.append(String.format("§e§lAutheur(e):§r %s\n", letterData.getAuthor()));
		
		SimpleDateFormat sdf =  new SimpleDateFormat("dd/MM/yyyy à HH:mm:ss");
		
		letterHead.append(String.format("§e§lDate de récéption:§r %s\n", sdf.format(letterData.getCreationDate()) ));
		letterHead.append(String.format("§e§lObject:\n - %s§r\n", letterData.getObject() ) );
		
		ItemStack book = new ItemStackBuilder(Material.WRITTEN_BOOK).build();
		BookMeta bookMeta = (BookMeta) book.getItemMeta();
		bookMeta.setAuthor(letterData.getAuthor());
		List<String> pages = new ArrayList<>();
		pages.add(letterHead.toString());
		pages.addAll(letterData.getContent() );
		
		bookMeta.setPages(pages);
		bookMeta.setTitle(letterData.getObject());
		
		book.setItemMeta(bookMeta);
		System.out.println("here");
		
		return book;
	}
	
	/**
	 * Envoie le contenue de la lettre au joueur cible et la marque comme ayant été lu si le joueur est aussi le destinataire
	 */
	public void readLetter(Player player, LetterData letterData) {
		player.openBook(getBookView(letterData));
		
		if(letterData.getUuid().equals(player.getUniqueId())) {
			letterData.setIsRead(true);
			LetterDataSQL.getInstance().update(letterData);
			
		}
	}

	public void deleteLetter(DataHolder holder, Long id) {
		Data data = holder.getData(id);

		if (data instanceof LetterData) {
			holder.removeData(id);
			LetterDataSQL.getInstance().delete((LetterData) data);
		}

	}
	
	public void deleteData(DataHolder holder, Long id) {
		Data data = holder.getData(id);
		
		if(data instanceof ItemData) {
			MailBoxController.getInstance().deleteItem(holder, data.getId());
			
		} else if (data instanceof LetterData) {
			MailBoxController.getInstance().deleteLetter(holder, data.getId());
		}
	}

	public void purgeLetters(Player player) {
		dataManager.purgeData(dataManager.getDataHolder(player.getUniqueId()), LetterData.class);
	}

	public Boolean recoverItem(Player player, Long id) {
		Boolean success = false;
		
		if(player.getInventory().firstEmpty() != -1) {
			DataHolder pHolder = dataManager.getDataHolder(player.getUniqueId());
			Data data = pHolder.getData(id);
	
			if (data instanceof ItemData) {
				ItemData itemData = (ItemData) data;
				player.getInventory().addItem(itemData.getItem());
				deleteItem(pHolder, id);
				success = true;
	
			}
		}
		
		return success;
	}
	
	public Boolean isOutOfDate(ItemData itemData) {
		LocalDateTime date = itemData.getCreationDate().toLocalDateTime();
		LocalDateTime added = date.plus(itemData.getDuration());
		
		return added.compareTo(LocalDateTime.now()) < 0 ;
	}
	
	public void deleteItem(DataHolder holder, Long id) {
		Data data = holder.getData(id);

		if (data != null && data instanceof ItemData) {
			holder.removeData(id);
			ItemDataSQL.getInstance().delete((ItemData) data);
		}
	}
	
	public void sendItem(String recipient, ItemStack itemstack, Duration d) {
		PlayerInfo playerInfo = PlayerManager.getInstance().getPlayerInfo(recipient);
		
		if (playerInfo != null) {
			Data data = new DataFactory(playerInfo.getUuid(), recipient, "object hard code"); //TODO parametrize author
			ItemData temp = ItemDataSQL.getInstance().create(new ItemData(data, itemstack, d) );
			
			if(temp != null) {
				DataHolder rHolder = getDataManager().getDataHolder(playerInfo.getUuid());
				if(rHolder != null) {
					rHolder.addData(temp);
				}
				
				Player p = Bukkit.getPlayer(playerInfo.getUuid());
				if(p != null) {
					p.sendMessage("Vous avez reçu un objet de la part de " + playerInfo.getName() );
					
				}
			} else {
				//TODO return false
			}
		}
	}

	public DataManager getDataManager() {
		return dataManager;
	}
}
