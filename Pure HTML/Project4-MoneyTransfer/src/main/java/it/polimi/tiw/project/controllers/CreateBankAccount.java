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

import it.polimi.tiw.project.beans.BankAccount;
import it.polimi.tiw.project.beans.User;
import it.polimi.tiw.project.dao.BankAccountDAO;
import it.polimi.tiw.project.utils.ConnectionHandler;


@WebServlet("/CreateBankAccount")
public class CreateBankAccount extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
	private TemplateEngine templateEngine;
       
    
    public CreateBankAccount() {
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
		String nameAccount = null;
		boolean isBadRequest = false;
		
		try {
			nameAccount = StringEscapeUtils.escapeJava(request.getParameter("nameAccount"));
			isBadRequest = nameAccount.isEmpty();
			
		}catch(NullPointerException e) {
			isBadRequest = true;
			e.printStackTrace();
		}
		
		if(isBadRequest) {
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("errorMsg", "Incorrect or missing param values");
			String path = "/WEB-INF/ErrorPage.html";
			templateEngine.process(path, ctx, response.getWriter());
			return;
		}
		
		//check if a user's account with this name already exists 
		User user = (User)request.getSession().getAttribute("user");
		
		BankAccountDAO bankAccountDAO = new BankAccountDAO(connection);
		BankAccount bankAccount = null;
		
		try {
			bankAccount = bankAccountDAO.getBankAccountByName(user.getId(), nameAccount);
			
			if(bankAccount != null) {
				ServletContext servletContext = getServletContext();
				final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
				ctx.setVariable("errorMsg", "You already have an account with this name");
				String path = "/WEB-INF/ErrorPage.html";
				templateEngine.process(path, ctx, response.getWriter());
				return;
			}else {
				
				//create bank account
				bankAccountDAO.createBankAccount(user.getId(), nameAccount);
			}
		}catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to create account");
			return;
		}	
		
		String path = getServletContext().getContextPath() + "/GoToHomePage";
		response.sendRedirect(path);
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}

}
