package org.ngs.payments.entity;

import jakarta.persistence.*;
import lombok.*;
import org.ngs.payments.enums.UserType;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 20)
    private UserType userType;

    @ToString.Exclude
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
}
