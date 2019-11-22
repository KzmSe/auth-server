package az.gov.adra.service.interfaces;

import az.gov.adra.entity.User;
import az.gov.adra.exception.UserCredentialsException;

import java.util.List;

public interface UserService {

    List<User> findAllUsers();

    User findUserByUsername(String username) throws UserCredentialsException;

    List<User> findUsersRandomly();

    List<User> findUsersByBirthDate();

}
