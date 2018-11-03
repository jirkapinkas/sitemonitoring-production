package net.sf.sitemonitoring.service.check.util;

import org.springframework.http.MediaType;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class PagingServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        PrintWriter out = response.getWriter();
        out.write("<html>");
        if (request.getParameter("id") == null) {
            out.write("<a href='?id=1'>next</a>");
        } else {
            int id = Integer.parseInt(request.getParameter("id"));
            ++id;
            if (id < 10) {
                out.write("<a href='?id=" + id + "'>next</a>");
            } else {
                out.write("<a href='not-found.html'>not found at the end</a>");
            }
        }

        out.write("</html>");
    }
}
