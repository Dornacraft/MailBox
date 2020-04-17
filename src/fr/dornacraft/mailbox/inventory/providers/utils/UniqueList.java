package fr.dornacraft.mailbox.inventory.providers.utils;

import java.util.ArrayList;
import java.util.List;

public abstract class UniqueList<T> {
	
	private List<T> list = new ArrayList<>();
	
	public void addOnce(T obj) {
		if(!this.getList().contains(obj)) {
			this.getList().add(obj);
		}
	}
	
	public void remove(T obj) {
		this.getList().remove(obj);
	}
	
	public void addAllOnce(List<T> piList) {
		for(T pi : piList) {
			this.addOnce(pi);
		}
	}

	public List<T> getList() {
		return this.list;
	}

	public void setList(List<T> newList) {
		this.list = newList;
	}
	
	
}
