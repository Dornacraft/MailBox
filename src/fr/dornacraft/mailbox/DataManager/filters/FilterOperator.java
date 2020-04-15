package fr.dornacraft.mailbox.DataManager.filters;

public abstract class FilterOperator<T> {
	
	private String id;
	
	public FilterOperator(String id){
		this.setId(id);
	}
	
	public abstract Boolean checker(T obj);
	
	public Boolean check(T obj) {
		return this.checker(obj);
	}

	public String getId() {
		return id;
	}

	private void setId(String id) {
		this.id = id;
	}
}