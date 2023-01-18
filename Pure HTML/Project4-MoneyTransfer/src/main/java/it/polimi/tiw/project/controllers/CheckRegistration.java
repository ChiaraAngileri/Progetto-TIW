package it.polimi.tiw.project.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.project.beans.User;
import it.polimi.tiw.project.dao.UserDAO;
import it.polimi.tiw.project.utils.ConnectionHandler;


@WebServlet("/CheckRegistration")
public class CheckRegistration extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Connection connection = null;
    private TemplateEngine templateEngine;
    
    public CheckRegistration() {
        super();
    }

    public void init() throws ServletException {
    	connection = ConnectionHandler.getConnection(getServletContext());
    	
    	ServletContext servletContext = getServletContext();
    	ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
    	templateResolver.setTemplateMode(TemplateMode.HTML);
    	this.templateEngine = new TemplateEngine();
    	this.templateEngine.setTemplateResolver(templateResolver);
    	templateResolver.setSuffix(".html");
    }
	
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//get and parse parameter from request
		String name = null;
		String surname = null;
		String email = null;
		String username = null;
		String password = null;
		String repeatedPassword = null;
		boolean isBadRequest = false;
		
		try {
			name = StringEscapeUtils.escapeJava(request.getParameter("name"));
			surname = StringEscapeUtils.escapeJava(request.getParameter("surname"));
			email = StringEscapeUtils.escapeJava(request.getParameter("email"));
			username = StringEscapeUtils.escapeJava(request.getParameter("username"));
			password = StringEscapeUtils.escapeJava(request.getParameter("pwd"));
			repeatedPassword = StringEscapeUtils.escapeJava(request.getParameter("repeatPassword"));
	
			if(name.isEmpty() || surname.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || repeatedPassword.isEmpty()) {
				isBadRequest = true;
			}
		}catch(NullPointerException e) {
			isBadRequest = true;
			e.printStackTrace();
		}
		
		if(isBadRequest) {
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("errorMsgReg", "Missing param value");
			String path = "index.html";
			templateEngine.process(path, ctx, response.getWriter());
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
			        	if(password.equals(request.getParameter("repeatPassword"))) {
			        		
			        		//register user
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
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to register user");
			return;
		}
		
		//return user to the right view
		
		if(error == null) {
			String loginPath = request.getServletContext().getContextPath() + "/index.html";
			response.sendRedirect(loginPath);
			return;
		}else {
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			String path = "index.html";
			ctx.setVariable("errorMsgReg", error);
			templateEngine.process(path, ctx, response.getWriter());
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
