package com.code4ro.legalconsultation.security.service;

import com.code4ro.legalconsultation.authentication.model.persistence.ApplicationUser;
import com.code4ro.legalconsultation.security.model.CurrentUser;

public interface CurrentUserService {
    ApplicationUser getCurrentApplicationUser();
    CurrentUser getCurrentUser();
}
