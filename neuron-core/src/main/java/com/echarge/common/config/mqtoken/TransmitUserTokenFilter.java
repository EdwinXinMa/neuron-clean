/*
 * 
 * Could not load the following classes:
 *  jakarta.servlet.Filter
 *  jakarta.servlet.FilterChain
 *  jakarta.servlet.FilterConfig
 *  jakarta.servlet.ServletException
 *  jakarta.servlet.ServletRequest
 *  jakarta.servlet.ServletResponse
 *  jakarta.servlet.http.HttpServletRequest
 */
package com.echarge.common.config.mqtoken;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import com.echarge.common.config.mqtoken.UserTokenContext;

public class TransmitUserTokenFilter
implements Filter {
    private static String X_ACCESS_TOKEN = "X-Access-Token";

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        this.initUserInfo((HttpServletRequest)request);
        chain.doFilter(request, response);
    }

    private void initUserInfo(HttpServletRequest request) {
        String token = request.getHeader(X_ACCESS_TOKEN);
        if (token != null) {
            try {
                UserTokenContext.setToken(token);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public void destroy() {
    }
}

