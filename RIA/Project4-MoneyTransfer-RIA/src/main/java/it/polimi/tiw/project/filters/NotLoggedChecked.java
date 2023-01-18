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

import it.polimi.tiw.project.beans.User;

public class NotLoggedChecked implements Filter {
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		System.out.print("Not logged filter executing...\n");
		HttpServletRequest req =(HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		
		String homePath = req.getServletContext().getContextPath() + "/Home.html";
		
		HttpSession session = req.getSession();
		
		//check if a user is logged
		if(!session.isNew() && session.getAttribute("user") != null) {
			
			
			System.out.println(((User)session.getAttribute("user")).getUsername());
			
			res.setStatus(403);
			res.setHeader("Location", homePath);
			System.out.print("Already logged!\n");
			res.sendRedirect(homePath);
			return;
		}
		
		//pass the request along the filter chain
		chain.doFilter(request, response);
	}
	

}
