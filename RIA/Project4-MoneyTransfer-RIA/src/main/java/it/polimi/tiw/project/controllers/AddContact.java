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

import it.polimi.tiw.project.beans.User;
import it.polimi.tiw.project.dao.AddressBookDAO;
import it.polimi.tiw.project.dao.UserDAO;
import it.polimi.tiw.project.utils.ConnectionHandler;

@WebServlet("/AddContact")
@MultipartConfig
public class AddContact extends HttpServlet {
	private static final long serialVersionUID = 1L;
	Connection connection = null;
       
    public AddContact() {
        super();
    }
    
    public void init() throws ServletException {
    	connection = ConnectionHandler.getConnection(getServletContext());
    }
    
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//Get and parse parameters from request
		String owner = null;
		Integer ownerID = null;
		Integer destID = null;
		
		try {
			ownerID = Integer.parseInt(request.getParameter("ownerID"));
			destID = Integer.parseInt(request.getParameter("destID"));
		} catch (NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorret or missing params");
			return;
		}
		
		//check if destID already exists in owner's address book 
		//and if not add the contact
		AddressBookDAO addressBookDAO = new AddressBookDAO(connection);
	
		try {
			
			User user = new UserDAO(connection).getUserById(ownerID);
			
			if(user != null) {
				owner = user.getUsername();
			}
			
			if(!addressBookDAO.existsContact(owner, destID)) {
				addressBookDAO.addContact(owner, destID);
			}
			
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Internal error, not possible to obtain the address book.");
		}
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
}
