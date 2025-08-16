package org.bea.domain;

import lombok.Data;

@Data
public class EditPasswordRequest {
    private String password;
    private String confirmPassword;
    private String login;
}
