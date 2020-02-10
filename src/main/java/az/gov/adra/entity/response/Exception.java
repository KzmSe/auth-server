package az.gov.adra.entity.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Exception {

    private String code;
    private String message;
    private String errorStack;


}
