package it.polimi.tiw.project.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoggedChecked implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		
		System.out.print("Logged filter executing...\n");
		HttpServletRequest req =(HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		
		String loginPath = req.getServletContext().getContextPath() + "/index.html";
		
		HttpSession session = req.getSession();
		
		//check if no user is logged
		if(session.isNew() || session.getAttribute("user") == null) {
			res.setStatus(403);
			res.setHeader("Location", loginPath);
			System.out.print("Not logged!\n");
			res.sendRedirect(loginPath);
			return;
		}
		
		//pass the request along the filter chain
		chain.doFilter(request, response);		
	}
	
}
