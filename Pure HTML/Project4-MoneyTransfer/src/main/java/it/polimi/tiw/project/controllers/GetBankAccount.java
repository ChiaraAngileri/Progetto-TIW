package it.polimi.tiw.project.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.project.beans.BankAccount;
import it.polimi.tiw.project.beans.MoneyTransfer;
import it.polimi.tiw.project.beans.User;
import it.polimi.tiw.project.dao.BankAccountDAO;
import it.polimi.tiw.project.dao.MoneyTransferDAO;
import it.polimi.tiw.project.utils.ConnectionHandler;

@WebServlet("/GetBankAccount")
public class GetBankAccount extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
       
    
    public GetBankAccount() {
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
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//get and check parameters
		Integer bankAccountId = null;
		
		try {
			bankAccountId = Integer.parseInt(request.getParameter("bankAccountId"));		
		}catch(NumberFormatException | NullPointerException e) {
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("errorMsg", "Incorrect param values");
			String path = "/WEB-INF/ErrorPage.html";
			templateEngine.process(path, ctx, response.getWriter());
			return;
		}
		
		//if a bank account with that ID exists for the current user, obtains the details
		User user = (User) request.getSession().getAttribute("user");
		BankAccountDAO bankAccountDAO = new BankAccountDAO(connection);
		ArrayList<MoneyTransfer> moneyTransfers = new ArrayList<>();
		BankAccount bankAccount = null;
		String path;
		
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		
		try {
			bankAccount = bankAccountDAO.getBankAccountById(bankAccountId);
			
			if(bankAccount == null) {
				ctx.setVariable("errorMsg", "Bank account not found");
				path = "/WEB-INF/ErrorPage.html";
				templateEngine.process(path, ctx, response.getWriter());
				
				return;
			}
			
			if(bankAccount.getUserId() != user.getId()) {
				ctx.setVariable("errorMsg", "User not allowed");
				path = "/WEB-INF/ErrorPage.html";
				templateEngine.process(path, ctx, response.getWriter());
				
				return;
			}
			
			MoneyTransferDAO moneyTransferDAO = new MoneyTransferDAO(connection);			
			moneyTransfers = moneyTransferDAO.findMoneyTransferByAccountId(bankAccountId);
			
		}catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover bank account");
			return;
		}
		
		//Redirect to account status page
		ctx.setVariable("bankAccount", bankAccount);
		ctx.setVariable("moneyTransfers", moneyTransfers);
		path = "/WEB-INF/AccountStatusPage.html";
		templateEngine.process(path, ctx, response.getWriter());
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
}
