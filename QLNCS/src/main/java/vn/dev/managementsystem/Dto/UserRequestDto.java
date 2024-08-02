package vn.dev.managementsystem.Dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;

@Setter
@Getter
public class UserRequestDto {

    private String fullName;

    private String email;

    private String password;

    private LocalDate dateOfBirth;

    private String description;

    private String phoneNumber;


    public UserRequestDto(String fullName, String email, String password, LocalDate dateOfBirth,
                          String description, String phoneNumber) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.dateOfBirth = dateOfBirth;
        this.description = description;
        this.phoneNumber = phoneNumber;
    }

    public UserRequestDto() {
    }
}
