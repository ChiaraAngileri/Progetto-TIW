package it.polimi.tiw.project.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import it.polimi.tiw.project.beans.AddressBook;
import it.polimi.tiw.project.beans.User;
import it.polimi.tiw.project.dao.AddressBookDAO;
import it.polimi.tiw.project.dao.BankAccountDAO;
import it.polimi.tiw.project.dao.UserDAO;
import it.polimi.tiw.project.utils.ConnectionHandler;
import it.polimi.tiw.project.utils.CoupleBookAccounts;

@WebServlet("/GetContacts")
public class GetContacts extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Connection connection = null;   
    
    public GetContacts() {
        super();
    }

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
		User user = (User) request.getSession().getAttribute("user");
		AddressBook addressBook = new AddressBook();
		
		try {
			addressBook = new AddressBookDAO(connection).getAddressBookByOwner(user.getUsername());
		} catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Internal error, not possible to obtain the address book.");
			return;
		}
		

		List<Integer> contactIDs = addressBook.getContactID();

		//obtain the account IDs of the contacts in the user's address book 
		Map<String, List<Integer>> contactToAccounts = new HashMap<>();
		
		for(int contactID : contactIDs) {
			String username;
			List<Integer> accounts;
			try {
				User contact = new UserDAO(connection).getUserById(contactID);
				username = contact.getUsername();
				accounts = new BankAccountDAO(connection).findBankAccountsByUser(contactID).stream().map(x -> x.getId()).toList();
			} catch (SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Internal error, not possible to obtain the address book.");
				return;
			}
			contactToAccounts.put(username, accounts);
		}
		
		CoupleBookAccounts result = new CoupleBookAccounts(addressBook, contactToAccounts);
		
		String json = new Gson().toJson(result);
		
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
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}	
}
