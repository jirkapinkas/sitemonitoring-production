package net.sf.sitemonitoring.service.check.util;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

public class BasicAuthenticationFilter implements Filter {
    private String username = "admin";
    private String password = "admin";
    private String realm = "Protected";

    public BasicAuthenticationFilter() {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            StringTokenizer st = new StringTokenizer(authHeader);
            if (st.hasMoreTokens()) {
                String basic = st.nextToken();
                if (basic.equalsIgnoreCase("Basic")) {
                    try {
                        String credentials = new String(DatatypeConverter.parseBase64Binary(st.nextToken()), StandardCharsets.UTF_8);
                        int p = credentials.indexOf(":");
                        if (p != -1) {
                            String _username = credentials.substring(0, p).trim();
                            String _password = credentials.substring(p + 1).trim();
                            if (!this.username.equals(_username) || !this.password.equals(_password)) {
                                this.unauthorized(response, "Bad credentials");
                            }

                            filterChain.doFilter(servletRequest, servletResponse);
                        } else {
                            this.unauthorized(response, "Invalid authentication token");
                        }
                    } catch (UnsupportedEncodingException var13) {
                        throw new Error("Couldn't retrieve authentication", var13);
                    }
                }
            }
        } else {
            this.unauthorized(response);
        }

    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setHeader("WWW-Authenticate", "Basic realm=\"" + this.realm + "\"");
        response.sendError(401);
    }

    private void unauthorized(HttpServletResponse response) throws IOException {
        this.unauthorized(response, "Unauthorized");
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void destroy() {
    }
}
