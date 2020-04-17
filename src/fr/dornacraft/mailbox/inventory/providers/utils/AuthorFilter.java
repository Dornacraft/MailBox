package fr.dornacraft.mailbox.inventory.providers.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fr.dornacraft.mailbox.playerManager.PlayerInfo;
import fr.dornacraft.mailbox.playerManager.PlayerManager;

public class AuthorFilter extends UniqueList<PlayerInfo> {
	
	private Boolean offline = false;
	private Boolean online = false;
	private List<String> optionalNames = new ArrayList<>();
	
	public Boolean addIdentifier(String str) {
		Boolean res = true;
		String identifier = str.replace(" ", "");

		if (identifier.equals("#offline")) {
			this.addAllOnce(PlayerManager.getInstance().getOfflinePlayers());
			this.setOffline(true);
			
		} else if (identifier.equalsIgnoreCase("#online")) {
			this.addAllOnce(PlayerManager.getInstance().getOnlinePlayers());
			this.setOnline(true);

		} else if (identifier.equals("#all")) {
			this.addAllOnce(PlayerManager.getInstance().getCacheView());
			this.setOnline(res);
			this.setOffline(res);

		} else {
			UUID pUuid = PlayerManager.getInstance().getUUID(identifier);

			if (pUuid != null) {
				this.addOnce(new PlayerInfo(identifier, pUuid));
				
			} else {
				res = false;
			}
		}
		
		return res;
	}
	
	public void clear() {
		this.getOptionalNames().clear();
		this.setOffline(false);
		this.setOnline(false);
		super.getList().clear();
	}
	
	@Override
	public void addOnce(PlayerInfo pi) {
		if(!this.getOptionalNames().contains(pi.getName()) && !super.getList().contains(pi)) {
			this.getOptionalNames().add(pi.getName());
			
		}
		super.addOnce(pi);
	}
	
	@Override
	public void remove(PlayerInfo pi) {
		this.getOptionalNames().remove(pi.getName());
		super.remove(pi);
	}
	
	/**
	 * Tente de recuperer les information des joueurs a partir d'une liste
	 * d'identifiers
	 * 
	 * le premier index du resltat peut etre incomplet
	 * 
	 * @param str liste d'identifiers : "#online, #offline, #all, String"
	 * @return
	 */
	public String addAllIdentifiers(List<String> list) {
		String res = null;

		if (!list.isEmpty()) {
			for (Integer index = 0; index < list.size() && res == null; index++) {
				String identifier = list.get(index);
				
				if(!this.addIdentifier(identifier) ) {
					res = identifier;
					break;
				}
				
			}
		}

		return res;
	}
	
	public String getPreview() {
		StringBuilder sb = new StringBuilder();
		
		if(this.getList().size() > 3) {
			if(this.getOffline() && this.getOnline()) {
				sb.append("Tout les joueurs du server ");
				
			} else if (this.getOnline() ) {
				sb.append("Tout les joueurs en ligne ");
				
			} else if (this.getOffline()) {
				sb.append("Tout les joueurs hors ligne ");
			}
		}
		
		if(!sb.toString().isEmpty() ) {
			sb.append("et ");
		}
		
		if(!this.getOptionalNames().isEmpty() ) {
			Integer maxIndex = this.getOptionalNames().size() > 3 ? 3 : this.getOptionalNames().size();
			
			for(Integer index = 0; index < maxIndex; index ++) {
				String toAddName = this.getOptionalNames().get(index);
				sb.append(String.format("%s ", toAddName));
				
			}
		}
		
		if(sb.toString().isEmpty() ) {
			sb.append("Aucun joueur");
		}
		
		return sb.toString();
	}

	public Boolean getOffline() {
		return offline;
	}

	private void setOffline(Boolean offline) {
		this.offline = offline;
	}

	public Boolean getOnline() {
		return online;
	}

	private void setOnline(Boolean online) {
		this.online = online;
	}

	public List<String> getOptionalNames() {
		return optionalNames;
	}

	public void setOptionalNames(List<String> optionalNames) {
		this.optionalNames = optionalNames;
	}
	
}
