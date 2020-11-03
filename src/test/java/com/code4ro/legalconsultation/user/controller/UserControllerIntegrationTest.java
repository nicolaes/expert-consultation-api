package com.code4ro.legalconsultation.user.controller;

import com.code4ro.legalconsultation.core.controller.AbstractControllerIntegrationTest;
import com.code4ro.legalconsultation.invitation.repository.InvitationRepository;
import com.code4ro.legalconsultation.user.model.dto.UserDto;
import com.code4ro.legalconsultation.user.model.persistence.User;
import com.code4ro.legalconsultation.user.model.persistence.UserRole;
import com.code4ro.legalconsultation.user.repository.UserRepository;
import com.code4ro.legalconsultation.core.factory.RandomObjectFiller;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerIntegrationTest extends AbstractControllerIntegrationTest {

    private static final String USER_AS_STRING = "john,doe,john@email.com,42345,district,org";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InvitationRepository invitationRepository;

    @Before
    public void before() {
        invitationRepository.deleteAll();
        userRepository.deleteAll();
        when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));
    }

    @Test
    @WithMockUser
    @Transactional
    public void saveUser() throws Exception {
        final UserDto userDto = RandomObjectFiller.createAndFill(UserDto.class);
        userDto.setId(null);

        mvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value(userDto.getFirstName()))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.role").value(userDto.getRole().toString()))
                .andExpect(status().isOk());

        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    @WithMockUser
    @Transactional
    public void saveUserEmailException() throws Exception {
        final UserDto userDto = RandomObjectFiller.createAndFill(UserDto.class);
        userDto.setId(null);

        mvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value(userDto.getFirstName()))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.role").value(userDto.getRole().toString()))
                .andExpect(status().isOk());


        mvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.i18nFieldErrors.email.i18nErrorKey")
                        .value("user.save.duplicatedEmail"));

        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    @WithMockUser
    @Transactional
    public void saveUsers() throws Exception {
        final List<User> users = Arrays.asList(
                RandomObjectFiller.createAndFill(User.class), RandomObjectFiller.createAndFill(User.class));

        mvc.perform(post("/api/users/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(users))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(status().isOk());

        assertThat(userRepository.count()).isEqualTo(2);
    }

    @Test
    @WithMockUser
    @Transactional
    public void getUser() throws Exception {
        final User user = userRepository.save(RandomObjectFiller.createAndFill(User.class));

        mvc.perform(get("/api/users/" + user.getId().toString())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(user.getId().toString()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @Transactional
    public void list() throws Exception {
        final List<User> users = Arrays.asList(
                RandomObjectFiller.createAndFill(User.class),
                RandomObjectFiller.createAndFill(User.class),
                RandomObjectFiller.createAndFill(User.class));
        userRepository.saveAll(users);

        mvc.perform(get("/api/users?page=0")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.size()").value(2))
                .andExpect(status().isOk());

        mvc.perform(get("/api/users?page=1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(status().isOk());

        users.sort(Comparator.comparing(User::getFirstName));
        mvc.perform(get("/api/users?sort=firstName")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].firstName").value(users.get(0).getFirstName()))
                .andExpect(status().isOk());

        users.sort(Comparator.comparing(User::getEmail));
        mvc.perform(get("/api/users?sort=email")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].email").value(users.get(0).getEmail()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @Transactional
    public void deleteUser() throws Exception {
        final User user = userRepository.save(RandomObjectFiller.createAndFill(User.class));
        assertThat(userRepository.count()).isEqualTo(1);

        mvc.perform(delete("/api/users/" + user.getId().toString())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertThat(userRepository.count()).isEqualTo(0);
    }

    @Test
    @WithMockUser
    public void extractUsers() throws Exception {
        final String csvContent = "john,doe,john@email.com,42345,district,org";
        final MockMultipartFile firstFile = new MockMultipartFile("file", "users.csv",
                "text/plain", csvContent.getBytes());

        mvc.perform(MockMvcRequestBuilders.multipart("/api/users/extract")
                .file(firstFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("john"))
                .andExpect(jsonPath("$[0].lastName").value("doe"))
                .andExpect(jsonPath("$[0].email").value("john@email.com"))
                .andExpect(jsonPath("$[0].phoneNumber").value("42345"))
                .andExpect(jsonPath("$[0].district").value("district"))
                .andExpect(jsonPath("$[0].organisation").value("org"))
                .andExpect(jsonPath("$[0].role").value(UserRole.CONTRIBUTOR.toString()));
    }

    @Test
    @WithMockUser
    public void extractUserFromCopy() throws Exception {

        final List<String> users = Collections.singletonList(USER_AS_STRING);

        mvc.perform(post("/api/users/extract-from-copy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(users))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].firstName").value("john"))
                .andExpect(jsonPath("$[0].lastName").value("doe"))
                .andExpect(jsonPath("$[0].email").value("john@email.com"))
                .andExpect(jsonPath("$[0].phoneNumber").value("42345"))
                .andExpect(jsonPath("$[0].district").value("district"))
                .andExpect(jsonPath("$[0].organisation").value("org"))
                .andExpect(jsonPath("$[0].role").value(UserRole.CONTRIBUTOR.toString()))
                .andExpect(status().isOk());
    }


    @Test
    @WithMockUser
    @Transactional
    public void searchUserByTerm() throws Exception {
        User firstNameUser = RandomObjectFiller.createAndFill(User.class);
        firstNameUser.setFirstName("firstly");
        User lastNameUser = RandomObjectFiller.createAndFill(User.class);
        lastNameUser.setLastName("lastly");
        User emailUser = RandomObjectFiller.createAndFill(User.class);
        emailUser.setEmail("emaily@mail.com");
        userRepository.saveAll(Arrays.asList(firstNameUser, lastNameUser, emailUser));

        assertThat(userRepository.count()).isEqualTo(3);

        mvc.perform(get("/api/users/search")
                .param("searchTerm", "first")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].firstName").value("firstly"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/users/search")
                .param("searchTerm", "lastly")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].lastName").value("lastly"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/users/search")
                .param("searchTerm", "emaily")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].email").value("emaily@mail.com"))
                .andExpect(status().isOk());
    }

}
