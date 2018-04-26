package org.jasig.cas.web.service;

import org.jasig.cas.authentication.AbstractAuthenticationManager;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.handler.BadCredentialsAuthenticationException;
import org.jasig.cas.authentication.handler.UnsupportedCredentialsException;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.util.ApplicationContextProvider;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Iterator;
import java.util.List;

public class MyAuthenticationManagerImpl extends AbstractAuthenticationManager {
    @NotNull
    @Size(
            min = 1
    )
    private List<AuthenticationHandler> authenticationHandlers;
    @NotNull
    @Size(
            min = 1
    )

    private ForceLogoutManager forceLogoutManager;
    private List<CredentialsToPrincipalResolver> credentialsToPrincipalResolvers;

    public MyAuthenticationManagerImpl() {
    }

    protected AbstractAuthenticationManager.Pair<AuthenticationHandler, Principal> authenticateAndObtainPrincipal(Credentials credentials) throws AuthenticationException {
        boolean foundSupported = false;
        boolean authenticated = false;
        AuthenticationHandler authenticatedClass = null;
        Iterator var7 = this.authenticationHandlers.iterator();

        while(var7.hasNext()) {
            AuthenticationHandler authenticationHandler = (AuthenticationHandler)var7.next();
            if (authenticationHandler.supports(credentials)) {
                foundSupported = true;
                String handlerName = authenticationHandler.getClass().getName();

                try {
                    if (authenticationHandler.authenticate(credentials)) {
                        this.log.info("{} successfully authenticated {}", handlerName, credentials);
                        authenticatedClass = authenticationHandler;
                        authenticated = true;
                        //登录成功，踢掉前一个相同登录的人
                        String userName = ((UsernamePasswordCredentials)credentials).getUsername();
                        forceLogoutManager.doLogout(userName);
                        break;
                    }

                    this.log.info("{} failed to authenticate {}", handlerName, credentials);
                } catch (Exception var9) {
                    this.handleError(handlerName, credentials, var9);
                }
            }
        }




        if (!authenticated) {
            if (foundSupported) {
                throw BadCredentialsAuthenticationException.ERROR;
            } else {
                throw UnsupportedCredentialsException.ERROR;
            }
        } else {

            foundSupported = false;
            var7 = this.credentialsToPrincipalResolvers.iterator();

            while(var7.hasNext()) {
                CredentialsToPrincipalResolver credentialsToPrincipalResolver = (CredentialsToPrincipalResolver)var7.next();
                if (credentialsToPrincipalResolver.supports(credentials)) {
                    Principal principal = credentialsToPrincipalResolver.resolvePrincipal(credentials);
                    this.log.info("Resolved principal " + principal);
                    foundSupported = true;
                    if (principal != null) {
                        return new Pair(authenticatedClass, principal);
                    }
                }
            }

            if (foundSupported) {
                if (this.log.isDebugEnabled()) {
                    this.log.debug("CredentialsToPrincipalResolver found but no principal returned.");
                }

                throw BadCredentialsAuthenticationException.ERROR;
            } else {
                this.log.error("CredentialsToPrincipalResolver not found for " + credentials.getClass().getName());
                throw UnsupportedCredentialsException.ERROR;
            }
        }

    }

    public void setAuthenticationHandlers(List<AuthenticationHandler> authenticationHandlers) {
        this.authenticationHandlers = authenticationHandlers;
    }

    public void setCredentialsToPrincipalResolvers(List<CredentialsToPrincipalResolver> credentialsToPrincipalResolvers) {
        this.credentialsToPrincipalResolvers = credentialsToPrincipalResolvers;
    }

    public void setForceLogoutManager(ForceLogoutManager forceLogoutManager) {
        this.forceLogoutManager = forceLogoutManager;
    }
}