package fr.dornacraft.mailbox;

import java.time.Duration;

import org.bukkit.inventory.ItemStack;

public class ItemData extends Data {

	private Duration duration;
	private ItemStack item;

	public ItemData(String author, String object, ItemStack itemstack, Duration duration) {
		super(author, object);
		this.setItem(itemstack);
		this.setDuration(duration);
	}

	public Duration getDuration() {
		return duration;
	}

	private void setDuration(Duration duration) {
		this.duration = duration;
	}

	public ItemStack getItem() {
		return item;
	}

	private void setItem(ItemStack item) {
		this.item = item;
	}

	public ItemData clone() {
		ItemData res = new ItemData(this.getAuthor(), this.getObject(), this.getItem(), this.getDuration());
		res.setId(this.getId());
		return res;
	}

}
