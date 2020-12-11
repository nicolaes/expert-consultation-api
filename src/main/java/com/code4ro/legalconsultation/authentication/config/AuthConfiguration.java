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
    @Transactional
    @Autowired
    void initializeAdminUser(ApplicationUserService applicationUserService, UserService userService,
                             InvitationService invitationService) {
        String email = "admin@example.com";
        String username = "admin";
        String password = "admin";

        if (applicationUserService.count() > 0 || userService.findByEmail(email).isPresent()) {
            log.info("Admin user NOT created for local development due to existing users or duplicate email.");
            return;
        }

        User user = new User(email, UserRole.OWNER);
        user.setSpecialization(UserSpecialization.ARCHITECT);
        final User savedUser = userService.saveEntity(user);

        Invitation invitation = invitationService.create(savedUser);

        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setName(username);
        signUpRequest.setUsername(username);
        signUpRequest.setEmail(savedUser.getEmail());
        signUpRequest.setPassword(password);
        signUpRequest.setInvitationCode(invitation.getCode());
        applicationUserService.save(signUpRequest);
        log.info("Created user '{}' with password '{}' for local development.", username, password);
    }
}
