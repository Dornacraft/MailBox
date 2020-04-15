package fr.dornacraft.mailbox.inventory;

import java.text.SimpleDateFormat;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import fr.dornacraft.devtoolslib.smartinvs.ClickableItem;
import fr.dornacraft.devtoolslib.smartinvs.SmartInventory;
import fr.dornacraft.devtoolslib.smartinvs.content.InventoryContents;
import fr.dornacraft.devtoolslib.smartinvs.content.Pagination;
import fr.dornacraft.mailbox.ItemStackBuilder;
import fr.dornacraft.mailbox.DataManager.Data;
import fr.dornacraft.mailbox.DataManager.ItemData;
import fr.dornacraft.mailbox.DataManager.LetterData;
import fr.dornacraft.mailbox.DataManager.LetterType;
import fr.dornacraft.mailbox.inventory.providers.LetterInventory;

public class MailBoxInventoryHandler {
	
	private static MailBoxInventoryHandler INSTANCE = new MailBoxInventoryHandler();
	
	public Material PAGINATION_MATERIAL = Material.ARROW;
	public Material BORDER_MATERIAL = Material.BLACK_STAINED_GLASS_PANE;
	public Material DELETE_ALL_MATERIAL = Material.BARRIER;
	
	public Long REFRESH_TICKS = 20L;
	
	
	private MailBoxInventoryHandler() {
		
	}

	public static MailBoxInventoryHandler getInstance() {
		return INSTANCE;
	}
	
	public ItemStack generateItemRepresentation(Data data) {
		ItemStack res = null;
		
		if(data instanceof ItemData) {
			res = generateItemDataRepresentation((ItemData) data);
			
		} else if (data instanceof LetterData) {
			res = generateLetterDataRepresentation((LetterData) data);
		}
		
		
		return res;
	}
	
	private ItemStack generateItemDataRepresentation(ItemData data) {
		return data.getItem();
	}
	
	public ClickableItem getNextPageItem(Player player, InventoryContents contents) {
		Pagination pagination = contents.pagination();
		SmartInventory inventory = contents.inventory();
		return ClickableItem.of(new ItemStackBuilder(PAGINATION_MATERIAL).setName("§e§lPage suivante").build(), e -> inventory.open(player, pagination.next().getPage()));
	}
	
	public ClickableItem getPreviousPageItem(Player player, InventoryContents contents) {
		Pagination pagination = contents.pagination();
		SmartInventory inventory = contents.inventory();
		return ClickableItem.of(new ItemStackBuilder(PAGINATION_MATERIAL).setName("§e§lPage précédente").build(), e -> inventory.open(player, pagination.previous().getPage()));
	}
	
	private ItemStack generateLetterDataRepresentation(LetterData data) {
		Material mat = getLetterTypeRepresentation(data.getLetterType() );
		
		SimpleDateFormat sdf =  new SimpleDateFormat("dd/MM/yyyy à HH:mm:ss");
		ItemStackBuilder itemGenerator = new ItemStackBuilder(mat).setName("§r§7" + data.getObject()).setLoreFormat("§r§7")
				.addLore("Expéditeur: " + data.getAuthor()).addLore("date de reception: " + sdf.format(data.getCreationDate()) );
		
		if(!data.getIsRead()) {
			itemGenerator.enchant(Enchantment.ARROW_FIRE, 1).addFlag(ItemFlag.HIDE_ENCHANTS);
		}
		
		return itemGenerator.build();
	}
	
	public Material getLetterTypeRepresentation(LetterType type) {
		Material res = LetterInventory.NO_LETTER_TYPE_MATERIAL;
		
		if(type != null) {
			if(type == LetterType.ANNOUNCE ) {
				res = LetterInventory.ANNOUNCE_LETTER_MATERIAL;
				
			} else if (type == LetterType.SYSTEM) {
				res = LetterInventory.SYSTEM_LETTER_MATERIAL;
				
			} else if (type == LetterType.STANDARD ) {
				res = LetterInventory.STANDARD_LETTER_MATERIAL;
			}
		}
		
		return res;
	}
	
}
