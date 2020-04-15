package fr.dornacraft.mailbox.DataManager.filters;

public abstract class FilterTransformer<I, O> {
	public abstract O execute(I obj);
}
