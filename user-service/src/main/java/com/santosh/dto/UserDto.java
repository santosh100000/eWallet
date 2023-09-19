package com.santosh.dto;

import jakarta.persistence.Column;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private String name;

    private String email;

    private String phone;

    private String kycId;

    private String address;


}
