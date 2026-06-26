package com.daycarelog.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "guardians")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Guardian {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "child_id", nullable = false)
    private Long childId;

    @Column(nullable = false)
    private String name;

    private String relationship;

    @Column(name = "contact_number")
    private String contactNumber;

    @Column(name = "is_primary")
    @Builder.Default
    private Boolean isPrimary = false;
}
