package it.polimi.tiw.project.utils;

import it.polimi.tiw.project.beans.BankAccount;
import it.polimi.tiw.project.beans.MoneyTransfer;

public class SuccessTransfer {
	
	BankAccount srcAccount;
	BankAccount destAccount;
	MoneyTransfer transfer;
	
	public SuccessTransfer(BankAccount src, BankAccount dest, MoneyTransfer tr) {
		this.srcAccount = src;
		this.destAccount = dest;
		this.transfer = tr;
	}
	
	public BankAccount getSrcAccount() {
		return srcAccount;
	}
	
	public BankAccount getDestAccount() {
		return destAccount;
	}
	
	public MoneyTransfer getTransfer() {
		return transfer;
	}
	
	

}
