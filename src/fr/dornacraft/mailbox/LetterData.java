package fr.dornacraft.mailbox;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LetterData extends Data {

	private List<String> content = new ArrayList<>();
	private LetterType letterType;
	private Boolean isRead;

	public LetterData(UUID uuid, String author, String object, LetterType letterType, List<String> content, Boolean isRead) {
		super(uuid, author, object);
		this.setLetterType(letterType);
		this.setContent(content);
		this.setIsRead(isRead);
		
	}
	
	public LetterData(Data data, LetterType type, List<String> content, Boolean isRead) {
		super(data.getId(), data.getUuid(), data.getAuthor(), data.getObject(), data.getCreationDate());
		this.setCreationDate(data.getCreationDate());
		this.setLetterType(type);
		this.setContent(content);
		this.setIsRead(isRead);
		
	}

	public List<String> getContent() {
		return content;
	}

	private void setContent(List<String> content) {
		this.content = content;
	}

	public LetterType getLetterType() {
		return letterType;
	}

	private void setLetterType(LetterType letterType) {
		this.letterType = letterType;
	}

	public Boolean getIsRead() {
		return isRead;
	}

	public void setIsRead(Boolean isRead) {
		this.isRead = isRead;
	}
	
	public LetterData clone() {
		LetterData res =  new LetterData(this.getUuid(), this.getAuthor(), this.getObject(), this.getLetterType(), this.getContent(), this.getIsRead());
		res.setId(this.getId());
		return res;
	}

}
