package az.gov.adra.controller;

import az.gov.adra.constant.MessageConstants;
import az.gov.adra.entity.User;
import az.gov.adra.entity.response.GenericResponse;
import az.gov.adra.exception.UserCredentialsException;
import az.gov.adra.service.interfaces.UserService;
import az.gov.adra.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_HR')")
    public GenericResponse findAllUsers() {
        List<User> users = userService.findAllUsers();
        return GenericResponse.withSuccess(HttpStatus.OK, "list of users", users);
    }

    @GetMapping("/users/me")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_HR')")
    public GenericResponse findMe(final Principal principal) throws UserCredentialsException {
        User user = userService.findUserByUsername(principal.getName());
        return GenericResponse.withSuccess(HttpStatus.OK, "me", user);
    }

    @GetMapping("/users/{username}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_HR')")
    public GenericResponse findUserByUsername(@PathVariable(name = "username", required = false) String username) throws UserCredentialsException {
        if (ValidationUtil.isNullOrEmpty(username)) {
            throw new UserCredentialsException(MessageConstants.ERROR_MESSAGE_USER_NOT_FOUND);
        }

        User user = userService.findUserByUsername(username);

        return GenericResponse.withSuccess(HttpStatus.OK, "specific user by username", user);
    }

    @GetMapping("/users/random")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_HR')")
    public GenericResponse findUsersRandomly() {
        List<User> users = userService.findUsersRandomly();
        return GenericResponse.withSuccess(HttpStatus.OK, "list of random users", users);
    }

    @GetMapping("/users/birth-date")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_HR')")
    public GenericResponse findAllUsersByBirthDate() {
        List<User> users = userService.findUsersByBirthDate();
        return GenericResponse.withSuccess(HttpStatus.OK, "list of all users by birth date", users);
    }

    @GetMapping("/users/birth-date/top-three")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_HR')")
    public GenericResponse findTopThreeUsersByBirthDate() {
        List<User> users = userService.findUsersByBirthDate();
        List<User> topThreeUsers = new ArrayList<>();
        if (users.size() != 0) {
            for (int i = 0; i < 3; i++) {
                topThreeUsers.add(users.get(i));
            }
        }
        return GenericResponse.withSuccess(HttpStatus.OK, "list of top three all users by birth date", topThreeUsers);
    }

}
