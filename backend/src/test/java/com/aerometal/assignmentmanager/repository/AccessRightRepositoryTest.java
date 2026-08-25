package com.aerometal.assignmentmanager.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import com.aerometal.assignmentmanager.entity.AccessRight;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(
    replace = AutoConfigureTestDatabase.Replace.NONE
)
class AccessRightRepositoryTest {

    @Autowired
    private AccessRightRepository repository;

    @Test
    void shouldCreateReadUpdateAndDeleteAccessRight() {
        String name = uniqueName("RIGHT");
        AccessRight saved = repository.saveAndFlush(right(name, "Inspector"));

        assertThat(saved.getId()).isNotNull();
        AccessRight found = repository.findById(saved.getId()).orElseThrow();
        assertThat(found.getName()).isEqualTo(name);
        assertThat(found.getDescription()).isEqualTo("Inspector");

        found.setDescription("Final inspector");
        repository.saveAndFlush(found);
        assertThat(repository.findById(saved.getId()).orElseThrow().getDescription())
                .isEqualTo("Final inspector");

        repository.deleteById(saved.getId());
        repository.flush();
        assertThat(repository.findById(saved.getId())).isEmpty();
    }

    @Test
    void shouldRejectDuplicateAccessRightName() {
        String name = uniqueName("RIGHT");
        repository.saveAndFlush(right(name, "Inspector"));

        assertThatThrownBy(() -> repository.saveAndFlush(right(name, "Duplicate")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private String uniqueName(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private AccessRight right(String name, String description) {
        AccessRight right = new AccessRight();
        right.setName(name);
        right.setDescription(description);
        return right;
    }
}
