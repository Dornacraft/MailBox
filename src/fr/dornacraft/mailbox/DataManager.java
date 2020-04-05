package fr.dornacraft.mailbox;

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

	public DataHolder getDataHolder(UUID uuid) {
		return map.get(uuid);
	}
	
	public void putHolder(UUID uuid, DataHolder holder) {//ajout par rapport au diagrame de class
		map.put(uuid, holder);
	}
	
	public void remove(UUID uuid) {//ajout par rapport au diagrame de class
		this.map.remove(uuid);
	}

	public void addData(UUID uuid, Data data) { //ajout par rapport au diagrame de class
		DataHolder holder = getDataHolder(uuid);

		if (holder == null) {
			putHolder(uuid, new DataHolder(new ArrayList<>()));
			map.put(uuid, new DataHolder(new ArrayList<Data>()));
			holder = getDataHolder(uuid);
		}

		holder.addData(data);

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
				// dataHolder.removeData(data.getId());
			}

		}
	}

}
