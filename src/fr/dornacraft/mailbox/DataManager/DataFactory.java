package fr.dornacraft.mailbox.DataManager;

import java.sql.Timestamp;
import java.util.UUID;

public class DataFactory extends Data {
	
	public DataFactory(Long id, UUID uuid, String author, String object, Timestamp creationDate) {
		super(id, uuid, author, object, creationDate);
	}
	
	public DataFactory( UUID uuid, String author, String object) {
		super(null, uuid, author, object, null);
	}
	
}
