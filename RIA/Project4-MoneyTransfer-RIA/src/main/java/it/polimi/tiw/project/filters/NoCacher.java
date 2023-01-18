package it.polimi.tiw.project.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class NoCacher implements Filter {

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
			throws IOException, ServletException {
		
		System.out.print("No cacher filter executing...\n");
		
		HttpServletResponse res = (HttpServletResponse) response;
		
		res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); //HTTP 1.1
		res.setHeader("Pragma", "no-cache");  //HTTP 1.0
		res.setHeader("Expires", "0"); //Proxies
		
		//pass the request along the filter chain
		chain.doFilter(request, response);
		
	}
	
}
