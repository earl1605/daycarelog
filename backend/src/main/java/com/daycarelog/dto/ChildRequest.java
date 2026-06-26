package com.daycarelog.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ChildRequest {
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String sex;
    private String address;
    private LocalDate enrollmentDate;
    private String enrollmentStatus;
}
