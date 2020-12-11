package com.code4ro.legalconsultation.authentication.config;

import com.code4ro.legalconsultation.authentication.model.dto.SignUpRequest;
import com.code4ro.legalconsultation.authentication.service.ApplicationUserService;
import com.code4ro.legalconsultation.invitation.model.persistence.Invitation;
import com.code4ro.legalconsultation.invitation.service.InvitationService;
import com.code4ro.legalconsultation.user.model.persistence.User;
import com.code4ro.legalconsultation.user.model.persistence.UserRole;
import com.code4ro.legalconsultation.user.model.persistence.UserSpecialization;
import com.code4ro.legalconsultation.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

@Profile("dev")
@Configuration
@Slf4j
public class AuthConfiguration {
    public static final String ADMIN_EMAIL = "admin@example.com";
    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin";

    @Transactional
    @Autowired
    void initializeAdminUser(ApplicationUserService applicationUserService, UserService userService,
                             InvitationService invitationService) {
        if (applicationUserService.count() > 0 || userService.findByEmail(ADMIN_EMAIL).isPresent()) {
            log.info("Admin user NOT created for local development due to existing users or duplicate email.");
            return;
        }

        User user = new User(ADMIN_EMAIL, UserRole.OWNER);
        user.setSpecialization(UserSpecialization.ARCHITECT);
        final User savedUser = userService.saveEntity(user);

        Invitation invitation = invitationService.create(savedUser);

        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setName(ADMIN_USERNAME);
        signUpRequest.setUsername(ADMIN_USERNAME);
        signUpRequest.setEmail(savedUser.getEmail());
        signUpRequest.setPassword(ADMIN_PASSWORD);
        signUpRequest.setInvitationCode(invitation.getCode());
        applicationUserService.save(signUpRequest);
        log.info("Created user '{}' with password '{}' for local development.", ADMIN_USERNAME, ADMIN_PASSWORD);
    }
}
