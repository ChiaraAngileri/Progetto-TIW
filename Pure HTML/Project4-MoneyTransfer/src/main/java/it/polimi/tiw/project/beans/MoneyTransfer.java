package it.polimi.tiw.project.beans;

import java.math.BigDecimal;
import java.util.Date;

public class MoneyTransfer {
    
    private int id;
    private Date date;
    private int bankAccountSrcId;
    private int bankAccountDestId;
    private BigDecimal amount;
    private String reason;
    private BigDecimal origin_initialAmount;
    private BigDecimal destination_initialAmount;

    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getBankAccountSrcId() {
        return bankAccountSrcId;
    }

    public void setBankAccountSrcId(int bankAccountSrcId) {
        this.bankAccountSrcId = bankAccountSrcId;
    }

    public int getBankAccountDestId() {
        return bankAccountDestId;
    }

    public void setBankAccountDestId(int bankAccountDestId) {
        this.bankAccountDestId = bankAccountDestId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public BigDecimal getOrigin_initialAmount() {
    	return origin_initialAmount;
    }
    
    public void setOrigin_initialAmount(BigDecimal origin_initialAmount) {
    	this.origin_initialAmount = origin_initialAmount;
    }
    
    public BigDecimal getDestination_initialAmount() {
    	return destination_initialAmount;
    }
    
    public void setDestination_initialAmount(BigDecimal destination_initialAmount) {
    	this.destination_initialAmount = destination_initialAmount;
    }
}

