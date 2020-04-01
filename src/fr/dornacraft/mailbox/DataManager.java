package fr.dornacraft.mailbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DataManager {

	private static Map<UUID, DataHolder> map = new HashMap<>();

	public static DataHolder getDataHolder(UUID uuid) {
		return map.get(uuid);
	}

	public static void addData(UUID uuid, Data data) {
		DataHolder holder = getDataHolder(uuid);

		if (holder == null) {
			holder = map.put(uuid, new DataHolder(new ArrayList<Data>()));
		}

		holder.addData(data);

	}

	public static <T extends Data> List<T> getTypeData(DataHolder dataHolder, Class<T> c) {
		List<T> res = new ArrayList<>();

		for (Data data : dataHolder.getDataList()) {
			if (c.isInstance(data)) {
				res.add(c.cast(data));
			}
		}
		return null;
	}

	public static <T extends Data> void purgeData(DataHolder dataHolder, Class<T> c) {
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