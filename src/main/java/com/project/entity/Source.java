package com.project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "sources")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Source implements Serializable {
    @Id
    @GeneratedValue
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "clickflare_id", unique = true)
    private String clickflareId;
}
