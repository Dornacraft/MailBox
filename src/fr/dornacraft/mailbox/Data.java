package fr.dornacraft.mailbox;

import java.sql.Timestamp;
import java.util.UUID;

public abstract class Data {

	private Long id;
	private String author;
	private String object;
	private Timestamp creationDate;
	private UUID uuid;
	
	public Data(Long id, UUID uuid, String author, String object, Timestamp creationDate) {
		this.setId(id);
		this.setUuid(uuid);
		this.setAuthor(author);
		this.setObject(object);
		this.setCreationDate(creationDate);
	}
	
	public Data(UUID uuid, String author, String object) {
		this(null, uuid, author, object, null);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAuthor() {
		return author;
	}

	private void setAuthor(String author) {
		this.author = author;
	}

	public String getObject() {
		return object;
	}

	private void setObject(String object) {
		this.object = object;
	}

	public Timestamp getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Timestamp creationDate) {
		this.creationDate = creationDate;
	}

	public UUID getUuid() {
		return uuid;
	}

	private void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

}
