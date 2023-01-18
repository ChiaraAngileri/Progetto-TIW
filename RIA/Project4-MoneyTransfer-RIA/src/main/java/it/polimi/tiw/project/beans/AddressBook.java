package it.polimi.tiw.project.beans;

import java.util.ArrayList;
import java.util.List;

public class AddressBook {
	
	private String ownerUser;
	private List<Integer> listContactID = new ArrayList<Integer>();
	
	public String getOwnerUser() {
		return ownerUser;
	}
	
	public void setOwnerUser(String ownerUser) {
		this.ownerUser = ownerUser;
	}
	
	public List<Integer> getContactID() {
		return listContactID;
	}
	
	public void addContactID(int contactID) {
		if(!(this.listContactID.contains(contactID))) {
			this.listContactID.add(contactID);
		}
	}

}
