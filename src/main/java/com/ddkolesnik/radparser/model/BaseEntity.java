package com.ddkolesnik.radparser.model;

import lombok.Data;

import javax.persistence.*;
import java.time.Instant;

/**
 * @author Alexandr Stegnin
 */

@Data
@MappedSuperclass
public class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "creation_time")
    private Instant creationTime;

    @Column(name = "modified_time")
    private Instant modifiedTime;

    @Column(name = "modified_by")
    private String modifiedBy;

    @PrePersist
    public void prePersist() {
        this.creationTime = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.modifiedTime = Instant.now();
    }

}
