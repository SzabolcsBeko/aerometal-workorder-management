package com.aerometal.assignmentmanager.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.aerometal.assignmentmanager.entity.Employee;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(
    replace = AutoConfigureTestDatabase.Replace.NONE
)
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository repository;

    @Test
    void shouldCreateReadUpdateAndDeleteEmployee() {
        Employee employee = employee("John", "Smith", "AMP-100", LocalDate.of(2025, 1, 10));

        Employee saved = repository.saveAndFlush(employee);

        assertThat(saved.getId()).isNotNull();
        Employee found = repository.findById(saved.getId()).orElseThrow();
        assertThat(found.getFirstName()).isEqualTo("John");
        assertThat(found.getLastName()).isEqualTo("Smith");
        assertThat(found.getAmpNumber()).isEqualTo("AMP-100");
        assertThat(found.getHireDate()).isEqualTo(LocalDate.of(2025, 1, 10));

        found.setLastName("Taylor");
        repository.saveAndFlush(found);
        assertThat(repository.findById(saved.getId()).orElseThrow().getLastName())
                .isEqualTo("Taylor");

        repository.deleteById(saved.getId());
        repository.flush();
        assertThat(repository.findById(saved.getId())).isEmpty();
    }

    private Employee employee(String firstName, String lastName, String ampNumber, LocalDate hireDate) {
        Employee employee = new Employee();
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setAmpNumber(ampNumber);
        employee.setHireDate(hireDate);
        return employee;
    }
}
