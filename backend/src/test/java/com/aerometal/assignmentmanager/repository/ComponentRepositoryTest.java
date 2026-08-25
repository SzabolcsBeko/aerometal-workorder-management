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

import com.aerometal.assignmentmanager.entity.Component;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(
    replace = AutoConfigureTestDatabase.Replace.NONE
)
class ComponentRepositoryTest {

    @Autowired
    private ComponentRepository repository;

    @Test
    void shouldCreateReadUpdateAndDeleteComponent() {
        String name = uniqueName("COMP");
        Component saved = repository.saveAndFlush(component(name, "Engine module"));

        assertThat(saved.getId()).isNotNull();
        Component found = repository.findById(saved.getId()).orElseThrow();
        assertThat(found.getName()).isEqualTo(name);
        assertThat(found.getDescription()).isEqualTo("Engine module");

        found.setDescription("Updated engine module");
        repository.saveAndFlush(found);
        assertThat(repository.findById(saved.getId()).orElseThrow().getDescription())
                .isEqualTo("Updated engine module");

        repository.deleteById(saved.getId());
        repository.flush();
        assertThat(repository.findById(saved.getId())).isEmpty();
    }

    @Test
    void shouldRejectDuplicateComponentName() {
        String name = uniqueName("COMP");
        repository.saveAndFlush(component(name, "First"));

        assertThatThrownBy(() -> repository.saveAndFlush(component(name, "Second")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private String uniqueName(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private Component component(String name, String description) {
        Component component = new Component();
        component.setName(name);
        component.setDescription(description);
        return component;
    }
}
