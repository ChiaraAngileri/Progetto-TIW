package it.polimi.tiw.project.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import it.polimi.tiw.project.beans.User;
import it.polimi.tiw.project.dao.AddressBookDAO;
import it.polimi.tiw.project.utils.ConnectionHandler;

@WebServlet("/GetDestinations")
public class GetDestinations extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Connection connection = null;   
    
    public GetDestinations() {
        super();
    }

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
    	AddressBookDAO addressBookDAO = new AddressBookDAO(connection);
		User user = (User) request.getSession().getAttribute("user");
		String owner = user.getUsername();
		
		List<String> destinations = new ArrayList<>();
		
		//get the names of contacts in the user's address book
		try {
			destinations = addressBookDAO.getDestionations(owner);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Internal error, not possible to get the destinations.");
			return;
		}
		
		String json = new Gson().toJson(destinations);
				
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
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
}
