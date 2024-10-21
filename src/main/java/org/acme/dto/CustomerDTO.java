package org.acme.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CustomerDTO {

    @NotNull
    @Size(max = 9)
    private String vat;

    @NotNull
    @Size(max = 30)
    private String firstName;

    @NotNull
    @Size(max = 50)
    private String lastName;

    @NotNull
    @Email
    @Size(max = 80)
    private String email;

    @Size(max = 20)
    private String mobilePhone;

}
