package com.code4ro.legalconsultation.security.service.impl;

import com.code4ro.legalconsultation.authentication.model.persistence.ApplicationUser;
import com.code4ro.legalconsultation.authentication.service.ApplicationUserService;
import com.code4ro.legalconsultation.security.model.CurrentUser;
import com.code4ro.legalconsultation.security.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class SpringCurrentUserService implements CurrentUserService {
    private final ApplicationUserService applicationUserService;

    @Autowired
    public SpringCurrentUserService(ApplicationUserService applicationUserService) {
        this.applicationUserService = applicationUserService;
    }

    @Override
    public ApplicationUser getCurrentApplicationUser() {
        String username = Objects.requireNonNull(getCurrentUserDetails()).getUsername();
        return applicationUserService.getByUsernameOrEmail(username);
    }

    @Override
    public CurrentUser getCurrentUser() {
        UserDetails user = getCurrentUserDetails();
        return (user instanceof CurrentUser) ? (CurrentUser) user : null;
    }

    private UserDetails getCurrentUserDetails() {
        final SecurityContext securityContext = SecurityContextHolder.getContext();
        final Authentication auth = securityContext.getAuthentication();
        if (auth == null) {
            return null;
        }
        final Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails) {
            return (UserDetails) principal;
        }
        return null;
    }
}
