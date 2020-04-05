package fr.dornacraft.mailbox.DataManager;

import org.bukkit.Material;

public enum LetterType {
	
	STANDARD(Material.PAPER),
	SYSTEM(Material.EMPTY_MAP),
	ANNOUNCE(Material.MAP);//TODO séprarer et mettre dans LetterContentProvider
	
	private Material representation;
	
	private LetterType(Material material) {
		setRepresentation(material);
	}

	public Material getRepresentation() {
		return representation;
	}

	private void setRepresentation(Material representation) {
		this.representation = representation;
	}
}
