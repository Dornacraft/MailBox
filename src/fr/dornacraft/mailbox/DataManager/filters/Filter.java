package fr.dornacraft.mailbox.DataManager.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class Filter<T> implements List<T> {
	
	private List<T> entries = new ArrayList<>();
	private List<FilterOperator<T>> filterOperators = new ArrayList<>();
	
	public Filter() {
		
	}
	
	public Filter(List<T> defaultEntry) {
		this.setEntries(defaultEntry);
	}

	public List<T> getEntries() {
		return entries;
	}

	public void setEntries(List<T> list) {
		this.entries = list;
	}
	
	public void addEntry(T obj) {
		this.getEntries().add(obj);
	}
	
	public void addAllOnce(List<T> list) {
		for(T obj : list) {
			if(!this.getEntries().contains(obj) ) {
				this.addEntry(obj);
			}
		}
	}
	
	public void sort(Comparator<? super T> comparator) {
		this.getEntries().sort(comparator);
	}
	
	public Boolean containsEntry(T obj) {
		return this.getEntries().contains(obj);
	}
	
	public void purgeEntries() {
		this.setEntries(new ArrayList<>());
	}
	
	public T getEntry(Integer index) {
		return this.getEntries().get(index);
	}
	
	@Override
	public Iterator<T> iterator() {
		return this.getEntries().iterator();
	}
	
	public void cleanFilters() {
		this.setFilterOperators(new ArrayList<>());
	}
	
	public Filter<T> applyFilters(){
		Filter<T> res = new Filter<>();

		for(T obj : this.getEntries()) {
			Boolean add = true;
			
			for(FilterOperator<T> operator : this.getFilterOperators() ) {
				if(!operator.check(obj) ) {
					add = false;
					break;
					
				}
			}
			
			if(add) {
				res.add(obj);
			}
		}
		
		return res;
	}
	
	public static <E> Filter<E> filter(List<E> list, FilterOperator<E> filterOperator){
		Filter<E> filter = new Filter<E>(list);
		filter.putFilterOperator(filterOperator);
		return filter.applyFilters();
	}
	
	public static <I, O> Filter<O> transform(Filter<I> filter, FilterTransformer<I, O> transformer){
		Filter<O> res = new Filter<>();
		
		for(I o : filter ) {
			res.addEntry(transformer.execute(o) );
		
		}
		
		return res;
	}

	@Override
	public boolean add(T e) {
		return this.getEntries().add(e);
	}

	@Override
	public void add(int index, T element) {
		this.getEntries().add(index, element);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		return this.getEntries().addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		return this.getEntries().addAll(index, c);
	}

	@Override
	public void clear() {
		this.getEntries().clear();
	}

	@Override
	public boolean contains(Object o) {
		return this.getEntries().contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return this.getEntries().containsAll(c);
	}

	@Override
	public T get(int index) {
		return this.getEntries().get(index);
	}

	@Override
	public int indexOf(Object o) {
		return this.getEntries().indexOf(o);
	}

	@Override
	public boolean isEmpty() {
		return this.getEntries().isEmpty();
	}

	@Override
	public int lastIndexOf(Object o) {
		return this.getEntries().lastIndexOf(o);
	}

	@Override
	public ListIterator<T> listIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return this.getEntries().listIterator(index);
	}

	@Override
	public boolean remove(Object o) {
		return this.getEntries().remove(o);
	}

	@Override
	public T remove(int index) {
		return this.getEntries().remove(index);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return this.getEntries().removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return this.getEntries().retainAll(c);
	}

	@Override
	public T set(int index, T element) {
		return this.getEntries().set(index, element);
	}

	@Override
	public int size() {
		return this.getEntries().size();
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return this.getEntries().subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		return this.getEntries().toArray();
	}

	@Override
	public <E> E[] toArray(E[] a) {
		return this.getEntries().toArray(a);
	}

	public List<FilterOperator<T>> getFilterOperators() {
		return filterOperators;
	}

	public void setFilterOperators(List<FilterOperator<T>> filterOperators) {
		this.filterOperators = filterOperators;
	}
	
	public void putFilterOperator(FilterOperator<T> filterOperator) {
		FilterOperator<T> actual = this.getFilter(filterOperator.getId());
		
		if(actual != null) {
			this.removeFilterOperator(actual);
		}
		
		this.getFilterOperators().add(filterOperator);
	}
	
	public void removeFilterOperator(FilterOperator<T> filterOperator) {
		Iterator<FilterOperator<T>> it = this.getFilterOperators().iterator();
		Boolean action = false;
		
		if(this.containsOperator(filterOperator)) {
			while(it.hasNext() && !action) {
				FilterOperator<T> tempFO = it.next();
				
				if(tempFO.getId().equals(filterOperator.getId()) ){
					it.remove();
					action = true;
				}
			}
		}
		
	}
	
	public FilterOperator<T> getFilter(String id){
		FilterOperator<T> res = null;
		
		for(FilterOperator<T> operator : this.getFilterOperators()) {
			if(operator.getId().equals(id) ){
				res = operator;
				break;
			}
		}
		
		return res;
	}
	
	public Boolean containsOperator(FilterOperator<T> filterOperator) {
		return this.getFilter(filterOperator.getId()) != null;
	}
	
}
