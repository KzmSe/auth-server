package az.gov.adra.controller;

import az.gov.adra.constant.MessageConstants;
import az.gov.adra.dataTransferObject.UserDTOForUpdateUser;
import az.gov.adra.entity.User;
import az.gov.adra.entity.response.GenericResponse;
import az.gov.adra.exception.UserCredentialsException;
import az.gov.adra.service.interfaces.UserService;
import az.gov.adra.util.EmailSenderUtil;
import az.gov.adra.util.ResourceUtil;
import az.gov.adra.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private BCryptPasswordEncoder encoder;
    @Autowired
    private EmailSenderUtil emailSenderUtil;
    private final int maxFileSize = 3145728;
    @Value("${spring.email.changePassword.subject}")
    private String subject;
    @Value("${spring.email.changePassword.body}")
    private String body;
    @Value("${file.upload.path.win}")
    private String imageUploadPath;

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_HR')")
    public GenericResponse findAllUsers() throws UserCredentialsException {
        List<User> users = userService.findAllUsers();
        for (User user : users) {
            if (user.getImgUrl() == null) {
                continue;
            }
            user.setImgUrl(ResourceUtil.convertToString(user.getImgUrl()));
        }

        return GenericResponse.withSuccess(HttpStatus.OK, "list of users", users);
    }

    @GetMapping("/users/me")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_HR')")
    public GenericResponse findMe(final Principal principal) throws UserCredentialsException {
        User user = userService.findUserByUsername(principal.getName());
        user.setImgUrl(ResourceUtil.convertToString(user.getImgUrl()));
        return GenericResponse.withSuccess(HttpStatus.OK, "me", user);
    }

    @GetMapping("/users/{username}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_HR')")
    public GenericResponse findUserByUsername(@PathVariable(name = "username", required = false) String username) throws UserCredentialsException {
        if (ValidationUtil.isNullOrEmpty(username)) {
            throw new UserCredentialsException(MessageConstants.ERROR_MESSAGE_USER_NOT_FOUND);
        }

        User user = userService.findUserByUsername(username);
        user.setImgUrl(ResourceUtil.convertToString(user.getImgUrl()));
        return GenericResponse.withSuccess(HttpStatus.OK, "specific user by username", user);
    }

