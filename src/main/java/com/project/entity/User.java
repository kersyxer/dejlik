package com.project.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "app_user")
@NamedQueries({
        @NamedQuery(
                name  = "User.findByEmail",
                query = "SELECT u FROM User u WHERE u.email = :email"
        ),
        @NamedQuery(
                name  = "User.findByName",
                query = "SELECT u FROM User u WHERE u.name = :name"
        ),
        @NamedQuery(
                name  = "User.findById",
                query = "SELECT u FROM User u WHERE u.id = :id"
        ),
        @NamedQuery(
                name  = "User.findByEmailAndPassword",
                query = "SELECT u FROM User u WHERE u.email = :email AND u.password = :password"
        ),
        @NamedQuery(
                name  = "User.deleteById",
                query = "DELETE FROM User u WHERE u.id = :id"
        ),
        @NamedQuery(
                name  = "User.allUsers",
                query = "SELECT u FROM User u"
        )
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements Serializable {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "varchar(50) default 'USER'")
    private String role = "USER";

    @OneToMany(mappedBy = "user")
    private Set<Campaign> campaigns;
}