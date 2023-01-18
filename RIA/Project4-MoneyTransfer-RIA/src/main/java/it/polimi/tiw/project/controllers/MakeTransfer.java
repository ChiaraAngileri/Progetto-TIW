package it.polimi.tiw.project.controllers;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import com.google.gson.Gson;

import it.polimi.tiw.project.beans.BankAccount;
import it.polimi.tiw.project.beans.MoneyTransfer;
import it.polimi.tiw.project.beans.User;
import it.polimi.tiw.project.dao.BankAccountDAO;
import it.polimi.tiw.project.dao.MoneyTransferDAO;
import it.polimi.tiw.project.dao.UserDAO;
import it.polimi.tiw.project.utils.ConnectionHandler;
import it.polimi.tiw.project.utils.SuccessTransfer;

@WebServlet("/MakeTransfer")
@MultipartConfig
public class MakeTransfer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
    public MakeTransfer() {
        super();
    }
    
    public void init() throws ServletException {
    	connection = ConnectionHandler.getConnection(getServletContext());
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//get and parse parameters
		Integer idAccSrc = null;
		String nameUserDest = null;
		Integer idAccDest = null;
		String amountString = null;
		BigDecimal amount = null;
		String reason = null;
		boolean isBadRequest = false;
		
		BankAccount bankAccountSrc = null;
		BankAccount bankAccountDest = null;
		
		try {
			idAccSrc = Integer.parseInt(request.getParameter("idAcc_src"));
			nameUserDest = StringEscapeUtils.escapeJava(request.getParameter("userDest"));
			idAccDest = Integer.parseInt(request.getParameter("idAcc_dest"));
			amountString = reason = StringEscapeUtils.escapeJava(request.getParameter("amount"));
			amount =  new BigDecimal(amountString.replace(",","."));
			reason = StringEscapeUtils.escapeJava(request.getParameter("reason"));
			
			isBadRequest = reason.isEmpty() || nameUserDest.isEmpty() || amountString.isEmpty();
		}catch(NumberFormatException | NullPointerException e) {
			isBadRequest = true;
			e.printStackTrace();
		}
		
		if(isBadRequest) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect or missing param values");
			return;
		}
		
		//check parameters: idAccSrc (the current user is the owner),
		//idDestAcc (the owner is the user with user name nameAccDest and the accountId destination is different from the accountId origin)
		//amount (positive and major than the amount on the account source)
		
		BankAccountDAO bankAccountDAO = new BankAccountDAO(connection);
		User user = (User) request.getSession().getAttribute("user");
		String error = null;
		Integer newTransferID = null;
		
		try {
			//check idAddSrc
			bankAccountSrc = bankAccountDAO.getBankAccountById(idAccSrc);
			
			if(bankAccountSrc != null) {
				
				if(bankAccountSrc.getUserId() == user.getId()) {
					
					//check idAccDest
					bankAccountDest = bankAccountDAO.getBankAccountById(idAccDest);
					
					if(bankAccountDest != null) {
						
						User userDest = new UserDAO(connection).getUserByUsername(nameUserDest);
						if(userDest != null) {
							
							int idUserDest = userDest.getId();
							if(bankAccountDest.getUserId() == idUserDest) {
								
								if(idAccDest != idAccSrc) {
									
									//check amount
									if(amount.compareTo(BigDecimal.ZERO) == 1) {
										
										if(bankAccountSrc.getBalance().subtract(amount).compareTo(BigDecimal.ZERO) != -1) {
											
											MoneyTransferDAO moneyTransferDAO = new MoneyTransferDAO(connection);
											newTransferID = moneyTransferDAO.makeTransfer(idAccSrc, idAccDest, amount, reason);
											
										}else {
											error = "There aren't enough money on the origin account to do this transfer.";
										}
										
									}else {
										error = "Transfer amount must be greater than 0.";
									}
									
								}else {
									error = "The origin and destination account can't be the same.";
								}
								
							}else {
								error = "The destination account doesn't belong to the selected destination user.";
							}
							
						}else {
							error = "The selected user doesn't exists.";
						}
						
					} else {
						error = "The destination bank account doesn't exist.";
					}
					
				}else {
					error = "User not allowed.";
				}
				
			}else {
				error = "The origin bank account doesn't exist.";
			}
			
		}catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Internal error, not possible to make a money transfer.");
			return;
		}
		
		//return the user to the right view
		if(error == null) {
			MoneyTransfer t = null;	
			BankAccount origin = null;
			BankAccount destination = null;
			
			try {
				t = new MoneyTransferDAO(connection).getMoneyTransferByID(newTransferID);
				origin = bankAccountDAO.getBankAccountById(idAccSrc);
				destination = bankAccountDAO.getBankAccountById(idAccDest);
			} catch (SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Internal error, not possible to make a money transfer.");
				return;
			}
			
			response.setStatus(HttpServletResponse.SC_OK);
			
			String json = new Gson().toJson(new SuccessTransfer(origin, destination, t));
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(json);			
		}else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST); 
			response.getWriter().println(error);
		}		
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
}
