package com.cts.entity;

import com.cts.enumeration.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String phone;

    private String address;

    private String city;

    private String state;

    private String country;

    private java.time.LocalDate dateOfBirth;

    private String gender;

    @Column(columnDefinition = "TEXT")
    private String agentBio;

    private Integer agentExperienceYears;
}