package com.code4ro.legalconsultation.authentication.config;

import com.code4ro.legalconsultation.authentication.model.dto.SignUpRequest;
import com.code4ro.legalconsultation.authentication.service.ApplicationUserService;
import com.code4ro.legalconsultation.invitation.model.persistence.Invitation;
import com.code4ro.legalconsultation.invitation.service.InvitationService;
import com.code4ro.legalconsultation.user.model.persistence.User;
import com.code4ro.legalconsultation.user.model.persistence.UserRole;
import com.code4ro.legalconsultation.user.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuthConfigurationTest {
    @Mock
    private ApplicationUserService applicationUserService;
    @Mock
    private UserService userService;
    @Mock
    private InvitationService invitationService;
    @Captor
    private ArgumentCaptor<User> userCaptor;
    @Captor
    private ArgumentCaptor<SignUpRequest> signUpRequestCaptor;

    private final AuthConfiguration authConfiguration = new AuthConfiguration();

    @Test
    public void testInitializeAdminUser() {
        User savedUser = mock(User.class);
        Invitation invitation = mock(Invitation.class);

        when(applicationUserService.count()).thenReturn(0L);
        when(userService.findByEmail(AuthConfiguration.ADMIN_EMAIL)).thenReturn(Optional.empty());
        when(userService.saveEntity(userCaptor.capture())).thenReturn(savedUser);
        when(invitationService.create(Mockito.any(User.class))).thenReturn(invitation);

        authConfiguration.initializeAdminUser(applicationUserService, userService, invitationService);

        assertThat(userCaptor.getValue().getEmail()).isEqualTo(AuthConfiguration.ADMIN_EMAIL);
        assertThat(userCaptor.getValue().getRole()).isEqualTo(UserRole.OWNER);
        verify(userService).saveEntity(userCaptor.getValue());
        verify(invitationService).create(savedUser);
        verify(applicationUserService).save(signUpRequestCaptor.capture());
        assertThat(signUpRequestCaptor.getValue().getUsername()).isEqualTo(AuthConfiguration.ADMIN_USERNAME);
        assertThat(signUpRequestCaptor.getValue().getEmail()).isEqualTo(savedUser.getEmail());
        assertThat(signUpRequestCaptor.getValue().getPassword()).isEqualTo(AuthConfiguration.ADMIN_PASSWORD);
        assertThat(signUpRequestCaptor.getValue().getInvitationCode()).isEqualTo(invitation.getCode());
    }
}
