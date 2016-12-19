package edu.upenn.cis455.servlet;

import edu.upenn.cis455.querying.QueryHandler;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@SuppressWarnings("serial")
public class MainServlet extends HttpServlet {
	
	/* TODO: Implement user interface for XPath engine here */
	
	/* You may want to override one or both of the following methods */

    String tableName;
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        tableName = config.getInitParameter("table");
    }


	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String path = request.getServletPath();
        if (path.equals("/search")) {
            System.out.println(request.getParameter("q"));
            PrintWriter out = response.getWriter();
            String query = request.getParameter("q");
            //QueryHandler qh = new QueryHandler(tableName);
            //List<String> results = qh.query(query);

            out.println("<html><head><title>Test</title></head><body>");
            //for (String s : results){
//                out.println("<p>"+s+"</p>");
//            }
//            out.println("</body></html>");

        }

	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        PrintWriter out = response.getWriter();


        String path = request.getServletPath();
        System.out.println(path);
        if (path.startsWith("/main") || path.equals("")) {
            out.println("<html><head><title>Test</title></head><body>");
            out.println("<form action=\"search\" method=\"post\">");
            out.println("Come on bud hit me with that search bro:<br><input type=\"text\" name=\"q\"><br>");
            out.println("<input type=\"submit\" value=\"Submit\"></form>");
            out.println("</body></html>");
        } else {
            response.sendError(404);
        }



    }

}









