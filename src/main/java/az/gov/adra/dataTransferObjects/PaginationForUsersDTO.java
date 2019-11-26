package az.gov.adra.dataTransferObjects;

import az.gov.adra.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginationForUsersDTO {

    private Integer totalPages;
    private List<User> users;

}
