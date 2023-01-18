package it.polimi.tiw.project.controllers;

import java.io.IOException;
import java.math.BigDecimal;
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
import it.polimi.tiw.project.dao.MoneyTransferDAO;
import it.polimi.tiw.project.utils.ConnectionHandler;

@WebServlet("/MakeTransfer")
public class MakeTransfer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
       
    public MakeTransfer() {
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
		
		//get and parse parameters
		Integer idAccSrc = null;
		Integer idDest = null;
		Integer idAccDest = null;
		String amountString = null;
		String reason = null;
		BigDecimal amount = null;
		
		boolean isBadRequest = false;
		
		try {
			idAccSrc = Integer.parseInt(request.getParameter("idAcc_src"));
			idDest = Integer.parseInt(request.getParameter("ID_Dest"));
			idAccDest = Integer.parseInt(request.getParameter("idAcc_dest"));
			amountString = StringEscapeUtils.escapeJava(request.getParameter("amount"));
			amount =  new BigDecimal(amountString.replace(",","."));
			reason = StringEscapeUtils.escapeJava(request.getParameter("reason"));
			
			isBadRequest = reason.isEmpty() || amountString.isEmpty();
		}catch(NumberFormatException | NullPointerException e) {
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
		
		//check parameters: idAccSrc (the current user is the owner),
		//idDestAcc (the owner is the user with id idDest and the accountId is different from the accountId origin)
		//amount (positive and major than the amount on the account source)
		
		BankAccountDAO bankAccountDAO = new BankAccountDAO(connection);
		User user = (User) request.getSession().getAttribute("user");
		String error = null;

		try {
			//check idAddSrc
			BankAccount bankAccountSrc = bankAccountDAO.getBankAccountById(idAccSrc);
			
			if(bankAccountSrc != null) {
				
				if(bankAccountSrc.getUserId() == user.getId()) {
					
					//check idAccDest
					BankAccount bankAccountDest = bankAccountDAO.getBankAccountById(idAccDest);
					
					if(bankAccountDest != null) {
						
						if(bankAccountDest.getUserId() == idDest) {
							
							if(idAccDest != idAccSrc) {
								
								//check amount
								if(amount.compareTo(BigDecimal.ZERO) == 1) {
									
									if(bankAccountSrc.getBalance().subtract(amount).compareTo(BigDecimal.ZERO) != -1) {
										
										MoneyTransferDAO moneyTransferDAO = new MoneyTransferDAO(connection);
										moneyTransferDAO.makeTransfer(idAccSrc, idAccDest, amount, reason);
										
									}else {
										error = "There aren't enough money on the origin account to do this transfer.";
									}
									
								}else {
									error = "Transfer amount must be greater than 0.";
								}
								
							}else {
								error = "The origin and destination account can't be the same.";
							}
							
						}else {
							error = "The destination account doesn't belong to the selected destination user.";
						}
						
					}else {
						error = "The destination bank account doesn't exist.";
					}
					
				}else {
					error = "User not allowed.";
				}
				
			}else {
				error = "The origin bank account doesn't exist.";
			}
			
		}catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to make a money transfer.");
			return;
		}
		
		//return the user to the right view
		String path = getServletContext().getContextPath();
		
		if(error == null) {
			path = path + "/GoToTransferConfirmationPage";
			
			response.sendRedirect(path);
		}else {
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("idSrcAcc", idAccSrc);
			ctx.setVariable("errorMsg", error);
			path = "/WEB-INF/TransferFailurePage.html";
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
