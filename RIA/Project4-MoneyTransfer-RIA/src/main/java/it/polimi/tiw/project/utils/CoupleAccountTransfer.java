package it.polimi.tiw.project.utils;

import java.util.List;

import it.polimi.tiw.project.beans.BankAccount;
import it.polimi.tiw.project.beans.MoneyTransfer;

public class CoupleAccountTransfer {

	private BankAccount bankAccount;
	private List<MoneyTransfer> moneyTransfers;
	
	public CoupleAccountTransfer(BankAccount acc, List<MoneyTransfer> transfers) {
		this.bankAccount = acc;
		this.moneyTransfers = transfers;
	}
	
	public BankAccount getBankAccount() {
		return bankAccount;
	}
	
	public List<MoneyTransfer> getMoneyTransfers(){
		return moneyTransfers;
	}
	
	
	
	
}
