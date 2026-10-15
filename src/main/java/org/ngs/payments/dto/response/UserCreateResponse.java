package org.ngs.payments.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ngs.payments.enums.UserType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateResponse {
    private Long id;
    private String email;
    private UserType userType;
}
