package com.aerometal.assignmentmanager.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class VersionedEntity {

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}