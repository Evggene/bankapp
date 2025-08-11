package org.bea.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user", schema = "accounts")
@Setter
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class User {
    @Id
    private UUID id;
    private String username;
    private String password;
    private String name;
    private LocalDate birthdate;
    private boolean enabled;

}
