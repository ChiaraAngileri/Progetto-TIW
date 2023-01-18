package it.polimi.tiw.project.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.project.beans.BankAccount;
import it.polimi.tiw.project.beans.User;
import it.polimi.tiw.project.dao.BankAccountDAO;
import it.polimi.tiw.project.utils.ConnectionHandler;

@WebServlet("/CreateBankAccount")
@MultipartConfig
public class CreateBankAccount extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
    public CreateBankAccount() {
        super();
    }
    
    public void init() throws ServletException {
    	connection = ConnectionHandler.getConnection(getServletContext());
    }
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//Get and check parameter from request
		String nameAccount = null;
		
		try {
			nameAccount = StringEscapeUtils.escapeJava(request.getParameter("nameAccount"));
			
			if(nameAccount.isEmpty()) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Incorrect or missing param values");
				return;
			}
		}catch(NullPointerException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect or missing param values");
			return;
		}
		
		//check if a user's account with this name already exists 
		User user = (User)request.getSession().getAttribute("user");
		
		BankAccountDAO bankAccountDAO = new BankAccountDAO(connection);
		BankAccount bankAccount = null;
		int newBankAccountID;
		
		try {
			bankAccount = bankAccountDAO.getBankAccountByName(user.getId(), nameAccount);
			
			if(bankAccount != null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("You already have an account with this name");
				return;
			}else {
				//create bank account
				newBankAccountID = bankAccountDAO.createBankAccount(user.getId(), nameAccount);
			}
		}catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Internal error, not possibile to create account. Error: " + e.getMessage());
			return;
		}	
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().print(newBankAccountID);
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
}
