package az.gov.adra.controller;

import az.gov.adra.constant.MessageConstants;
import az.gov.adra.dataTransferObject.UserDTOForUpdateUser;
import az.gov.adra.entity.User;
import az.gov.adra.entity.response.GenericResponse;
import az.gov.adra.exception.UserCredentialsException;
import az.gov.adra.service.interfaces.UserService;
import az.gov.adra.util.ResourceUtil;
import az.gov.adra.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private final int maxFileSize = 3145728;
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
        System.out.println(total);
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

        System.out.println(offset);

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
    public void updateUser(@RequestParam(name = "file", required = false) MultipartFile file,
                           @RequestParam(name = "mobile", required = false) String mobile,
                           @RequestParam(name = "work", required = false) String work,
                           Principal principal) throws UserCredentialsException, IOException {
        if (ValidationUtil.isNull(file) && ValidationUtil.isNullOrEmpty(mobile) && ValidationUtil.isNullOrEmpty(work)) {
            throw new UserCredentialsException(MessageConstants.ERROR_MESSAGE_ONE_OR_MORE_FIELDS_ARE_EMPTY);
        }

        if (!ValidationUtil.isNullOrEmpty(mobile) && mobile.trim().length() != 10) {
            throw new UserCredentialsException(MessageConstants.ERROR_MESSAGE_MOBILE_NUMBER_MUST_CONTAINS_TEN_CHARACTERS);
        }

        if (!ValidationUtil.isNullOrEmpty(work) && work.trim().length() != 4) {
            throw new UserCredentialsException(MessageConstants.ERROR_MESSAGE_WORK_NUMBER_MUST_CONTAINS_FOUR_CHARACTERS);
        }

        User user = new User();
        user.setUsername(principal.getName());

        if (file == null || file.isEmpty()) {
            user.setImgUrl(null);

        } else {
            if (!(file.getOriginalFilename().endsWith(".jpg")
                    || file.getOriginalFilename().endsWith(".jpeg")
                    || file.getOriginalFilename().endsWith(".png"))) {
                throw new UserCredentialsException(MessageConstants.ERROR_MESSAGE_INVALID_FILE_TYPE);
            }

            if (file.getSize() >= maxFileSize) {
                throw new UserCredentialsException(MessageConstants.ERROR_MESSAGE_FILE_SIZE_MUST_BE_SMALLER_THAN_5MB);
            }

            Path pathToSaveFile = Paths.get(imageUploadPath, "profiles", user.getUsername());

            if (!Files.exists(pathToSaveFile)) {
                Files.createDirectories(pathToSaveFile);
            }

            String fileName = UUID.randomUUID() + "&&" + file.getOriginalFilename();
            Path fullFilePath = Paths.get(pathToSaveFile.toString(), fileName);
            Files.copy(file.getInputStream(), fullFilePath, StandardCopyOption.REPLACE_EXISTING);
            Path pathToSaveDb = Paths.get("profiles", user.getUsername(), fileName);

            user.setImgUrl(DatatypeConverter.printHexBinary(pathToSaveDb.toString().getBytes()));
        }

        user.setMobile(mobile != null ? mobile.trim() : null);
        user.setHome(work != null ? work.trim() : null);

        userService.updateUser(user);
    }

}
