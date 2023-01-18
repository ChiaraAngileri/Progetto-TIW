package it.polimi.tiw.project.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.polimi.tiw.project.beans.User;

public class UserDAO {

	private Connection connection;
	
	public UserDAO(Connection connection) {
		this.connection = connection;
	}
	
	public User checkCredentials(String email, String pwd) throws SQLException {
		String query = "SELECT id, name, surname, username FROM user WHERE email = ? AND password = ?";
		
		try(PreparedStatement pStatement = connection.prepareStatement(query);){
			pStatement.setString(1, email);
			pStatement.setString(2, pwd);
			
			try(ResultSet result = pStatement.executeQuery();){
				if(!result.isBeforeFirst()) {
					//no results, credential check failed
					return null;
				} else {
					result.next();
					User user = new User();
					user.setId(result.getInt("id"));
					user.setName(result.getString("name"));
					user.setSurname(result.getString("surname"));
					user.setUsername(result.getString("username"));
					user.setEmail(email);
					return user;
				}
			}
		}
	}
	
	public User getUserByEmail(String userEmail) throws SQLException {
		User user = null;
		
		String query = "SELECT id, name, surname, username, email FROM user WHERE email = ?";
		
		try(PreparedStatement pStatement = connection.prepareStatement(query);){
			pStatement.setString(1, userEmail);
			
			try(ResultSet result = pStatement.executeQuery();){
				if(result.next()) {
					user = new User();
					user.setId(result.getInt("id"));
					user.setName(result.getString("name"));
					user.setSurname(result.getString("surname"));
					user.setUsername(result.getString("username"));
					user.setEmail(result.getString("email"));
				}
			}
		}
		
		return user;
	}
	
	
	public User getUserByUsername(String userUsername) throws SQLException {
		User user = null;
		
		String query = "SELECT id, name, surname, username, email FROM user WHERE username = ?";
		
		try(PreparedStatement pStatement = connection.prepareStatement(query);){
			pStatement.setString(1, userUsername);
			
			try(ResultSet result = pStatement.executeQuery();){
				if(result.next()) {
					user = new User();
					user.setId(result.getInt("id"));
					user.setName(result.getString("name"));
					user.setSurname(result.getString("surname"));
					user.setUsername(result.getString("username"));
					user.setEmail(result.getString("email"));
				}
			}
		}
		
		return user;
	}
	
	
	public void registerUser(String name, String surname, String usr, String email, String pwd) throws SQLException {		
		String query = "INSERT into user (name, surname, username, email, password) VALUES (?,?,?,?,?)";
		
		try(PreparedStatement pStatement = connection.prepareStatement(query);){
			pStatement.setString(1, name);
			pStatement.setString(2, surname);
			pStatement.setString(3, usr);
			pStatement.setString(4, email);
			pStatement.setString(5, pwd);
			
			pStatement.executeUpdate();			
		}
		
		//adding a default bank account
		User user = getUserByEmail(email);
		BankAccountDAO bankAccountDAO = new BankAccountDAO(connection);
		bankAccountDAO.createBankAccount(user.getId(), "Default account");
	}
	
}
