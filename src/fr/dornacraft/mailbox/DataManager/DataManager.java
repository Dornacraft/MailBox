package fr.dornacraft.mailbox.DataManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DataManager {
	
	public DataManager() {
		
	}
	
	private Map<UUID, DataHolder> map = new HashMap<>();
	
	/**
	 * 
	 * @param uuid UUID du joueur concerné
	 * @return le DataHolder en cache du joueur, peut etre null.
	 */
	public DataHolder getDataHolder(UUID uuid) {
		return map.get(uuid);
	}
	
	public Map<UUID, DataHolder> getCache() {//ajout par rapport au diagrame de class
		return map;
	}
	
	public <T extends Data> List<T> getTypeData(DataHolder dataHolder, Class<T> c) {
		List<T> res = new ArrayList<>();

		for (Data data : dataHolder.getDataList()) {
			if (c.isInstance(data)) {
				res.add(c.cast(data));
			}
		}
		return res;
	}

	public <T extends Data> void purgeData(DataHolder dataHolder, Class<T> c) {
		Iterator<Data> it = dataHolder.getDataList().iterator();

		while (it.hasNext()) {
			Data data = it.next();

			if (c.isInstance(data)) {
				it.remove();
			}

		}
	}
	
	public List<Data> sort(List<Data> list) {
		List<Data> res = new ArrayList<>();
		
		
		
		return res;
	}

}
