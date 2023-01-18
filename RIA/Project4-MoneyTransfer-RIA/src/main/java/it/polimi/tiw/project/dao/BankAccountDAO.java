package it.polimi.tiw.project.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;



import it.polimi.tiw.project.beans.BankAccount;

public class BankAccountDAO {
	
	private Connection connection;
	
	public BankAccountDAO(Connection connection) {
		this.connection = connection;
	}
	
	
	public BankAccount getBankAccountById(int bankAccountId) throws SQLException {
		BankAccount bankAccount = null;
		String query = "SELECT * from bank_account WHERE id = ?";
		
		try(PreparedStatement pStatement = connection.prepareStatement(query);){
			pStatement.setInt(1, bankAccountId);
			
			try(ResultSet result = pStatement.executeQuery();){
				if(result.next()) {
					bankAccount = new BankAccount();
					bankAccount.setId(result.getInt("id"));
					bankAccount.setUserId(result.getInt("user_id"));
					bankAccount.setBalance(result.getBigDecimal("balance"));
					bankAccount.setName(result.getString("name"));
				}
			}
		}
		
		return bankAccount;
	}
	
	
	public BankAccount getBankAccountByName(int userId, String accountName) throws SQLException {
		BankAccount bankAccount = null;
		String query = "SELECT * from bank_account WHERE user_id = ? AND name = ?";
		
		try(PreparedStatement pStatement = connection.prepareStatement(query);){
			pStatement.setInt(1, userId);
			pStatement.setString(2, accountName);
			
			try(ResultSet result = pStatement.executeQuery();){
				if(result.next()) {
					bankAccount = new BankAccount();
					bankAccount.setId(result.getInt("id"));
					bankAccount.setUserId(result.getInt("user_id"));
					bankAccount.setBalance(result.getBigDecimal("balance"));
					bankAccount.setName(result.getString("name"));
				}
			}
		}
		
		return bankAccount;
	}
	
	
	public ArrayList<BankAccount> findBankAccountsByUser(int userId) throws SQLException {
		ArrayList<BankAccount> bankAccounts = new ArrayList<>();
		String query = "SELECT * FROM bank_account WHERE user_id = ?";
		
		try(PreparedStatement pStatement = connection.prepareStatement(query);){
			pStatement.setInt(1, userId);
			
			try(ResultSet result = pStatement.executeQuery();){
				while(result.next()) {
					BankAccount bankAccount = new BankAccount();
					bankAccount.setId(result.getInt("id"));
					bankAccount.setUserId(result.getInt("user_id"));
					bankAccount.setBalance(result.getBigDecimal("balance"));
					bankAccount.setName(result.getString("name"));
					
					bankAccounts.add(bankAccount);
				}
			}
		}
		
		return bankAccounts;
	}
	
	
	public int createBankAccount(int userId, String bankAccountName) throws SQLException {
		BigDecimal amount = BigDecimal.ZERO;
		String query = "INSERT into bank_account (balance, user_id, name) VALUES(?, ?, ?)";
		
		try(PreparedStatement pStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);){
			
			pStatement.setBigDecimal(1, amount);
			pStatement.setInt(2, userId);
			pStatement.setString(3, bankAccountName);
			
			pStatement.executeUpdate();
			
			ResultSet generatedKeys = pStatement.getGeneratedKeys();
			if(generatedKeys.next()) {
				return generatedKeys.getInt(1);
			}else {
				throw new SQLException("Creating account failed, no ID obtained.");
			}
		}
	}
	
}
