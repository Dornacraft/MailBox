package fr.dornacraft.mailbox;

public class MailBoxController {
	
	private static MailBoxController INSTANCE;
	
	private MailBoxController() {	
	}
	
	public static MailBoxController getInstance() {
		return INSTANCE;
	}
	
	public void initialize() {
		//TODO
	}
	
}
