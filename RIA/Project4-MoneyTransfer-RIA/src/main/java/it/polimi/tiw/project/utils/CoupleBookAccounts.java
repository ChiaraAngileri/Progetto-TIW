package it.polimi.tiw.project.utils;

import java.util.List;
import java.util.Map;

import it.polimi.tiw.project.beans.AddressBook;

public class CoupleBookAccounts {
	
	private AddressBook addressBook;
	private Map<String, List<Integer>> contactToAccounts;
	
	public CoupleBookAccounts(AddressBook addBook, Map<String, List<Integer>> map) {
		this.addressBook = addBook;
		this.contactToAccounts = map;
	}
	
	public AddressBook getAddressBook() {
		return addressBook;
	}
	
	public Map<String, List<Integer>> getMap(){
		return contactToAccounts;
	}

}
