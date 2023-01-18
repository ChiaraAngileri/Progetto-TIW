package it.polimi.tiw.project.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

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

@WebServlet("/CheckLogin")
public class CheckLogin extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	
    public CheckLogin() {
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
		
		//Get and check parameter from request
		String email = null;
		String password = null;
		boolean isBadrequest = false;
		
		try {
			email = StringEscapeUtils.escapeJava(request.getParameter("email"));
			email.toLowerCase();
			password = StringEscapeUtils.escapeJava(request.getParameter("pwd"));
			
			isBadrequest = email.isEmpty() || password.isEmpty();
		} catch(NullPointerException e) {
			isBadrequest = true;
		}
		
		if(isBadrequest) {
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("errorMsgLogin", "Missing or empty credential value");
			String path = "index.html";
			templateEngine.process(path, ctx, response.getWriter());
			return;
		}
		
		//query db to authenticate for user
		UserDAO userDAO = new UserDAO(connection);
		User user = null;
		
		try {
			user = userDAO.checkCredentials(email, password);
		}catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failure in database credential checking");
			return;
		}
		
		//If the user exists, add info to the session and go to home page
		//otherwise show an error message
		String path;
		if(user == null) {
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("errorMsgLogin", "Incorrect email or password!");
			path = "index.html";
			templateEngine.process(path, ctx, response.getWriter());
		}else {
			request.getSession().setAttribute("user", user);
			path = getServletContext().getContextPath() + "/GoToHomePage";
			response.sendRedirect(path);
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