//    @GetMapping("/users/count")
//    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_HR')")
//    public GenericResponse findCountOfAllUsers() {
//        int count = userService.findCountOfAllUsers();
//        return GenericResponse.withSuccess(HttpStatus.OK, "count of users", count);
//    }

    @GetMapping("/users/random")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_HR')")
    public GenericResponse findUsersRandomly() {
        List<User> users = userService.findUsersRandomly();
        for (User user : users) {
            if (user.getImgUrl() == null) {
                continue;
            }
            user.setImgUrl(ResourceUtil.convertToString(user.getImgUrl()));
        }
        return GenericResponse.withSuccess(HttpStatus.OK, "list of random users", users);
    }

    @GetMapping("/users/birth-date")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_HR')")
    public GenericResponse findAllUsersByBirthDate(@RequestParam(name = "page", required = false) Integer page,
                                                   HttpServletResponse response) throws UserCredentialsException {
        if (ValidationUtil.isNull(page)) {
            throw new UserCredentialsException(MessageConstants.ERROR_MESSAGE_ONE_OR_MORE_FIELDS_ARE_EMPTY);
        }

        int total = userService.findCountOfAllUsersByBirthDate();
        int totalPages = 0;
        int offset = 0;

        if (total != 0) {
            totalPages = (int) Math.ceil((double) total / 10);

            if (page != null && page >= totalPages) {
                offset = (totalPages - 1) * 10;

            } else if (page != null && page > 1) {
                offset = (page - 1) * 10;
            };
        }

        List<User> users = userService.findUsersByBirthDate(offset);
        for (User user : users) {
            if (user.getImgUrl() == null) {
                continue;
            }
            user.setImgUrl(ResourceUtil.convertToString(user.getImgUrl()));
        }

        response.addIntHeader("Total-Pages", totalPages);
        return GenericResponse.withSuccess(HttpStatus.OK, "list of all users by birth date", users);
    }

    @GetMapping("/users/birth-date/top-three")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_HR')")
    public GenericResponse findTopThreeUsersByBirthDate() {
        List<User> users = userService.findTopUsersByBirthDate();
        for (User user : users) {
            if (user.getImgUrl() == null) {
                continue;
            }
            user.setImgUrl(ResourceUtil.convertToString(user.getImgUrl()));
        }
        return GenericResponse.withSuccess(HttpStatus.OK, "list of top three users by birth date", users);
    }

    @PutMapping("/users/data")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_HR')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void updateUser(@RequestBody UserDTOForUpdateUser dto,
                           Principal principal) throws UserCredentialsException, IOException {
        if (ValidationUtil.isNull(dto.getMultipartFile()) && ValidationUtil.isNullOrEmpty(dto.getMobile()) && ValidationUtil.isNullOrEmpty(dto.getHome())) {
            throw new UserCredentialsException(MessageConstants.ERROR_MESSAGE_ONE_OR_MORE_FIELDS_ARE_EMPTY);
        }

        User user = new User();
        user.setUsername(principal.getName());

        if (dto.getMultipartFile() == null || dto.getMultipartFile().isEmpty()) {
            user.setImgUrl(null);

        } else {
            if (!(dto.getMultipartFile().getOriginalFilename().endsWith(".jpg")
                    || dto.getMultipartFile().getOriginalFilename().endsWith(".jpeg")
                    || dto.getMultipartFile().getOriginalFilename().endsWith(".png"))) {
                throw new UserCredentialsException(MessageConstants.ERROR_MESSAGE_INVALID_FILE_TYPE);
            }

            if (dto.getMultipartFile().getSize() >= maxFileSize) {
                throw new UserCredentialsException(MessageConstants.ERROR_MESSAGE_FILE_SIZE_MUST_BE_SMALLER_THAN_5MB);
            }

            Path pathToSaveFile = Paths.get(imageUploadPath, "profiles", user.getUsername());

            if (!Files.exists(pathToSaveFile)) {
                Files.createDirectories(pathToSaveFile);
            }

            String fileName = UUID.randomUUID() + "##" + dto.getMultipartFile().getOriginalFilename();
            Path fullFilePath = Paths.get(pathToSaveFile.toString(), fileName);
            Files.copy(dto.getMultipartFile().getInputStream(), fullFilePath, StandardCopyOption.REPLACE_EXISTING);
            Path pathToSaveDb = Paths.get("profiles", user.getUsername(), fileName);

            user.setImgUrl(DatatypeConverter.printHexBinary(pathToSaveDb.toString().getBytes()));
        }

        user.setMobile(dto.getMobile());
        user.setHome(dto.getHome());

        userService.updateUser(user);
    }

    @PostMapping("/users/email")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_HR')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void sendEmail(@RequestParam(value = "email", required = false) String email) throws UserCredentialsException {
        if (ValidationUtil.isNullOrEmpty(email)) {
            throw new UserCredentialsException(MessageConstants.ERROR_MESSAGE_ONE_OR_MORE_FIELDS_ARE_EMPTY);
        }

        User user = userService.findUserByEmail(email.trim());
        //TODO: add thread to send email!
        emailSenderUtil.sendEmailMessage(email, subject, String.format(body, user.getToken()));
    }


    @PutMapping("/users/password")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_HR')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void updatePassword(@RequestBody UserDTOForUpdateUser dto,
                           Principal principal) throws UserCredentialsException {
        if (ValidationUtil.isNullOrEmpty(dto.getToken()) && ValidationUtil.isNullOrEmpty(dto.getPassword()) && ValidationUtil.isNullOrEmpty(dto.getConfirmPassword())) {
            throw new UserCredentialsException(MessageConstants.ERROR_MESSAGE_ONE_OR_MORE_FIELDS_ARE_EMPTY);
        }

        User user = new User();
        user.setUsername(principal.getName());

        if (dto.getPassword().trim().length() >= 8 && dto.getConfirmPassword().trim().length() >= 8) {
            if (dto.getPassword().equals(dto.getConfirmPassword())) {
                try {
                    userService.updatePassword(encoder.encode(dto.getPassword()), dto.getToken());
                    String newToken = UUID.randomUUID().toString();
                    userService.updateToken(newToken, dto.getToken());

                } catch (UserCredentialsException e) {
                    throw new UserCredentialsException(MessageConstants.ERROR_MESSAGE_INTERNAL_ERROR);
                }
            } else {
                throw new UserCredentialsException(MessageConstants.ERROR_MESSAGE_ONE_OR_MORE_FIELDS_ARE_EMPTY);
            }
        } else {
            throw new UserCredentialsException(MessageConstants.ERROR_MESSAGE_PASSWORD_MUST_CONTAINS_MINIMUM_8_CHARACTERS);
        }
    }

}
