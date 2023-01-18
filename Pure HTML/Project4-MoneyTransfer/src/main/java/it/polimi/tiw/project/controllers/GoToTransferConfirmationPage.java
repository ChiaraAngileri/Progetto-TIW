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

@WebServlet("/GoToTransferConfirmationPage")
public class GoToTransferConfirmationPage extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Connection connection = null;
    private TemplateEngine templateEngine;   
   
    public GoToTransferConfirmationPage() {
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
		
		MoneyTransfer moneyTransfer = null;
		
		//get the last money transfer made
		try {
			moneyTransfer = new MoneyTransferDAO(connection).getLastTransfer();
		} catch(SQLException e){
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failure in database transfer checking");
		}
		
		//get the accounts involved in the last money transfer
		BankAccountDAO bankAccountDAO = new BankAccountDAO(connection);
		BankAccount bankAccountSrc = null;
		BankAccount bankAccountDest = null;
		
		try {
			bankAccountSrc = bankAccountDAO.getBankAccountById(moneyTransfer.getBankAccountSrcId());
			bankAccountDest = bankAccountDAO.getBankAccountById(moneyTransfer.getBankAccountDestId());
		}catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error: not possible to check the confirmation");
			return;
		}		

		//check if the current user is the one who is asking for his own transfer confirmation
		User userSession = (User) request.getSession().getAttribute("user");
		int idUserOriginTransfer = bankAccountSrc.getUserId();
		
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		
		if(idUserOriginTransfer != userSession.getId()) {
			String error = "You do not have permissions to view this page!";
			ctx.setVariable("errorMsg", error);
			
			String path = "/WEB-INF/ErrorPage.html";
			templateEngine.process(path, ctx, response.getWriter());
			return;
		}
		
		ctx.setVariable("srcAcc", bankAccountSrc);
		ctx.setVariable("originalAmountSrc", moneyTransfer.getOrigin_initialAmount());
		ctx.setVariable("destAcc", bankAccountDest);
		ctx.setVariable("originalAmountDest", moneyTransfer.getDestination_initialAmount());
		ctx.setVariable("amount", moneyTransfer.getAmount());
		ctx.setVariable("reason", moneyTransfer.getReason());
		
		String path = "/WEB-INF/TransferConfirmationPage.html";
		templateEngine.process(path, ctx, response.getWriter());		
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
