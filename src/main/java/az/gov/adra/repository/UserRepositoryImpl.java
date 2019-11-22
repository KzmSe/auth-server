package az.gov.adra.repository;

import az.gov.adra.constant.UserConstants;
import az.gov.adra.entity.*;
import az.gov.adra.exception.UserCredentialsException;
import az.gov.adra.repository.interfaces.UserRepository;
import az.gov.adra.util.TimeParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@Repository
public class UserRepositoryImpl implements UserRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String FIND_ALL_USERS_SQL = "select u.name, u.surname, u.email, u.img_url, p.name as position_name from users u inner join Position p on u.position_id = p.id where u.enabled = ?";
    private static final String FIND_USER_BY_USERNAME_SQL = "select u.username, u.name, u.surname, u.midname, u.gender, u.date_of_birth, u.mobile, u.home, u.email, u.img_url, r.name as region_name, d.name as department_name, s.name as section_name, p.name as position_name from users u inner join Region r on u.region_id = r.id inner join Department d on u.department_id = d.id inner join Section s on u.section_id = s.id inner join Position p on u.position_id = p.id where u.username = ? and u.enabled = ?";
    private static final String FIND_USERS_RANDOMLY_SQL = "select top 3 u.name, u.surname, u.email, u.img_url, p.name as position_name from users u inner join Position p on u.position_id = p.id where u.enabled = ? ORDER BY NEWID()";
    private static final String FIND_USERS_BY_BIRTH_DATE_SQL = "select u.name, u.surname, u.img_url, u.date_of_birth, p.name as position_name from users u inner join Position p on u.position_id = p.id where FORMAT(date_of_birth, 'MM-dd') BETWEEN FORMAT(GETDATE(), 'MM-dd') and FORMAT(DATEADD(DAY, 7, GETDATE()), 'MM-dd') and u.enabled = ?";

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
                    user.setMobile(rs.getString("mobile")); //null
                    user.setHome(rs.getString("home")); //null
                    user.setEmail(rs.getString("email"));
                    user.setImgUrl(rs.getString("img_url"));
                    user.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());

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
    public List<User> findUsersByBirthDate() {
        List<User> users = jdbcTemplate.query(FIND_USERS_BY_BIRTH_DATE_SQL, new Object[]{UserConstants.USER_STATUS_ENABLED}, new ResultSetExtractor<List<User>>() {
            @Override
            public List<User> extractData(ResultSet rs) throws SQLException, DataAccessException {
                List<User> usersList = new LinkedList<>();
                while (rs.next()) {
                    User user = new User();
                    user.setName(rs.getString("name"));
                    user.setSurname(rs.getString("surname"));
                    user.setImgUrl(rs.getString("img_url"));
                    user.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());

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

}
