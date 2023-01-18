package it.polimi.tiw.project.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import it.polimi.tiw.project.beans.AddressBook;

public class AddressBookDAO {

	private Connection connection;
	
	public AddressBookDAO(Connection connection) {
		this.connection = connection;
	}
	
	public AddressBook getAddressBookByOwner(String owner) throws SQLException {
		
		AddressBook addressBook = new AddressBook();
		addressBook.setOwnerUser(owner);
		
		String query = "SELECT contact_id"
				+ " from address_book"
				+ " WHERE owner = ?";
		
		try(PreparedStatement pStatement = connection.prepareStatement(query);){
			pStatement.setString(1, owner);
						
			try(ResultSet result = pStatement.executeQuery();){
				
				while(result.next()) {										
					addressBook.addContactID(result.getInt("contact_id"));
				}
			}
		}
		
		return addressBook;		
	}
	
	public void addContact(String owner, int contactID) throws SQLException {
		
		String query = "INSERT into address_book (owner, contact_id) VALUES(?, ?)";
		
		try(PreparedStatement pStatement = connection.prepareStatement(query);){
			pStatement.setString(1, owner);
			pStatement.setInt(2, contactID);
			
			pStatement.executeUpdate();
		}
	}
	
	public boolean existsContact(String owner, int contactID) throws SQLException {
		
		boolean result = false;
		
		String query = "SELECT * from address_book where owner = ? and contact_id = ?";
		
		try(PreparedStatement pStatement = connection.prepareStatement(query);){
			pStatement.setString(1, owner);
			pStatement.setInt(2, contactID);
			
			try(ResultSet resultSet = pStatement.executeQuery();){
				if(resultSet.next()) {
					result = true;
				}
			}
		}
		
		return result;
	}
	
	
	//TODO: check
	
	public ArrayList<String> getDestionations(String owner) throws SQLException {
		
		ArrayList<String> names = new ArrayList<>();
		
		String query = "SELECT U.username from address_book as AB join user as U on AB.contact_id = U.id where AB.owner = ?";
		
		try(PreparedStatement pStatement = connection.prepareStatement(query);){
			pStatement.setString(1, owner);
			
			try(ResultSet resultSet = pStatement.executeQuery();){
				
				while(resultSet.next()) {
					names.add(resultSet.getString("username"));
				}
			}
		}
		
		return names;
	}
	
}
