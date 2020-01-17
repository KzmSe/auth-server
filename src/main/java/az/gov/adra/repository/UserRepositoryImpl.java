package az.gov.adra.repository;

import az.gov.adra.constant.MessageConstants;
import az.gov.adra.constant.UserConstants;
import az.gov.adra.entity.*;
import az.gov.adra.exception.UserCredentialsException;
import az.gov.adra.repository.interfaces.UserRepository;
import az.gov.adra.util.TimeParserUtil;
import az.gov.adra.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Repository
public class UserRepositoryImpl implements UserRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String FIND_ALL_USERS_SQL = "select u.name, u.surname, u.username, u.email, u.img_url, p.name as position_name from users u inner join Position p on u.position_id = p.id where u.enabled = ? order by u.name, u.surname";
    private static final String FIND_USER_BY_USERNAME_SQL = "select u.username, u.name, u.surname, u.midname, u.gender, u.date_of_birth, u.mobile, u.home, u.email, u.img_url, r.name as region_name, d.name as department_name, s.name as section_name, p.name as position_name from users u inner join Region r on u.region_id = r.id inner join Department d on u.department_id = d.id inner join Section s on u.section_id = s.id inner join Position p on u.position_id = p.id where u.username = ? and u.enabled = ?";
    private static final String FIND_USERS_RANDOMLY_SQL = "select top 3 u.name, u.surname, u.username, u.email, u.img_url, p.name as position_name from users u inner join Position p on u.position_id = p.id where u.enabled = ? ORDER BY NEWID()";
    private static final String FIND_USERS_BY_BIRTH_DATE_SQL = "SELECT u.name, u.surname, u.username, u.img_url, date_of_birth, p.name as position_name FROM  users u inner join Position p on u.position_id = p.id WHERE 1 = (FLOOR(DATEDIFF(dd, u.date_of_birth, GETDATE() + 30) / 365.25)) - (FLOOR(DATEDIFF(dd, u.date_of_birth, GETDATE()) / 365.25)) and u.enabled = ? order by DATENAME(mm, u.date_of_birth) DESC, DATENAME(dd, u.date_of_birth) OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    private static final String FIND_TOP_USERS_BY_BIRTH_DATE_SQL = "SELECT top 3 u.name, u.surname, u.username, u.img_url, date_of_birth, p.name as position_name FROM  users u inner join Position p on u.position_id = p.id WHERE 1 = (FLOOR(DATEDIFF(dd, u.date_of_birth, GETDATE()+30) / 365.25)) -(FLOOR(DATEDIFF(dd, u.date_of_birth, GETDATE()) / 365.25)) and u.enabled = ? order by DATENAME(mm, u.date_of_birth) DESC, DATENAME(dd, u.date_of_birth)";
    private static final String UPDATE_USER_SQL = "update users set ";
    private static final String FIND_COUNT_OF_ALL_USERS_SQL = "select count(*) as count from users where enabled = ?";
    private static final String FIND_COUNT_OF_ALL_USERS_BY_BIRTH_DATE_SQL = "SELECT count(*) as count FROM  users u inner join Position p on u.position_id = p.id WHERE 1 = (FLOOR(DATEDIFF(dd, u.date_of_birth, GETDATE()+30) / 365.25)) -(FLOOR(DATEDIFF(dd, u.date_of_birth, GETDATE()) / 365.25)) and u.enabled = ?";

    @Override
    public List<User> findAllUsers() {
        List<User> users = jdbcTemplate.query(FIND_ALL_USERS_SQL, new Object[]{UserConstants.USER_STATUS_ENABLED}, new ResultSetExtractor<List<User>>() {
            @Override
            public List<User> extractData(ResultSet rs) throws SQLException, DataAccessException {
                List<User> usersList = new LinkedList<>();
                while (rs.next()) {
                    User user = new User();
                    user.setName(rs.getString("name"));
                    user.setSurname(rs.getString("surname"));
                    user.setUsername(rs.getString("username"));

                    user.setEmail(rs.getString("email"));
                    user.setImgUrl(rs.getString("img_url"));

                    Position position = new Position();
                    position.setName(rs.getString("position_name"));

                    user.setPosition(position);

                    usersList.add(user);
                }
                return usersList;
            }
        });
        return users;
    }

    @Override
    public User findUserByUsername(String username) throws UserCredentialsException {
        try {
            User user = jdbcTemplate.queryForObject(FIND_USER_BY_USERNAME_SQL, new Object[]{username, UserConstants.USER_STATUS_ENABLED}, new RowMapper<User>() {
                @Override
                public User mapRow(ResultSet rs, int i) throws SQLException {
                    User user = new User();
                    user.setUsername(rs.getString("username"));
                    user.setName(rs.getString("name"));
                    user.setSurname(rs.getString("surname"));
                    user.setMidname(rs.getString("midname"));
                    user.setGender(rs.getString("gender"));
                    user.setMobile(rs.getString("mobile"));
                    user.setHome(rs.getString("home"));
                    user.setEmail(rs.getString("email"));
                    user.setImgUrl(rs.getString("img_url"));

                    LocalDate birthDate = rs.getDate("date_of_birth").toLocalDate();
                    user.setDateOfBirth(TimeParserUtil.formatBirthDate(birthDate));

                    Region region = new Region();
                    region.setName(rs.getString("region_name"));

                    Department department = new Department();
                    department.setName(rs.getString("department_name"));

                    Section section = new Section();
                    section.setName(rs.getString("section_name"));

                    Position position = new Position();
                    position.setName(rs.getString("position_name"));

                    user.setRegion(region);
                    user.setDepartment(department);
                    user.setSection(section);
                    user.setPosition(position);

                    return user;
                }
            });
            return user;

        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<User> findUsersRandomly() {
        List<User> users = jdbcTemplate.query(FIND_USERS_RANDOMLY_SQL, new Object[]{UserConstants.USER_STATUS_ENABLED}, new ResultSetExtractor<List<User>>() {
            @Override
            public List<User> extractData(ResultSet rs) throws SQLException, DataAccessException {
                List<User> usersList = new LinkedList<>();
                while (rs.next()) {
                    User user = new User();
                    user.setName(rs.getString("name"));
                    user.setSurname(rs.getString("surname"));
                    user.setUsername(rs.getString("username"));

                    user.setEmail(rs.getString("email"));
                    user.setImgUrl(rs.getString("img_url"));

                    Position position = new Position();
                    position.setName(rs.getString("position_name"));

                    user.setPosition(position);

                    usersList.add(user);
                }
                return usersList;
            }
        });
        return users;
    }

    @Override
    public List<User> findUsersByBirthDate(int offset) {
        List<User> users = jdbcTemplate.query(FIND_USERS_BY_BIRTH_DATE_SQL, new Object[]{UserConstants.USER_STATUS_ENABLED, offset, UserConstants.USER_FETCH_NEXT}, new ResultSetExtractor<List<User>>() {
            @Override
            public List<User> extractData(ResultSet rs) throws SQLException, DataAccessException {
                List<User> usersList = new LinkedList<>();
                while (rs.next()) {
                    User user = new User();
                    user.setName(rs.getString("name"));
                    user.setSurname(rs.getString("surname"));
                    user.setUsername(rs.getString("username"));
                    user.setImgUrl(rs.getString("img_url"));

                    LocalDate birthDate = rs.getDate("date_of_birth").toLocalDate();
                    user.setDateOfBirth(TimeParserUtil.formatBirthDate(birthDate));

                    Position position = new Position();
                    position.setName(rs.getString("position_name"));

                    user.setPosition(position);

                    usersList.add(user);
                }
                return usersList;
            }
        });
        return users;
    }

    @Override
    public List<User> findTopUsersByBirthDate() {
        List<User> users = jdbcTemplate.query(FIND_TOP_USERS_BY_BIRTH_DATE_SQL, new Object[]{UserConstants.USER_STATUS_ENABLED}, new ResultSetExtractor<List<User>>() {
            @Override
            public List<User> extractData(ResultSet rs) throws SQLException, DataAccessException {
                List<User> usersList = new LinkedList<>();
                while (rs.next()) {
                    User user = new User();
                    user.setName(rs.getString("name"));
                    user.setSurname(rs.getString("surname"));
                    user.setUsername(rs.getString("username"));
                    user.setImgUrl(rs.getString("img_url"));

                    LocalDate birthDate = rs.getDate("date_of_birth").toLocalDate();
                    user.setDateOfBirth(TimeParserUtil.formatBirthDate(birthDate));

                    Position position = new Position();
                    position.setName(rs.getString("position_name"));

                    user.setPosition(position);

                    usersList.add(user);
                }
                return usersList;
            }
        });
        return users;
    }

    @Override
    public void updateUser(User user) throws UserCredentialsException {
        StringBuilder builder = new StringBuilder(UPDATE_USER_SQL);
        List<Object> list = new LinkedList<>();

        if (!ValidationUtil.isNull(user.getImgUrl())) {
            builder.append("img_url = ?");
            list.add(user.getImgUrl());
            if (user.getMobile() != null || user.getHome() != null) {
                builder.append(", ");
            }
        }

        if (!ValidationUtil.isNull(user.getMobile())) {
            builder.append("mobile = ?");
            list.add(user.getMobile());
            if (user.getHome() != null) {
                builder.append(", ");
            }
        }

        if (!ValidationUtil.isNull(user.getHome())) {
            builder.append("home = ?");
            list.add(user.getHome());
        }

        builder.append(" where username = ? and enabled = ?");
        list.add(user.getUsername());
        list.add(UserConstants.USER_STATUS_ENABLED);

        Object[] parameters = list.toArray();

        System.out.println("USER UPDATED");
        System.out.println(builder.toString());
        Arrays.stream(parameters).forEach(System.out::println);

        int affectedRows = jdbcTemplate.update(builder.toString(), parameters);

        if (affectedRows == 0) {
            throw new UserCredentialsException(MessageConstants.ERROR_MESSAGE_INTERNAL_ERROR);
        }
    }

    @Override
    public int findCountOfAllUsers() {
        int totalCount = jdbcTemplate.queryForObject(FIND_COUNT_OF_ALL_USERS_SQL, new Object[] {UserConstants.USER_STATUS_ENABLED}, Integer.class);
        return totalCount;
    }

    @Override
    public int findCountOfAllUsersByBirthDate() {
        int totalCount = jdbcTemplate.queryForObject(FIND_COUNT_OF_ALL_USERS_BY_BIRTH_DATE_SQL, new Object[] {UserConstants.USER_STATUS_ENABLED}, Integer.class);
        return totalCount;
    }

}
