package fr.dornacraft.mailbox;

import java.util.ArrayList;
import java.util.List;

public class LetterData extends Data {

	private List<String> content = new ArrayList<>();
	private LetterType letterType;
	private Boolean isRead;

	public LetterData(String author, String object, LetterType letterType, List<String> content, Boolean isRead) {
		super(author, object);
		this.setLetterType(letterType);
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
		LetterData res =  new LetterData(this.getAuthor(), this.getObject(), this.getLetterType(), this.getContent(), this.getIsRead());
		res.setId(this.getId());
		return res;
	}

}
