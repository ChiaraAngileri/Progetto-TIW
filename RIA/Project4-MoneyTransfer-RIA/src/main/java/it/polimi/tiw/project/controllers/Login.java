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

import it.polimi.tiw.project.beans.User;
import it.polimi.tiw.project.dao.UserDAO;
import it.polimi.tiw.project.utils.ConnectionHandler;

@WebServlet("/Login")
@MultipartConfig
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	
    public Login() {
        super();
    }
   
    public void init() throws ServletException {
    	connection = ConnectionHandler.getConnection(getServletContext());
    }
    
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//obtain and escape parameters 
		String email = null;
		String password = null;
		
		email = StringEscapeUtils.escapeJava(request.getParameter("email"));
		password = StringEscapeUtils.escapeJava(request.getParameter("pwd"));
		
		if(email == null || email.isEmpty() || password == null || password.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Credentials must be not null");
			return;
		}
		
		//query db to authenticate for user
		UserDAO userDAO = new UserDAO(connection);
		User user = null;
		
		try {
			user = userDAO.checkCredentials(email, password);
		}catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Internal server error, retry later");
			return;
		}
		
		//If the user exists, add info to the session and go to home page
		//otherwise return an error status code and message
		if(user == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); 
			response.getWriter().println("Incorrect credentials");  
		}else {
			request.getSession().setAttribute("user", user);
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/java");
			response.setCharacterEncoding("UTF-8");
			String username = user.getUsername();
			response.getWriter().println(username);
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
