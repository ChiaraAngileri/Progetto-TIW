package it.polimi.tiw.project.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import it.polimi.tiw.project.beans.BankAccount;
import it.polimi.tiw.project.beans.MoneyTransfer;
import it.polimi.tiw.project.beans.User;
import it.polimi.tiw.project.dao.BankAccountDAO;
import it.polimi.tiw.project.dao.MoneyTransferDAO;
import it.polimi.tiw.project.utils.ConnectionHandler;
import it.polimi.tiw.project.utils.CoupleAccountTransfer;

@WebServlet("/GetBankAccountDetailsData")
@MultipartConfig
public class GetBankAccountDetailsData extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
    public GetBankAccountDetailsData() {
        super();
    }
    
    public void init() throws ServletException {
    	connection = ConnectionHandler.getConnection(getServletContext());
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//get and check parameters
		Integer bankAccountId = null;
		
		try {
			bankAccountId = Integer.parseInt(request.getParameter("bankAccountID"));		
		}catch(NumberFormatException | NullPointerException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect param values");
			return;
		}
		
		//if a bank account with that ID exists for the current user, obtains the details
		User user = (User) request.getSession().getAttribute("user");
		BankAccountDAO bankAccountDAO = new BankAccountDAO(connection);
		BankAccount bankAccount = null;
		ArrayList<MoneyTransfer> moneyTransfers = new ArrayList<>();
		
		try {
			bankAccount = bankAccountDAO.getBankAccountById(bankAccountId);
			
			if(bankAccount == null) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				response.getWriter().println("Resource not found.");
				return;
			}
			
			if(bankAccount.getUserId() != user.getId()) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().println("User not allowed.");
				return;
			}
			
			MoneyTransferDAO moneyTransferDAO = new MoneyTransferDAO(connection);			
			moneyTransfers = moneyTransferDAO.findMoneyTransferByAccountId(bankAccountId);
			
		}catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Internal error, not possible to recover bank account");
			return;
		}
		
		//Redirect to the home page and add bankAccount and moneyTransfers to the parameters
		String json = new Gson().toJson(new CoupleAccountTransfer(bankAccount, moneyTransfers));
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
