package com.code4ro.legalconsultation.user.factory;

import com.code4ro.legalconsultation.authentication.model.persistence.ApplicationUser;
import com.code4ro.legalconsultation.core.factory.RandomObjectFiller;
import com.code4ro.legalconsultation.user.model.persistence.User;
import com.code4ro.legalconsultation.user.model.persistence.UserRole;
import org.springframework.stereotype.Component;

@Component
public class UserFactory {

    public ApplicationUser createApplicationUserWithRole(final UserRole role) {
        final User user = RandomObjectFiller.createAndFillWithBaseEntity(User.class);
        user.setRole(role);

        final ApplicationUser applicationUser = RandomObjectFiller.createAndFillWithBaseEntity(ApplicationUser.class);
        applicationUser.setUser(user);

        return applicationUser;
    }
}
