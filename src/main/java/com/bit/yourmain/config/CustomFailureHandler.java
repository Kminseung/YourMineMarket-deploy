package com.bit.yourmain.config;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        String errMsg = null;
        if (exception instanceof AuthenticationServiceException) {
            errMsg = "err1";

        } else if (exception instanceof BadCredentialsException) {
            errMsg = "err2";

        } else {
            errMsg = "login error";
        }

        setDefaultFailureUrl("/loginpage?error="+errMsg);

        super.onAuthenticationFailure(request,response,exception);
    }
}
