package az.gov.adra.service;

import az.gov.adra.constant.MessageConstants;
import az.gov.adra.dataTransferObject.UserDTOForUpdateUser;
import az.gov.adra.entity.User;
import az.gov.adra.exception.UserCredentialsException;
import az.gov.adra.repository.interfaces.UserRepository;
import az.gov.adra.service.interfaces.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAllUsers();
    }

    @Override
    public User findUserByUsername(String username) throws UserCredentialsException {
        User user = userRepository.findUserByUsername(username);

        if (user == null) {
            throw new UserCredentialsException(MessageConstants.ERROR_MESSAGE_USER_NOT_FOUND);
        }

        return user;
    }

    @Override
    public User findUserByEmail(String email) throws UserCredentialsException {
        return userRepository.findUserByEmail(email);
    }

    @Override
    public void updateToken(String newToken, String oldToken) throws UserCredentialsException {
        userRepository.updateToken(newToken, oldToken);
    }

    @Override
    public List<User> findUsersRandomly() {
        return userRepository.findUsersRandomly();
    }

    @Override
    public List<User> findUsersByBirthDate(int offset) {
        return userRepository.findUsersByBirthDate(offset);
    }

    @Override
    public List<User> findTopUsersByBirthDate() {
        return userRepository.findTopUsersByBirthDate();
    }

    @Override
    public void updateUser(User user) throws UserCredentialsException {
        userRepository.updateUser(user);
    }

    @Override
    public void updatePassword(String password, String token) throws UserCredentialsException {
        userRepository.updatePassword(password, token);
    }

    @Override
    public int findCountOfAllUsers() {
        return userRepository.findCountOfAllUsers();
    }

    @Override
    public int findCountOfAllUsersByBirthDate() {
        return userRepository.findCountOfAllUsersByBirthDate();
    }

}
