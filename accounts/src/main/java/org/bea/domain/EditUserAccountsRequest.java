package org.bea.domain;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EditUserAccountsRequest {
    private String name;
    private LocalDate birthdate;
}
