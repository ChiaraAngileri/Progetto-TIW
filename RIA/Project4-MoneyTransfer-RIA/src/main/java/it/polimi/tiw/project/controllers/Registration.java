package it.polimi.tiw.project.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

@WebServlet("/Registration")
@MultipartConfig
public class Registration extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
    public Registration() {
        super();
    }
    
    public void init() throws ServletException {
    	connection = ConnectionHandler.getConnection(getServletContext());
    }
	
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String name = null;
		String surname = null;
		String email = null;
		String username = null;
		String password = null;
		String repeatedPassword = null;
		boolean isBadRequest = false;
		
		//get and parse parameter from request
		try {
			name = StringEscapeUtils.escapeJava(request.getParameter("name"));
			surname = StringEscapeUtils.escapeJava(request.getParameter("surname"));
			username = StringEscapeUtils.escapeJava(request.getParameter("username"));
			email = StringEscapeUtils.escapeJava(request.getParameter("email"));
			password = StringEscapeUtils.escapeJava(request.getParameter("pwd"));
			repeatedPassword = StringEscapeUtils.escapeJava(request.getParameter("repeated_pwd"));
	
			if(name.isEmpty() || surname.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || repeatedPassword.isEmpty()) {
				isBadRequest = true;
			}
			
		}catch(NullPointerException e) {
			isBadRequest = true;
		}
		
		if(isBadRequest) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Credentials must be not null");
			return;
		}
		
		//check parameters: email (syntactic validity and uniqueness), username (uniqueness), 
		//password and repeatPassword(equality)
		UserDAO userDAO = new UserDAO(connection);
		User user = null;
		String error = null;
					
		try {
			
			//check email
			user = userDAO.getUserByEmail(email);
			
			if(user == null) {
				
				Pattern pattern = Pattern.compile("[a-zA-Z0-9.]+@[a-zA-Z0-9]+\\.[a-zA-Z.]+");
				Matcher matcher = pattern.matcher(email);
		        if(!matcher.matches()) {
		        	error = "The email is not syntactically valide.";
		        }else {
		        
			        //check username
			        user = userDAO.getUserByUsername(username);
			        
			        if(user == null) {
			        	
			        	//check passwords
			        	if(password.equals(repeatedPassword)) {
			        		userDAO.registerUser(name, surname, username, email, password);
			        		user = userDAO.getUserByEmail(email);
			        	}else {
			        		error = "Password and repeated password are different.";
			        	}
			        }else{
			        	error = "The username already exists.";
			        }
		        }
			}else {
				error = "The email already exists.";
			}
			
		}catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Internal server error, not possible to register user");
			return;
		}
		
		if(error == null) {
			response.setStatus(HttpServletResponse.SC_OK);
			return;
		} else {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
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
