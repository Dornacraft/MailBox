package fr.dornacraft.mailbox.inventory.builders;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.OfflinePlayer;

public abstract class FilterableInventoryBuilder extends InventoryBuilder {
	
	private List<OfflinePlayer> filterList = new ArrayList<>();
	
	public FilterableInventoryBuilder(String id, String title, Integer rows) {
		super(id, title, rows);
		
	}

	public List<OfflinePlayer> getFilterList() {
		return filterList;
	}

	public void setFilterList(List<OfflinePlayer> filterList) {
		this.filterList = filterList;
	}
	
	public void addAll(List<OfflinePlayer> pList) {
		List<OfflinePlayer> filterList = this.getFilterList();
		
		for(OfflinePlayer offPlayer : pList) {
			if(!filterList.contains(offPlayer)) {
				filterList.add(offPlayer);
			}
		}
	}
	
	public void removeAll(List<OfflinePlayer> pList) {
		List<OfflinePlayer> filterList = this.getFilterList();
		
		for(OfflinePlayer offPlayer : pList) {
			if(filterList.contains(offPlayer)) {
				filterList.remove(offPlayer);
			}
		}
	}

}
