package it.polimi.tiw.project.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.polimi.tiw.project.beans.BankAccount;
import it.polimi.tiw.project.beans.User;
import it.polimi.tiw.project.dao.BankAccountDAO;
import it.polimi.tiw.project.utils.ConnectionHandler;

import com.google.gson.*;

@WebServlet("/GetAccountsData")
public class GetAccountsData extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
    public GetAccountsData() {
        super();
    }

    public void init() throws ServletException {
    	connection = ConnectionHandler.getConnection(getServletContext());
    }
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		User user = (User) request.getSession().getAttribute("user");
		BankAccountDAO bankAccountDAO = new BankAccountDAO(connection);
		ArrayList<BankAccount> bankAccounts = new ArrayList<>();
		
		try {
			bankAccounts = bankAccountDAO.findBankAccountsByUser(user.getId());
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Internal error, not possible to recover bank accounts.");
			return;
		}
		
		//Redirect to the home page and add bank accounts to the parameters
		String json = new Gson().toJson(bankAccounts);
		
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
