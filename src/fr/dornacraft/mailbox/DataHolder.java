package fr.dornacraft.mailbox;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DataHolder {

	private List<Data> listData = new ArrayList<>();
	
	public DataHolder(List<Data> dataList) {
		this.setDataList(dataList);
	}

	public List<Data> getDataList() {
		return listData;
	}

	private void setDataList(List<Data> dataList) {
		listData = dataList;
	}

	public Data getData(Long id) {
		Data res = null;

		for (Integer index = 0; index < this.getDataList().size() && res == null; index++) {
			Data data = this.getDataList().get(index);
			if (data.getId().equals(id)) {
				res = data;
			}
		}
		return res;
	}

	public void addData(Data data) {
		this.getDataList().add(data);
	}

	public void removeData(Long id) {
		Iterator<Data> it = this.getDataList().iterator();
		Boolean stop = false;
		
		while(it.hasNext() && !stop) {
			Data data = it.next();
			
			if(data.getId().equals(id)) {
				stop = true;
				it.remove();
			}
			
		}
	}

}
