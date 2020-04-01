package fr.dornacraft.mailbox;

import java.sql.Date;

public abstract class Data {

	private Integer id;
	private String author;
	private String object;
	private Date creationDate;
	//TODO rajouter UUID

	public Data(String author, String object) {
		this.setAuthor(author);
		this.setObject(object);
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
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

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

}
