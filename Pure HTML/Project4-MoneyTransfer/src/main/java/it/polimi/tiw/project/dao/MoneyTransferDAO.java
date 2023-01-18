package it.polimi.tiw.project.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import it.polimi.tiw.project.beans.BankAccount;
import it.polimi.tiw.project.beans.MoneyTransfer;

public class MoneyTransferDAO {
	
	private Connection connection;
	
	public MoneyTransferDAO(Connection connection) {
		this.connection = connection;
	}
	
	
	public ArrayList<MoneyTransfer> findMoneyTransferByAccountId(int accountId) throws SQLException {
		ArrayList<MoneyTransfer> moneyTransfers = new ArrayList<>();
		String query = "SELECT * from money_transfer WHERE (bank_account_origin = ? OR bank_account_destination = ?) ORDER BY date DESC";
		
		try(PreparedStatement pStatement = connection.prepareStatement(query);){
			pStatement.setInt(1, accountId);
			pStatement.setInt(2, accountId);
			
			try(ResultSet result = pStatement.executeQuery();){
				while(result.next()) {
					MoneyTransfer moneyTransfer = new MoneyTransfer();
					moneyTransfer.setId(result.getInt("id"));
					
					moneyTransfer.setDate(new Date(result.getTimestamp("date").getTime()));
					moneyTransfer.setAmount(result.getBigDecimal("amount"));
					moneyTransfer.setReason(result.getString("reason"));
					moneyTransfer.setBankAccountSrcId(result.getInt("bank_account_origin"));
					moneyTransfer.setBankAccountDestId(result.getInt("bank_account_destination"));
					moneyTransfer.setOrigin_initialAmount(result.getBigDecimal("origin_initial_amount"));
					moneyTransfer.setDestination_initialAmount(result.getBigDecimal("destination_initial_amount"));
					
					moneyTransfers.add(moneyTransfer);
				}
			}
		}
		
		return moneyTransfers;
	}
	
	
	public void makeTransfer(int bankAccountSourceId, int bankAccountDestinationId,
            BigDecimal amount, String reason) throws SQLException {

		BankAccount bankAccountSource = new BankAccountDAO(connection).getBankAccountById(bankAccountSourceId);
		BankAccount bankAccountDestination = new BankAccountDAO(connection).getBankAccountById(bankAccountDestinationId);
		
		BigDecimal origin_initialAmount = bankAccountSource.getBalance();
		BigDecimal destination_initialAmount = bankAccountDestination.getBalance();
		
		//delimit the transaction explicitly: not to leave the db in inconsistent state
		
		//disable autocommit
		connection.setAutoCommit(false);
				
		String queryCreateTransfer = "INSERT into money_transfer (bank_account_origin, bank_account_destination, amount, reason, origin_initial_amount, destination_initial_amount) VALUES (?, ?, ?, ?, ?, ?)";
		PreparedStatement pStatementCreate = null;
		
		String queryAddMoney = "UPDATE bank_account SET balance = balance + ? WHERE id = ?";
		PreparedStatement pStatementAdd = null;
		
		String queryRemoveMoney = "UPDATE bank_account SET balance = balance - ? WHERE id = ?";
		PreparedStatement pStatementRemove = null;
		
		try {
			
			//remove money from the source account
			pStatementRemove = connection.prepareStatement(queryRemoveMoney);
			pStatementRemove.setBigDecimal(1, amount);
			pStatementRemove.setInt(2, bankAccountSourceId);
			
			pStatementRemove.executeUpdate();    //1st update
			
			//add money to the destination account
			pStatementAdd = connection.prepareStatement(queryAddMoney);
			pStatementAdd.setBigDecimal(1, amount);
			pStatementAdd.setInt(2, bankAccountDestinationId);
			
			pStatementAdd.executeUpdate();    //2nd update
			
			//create money transfer
			pStatementCreate = connection.prepareStatement(queryCreateTransfer);
			pStatementCreate.setInt(1, bankAccountSourceId);
			pStatementCreate.setInt(2, bankAccountDestinationId);
			pStatementCreate.setBigDecimal(3, amount);
			pStatementCreate.setString(4, reason);
			pStatementCreate.setBigDecimal(5, origin_initialAmount);
			pStatementCreate.setBigDecimal(6, destination_initialAmount);
			
			pStatementCreate.executeUpdate();    //3rd update
			
			//commit if everything is ok
			connection.commit();
			
		}catch(SQLException e) {
			connection.rollback();  //if update 1 OR 2 OR 3 fails, roll back all work
			throw e;
		}finally {
			//enable autocommit again
			connection.setAutoCommit(true);
			
			if(pStatementRemove != null) {
				try {
					pStatementRemove.close();
				}catch(Exception e) {
					throw e;
				}
			}
			
			if(pStatementAdd != null) {
				try {
					pStatementAdd.close();
				}catch(Exception e) {
					throw e;
				}
			}
			
			if(pStatementCreate != null) {
				try {
					pStatementCreate.close();
				}catch(Exception e) {
					throw e;
				}
			}
		}
	}
	
	public MoneyTransfer getLastTransfer() throws SQLException {
		MoneyTransfer moneyTransfer = null;
		
		String queryLastTransfer = "SELECT * FROM money_transfer WHERE id = "
												+ "(SELECT max(id) FROM money_transfer)";

		try (PreparedStatement pStatementLastTransfer = connection.prepareStatement(queryLastTransfer)){
			try(ResultSet result = pStatementLastTransfer.executeQuery()){
			
				if(result.next()) {
					moneyTransfer = new MoneyTransfer();
					
					moneyTransfer.setDate(new Date(result.getTimestamp("date").getTime()));
					moneyTransfer.setBankAccountSrcId(result.getInt("bank_account_origin"));
					moneyTransfer.setBankAccountDestId(result.getInt("bank_account_destination"));
					moneyTransfer.setAmount(result.getBigDecimal("amount"));
					moneyTransfer.setReason(result.getString("reason"));
					moneyTransfer.setOrigin_initialAmount(result.getBigDecimal("origin_initial_amount"));
					moneyTransfer.setDestination_initialAmount(result.getBigDecimal("destination_initial_amount"));
				}
			}
		}
		
		return moneyTransfer;
	}
	
}
