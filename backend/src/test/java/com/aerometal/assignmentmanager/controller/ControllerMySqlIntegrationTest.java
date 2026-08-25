package com.aerometal.assignmentmanager.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.aerometal.assignmentmanager.entity.AccessRight;
import com.aerometal.assignmentmanager.entity.Component;
import com.aerometal.assignmentmanager.entity.Employee;
import com.aerometal.assignmentmanager.repository.AccessRightRepository;
import com.aerometal.assignmentmanager.repository.ComponentRepository;
import com.aerometal.assignmentmanager.repository.EmployeeRepository;
import com.aerometal.assignmentmanager.repository.WorkOrdertRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ControllerMySqlIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired EmployeeRepository employeeRepository;
    @Autowired ComponentRepository componentRepository;
    @Autowired AccessRightRepository accessRightRepository;
    @Autowired WorkOrdertRepository workOrderRepository;

    @Test
    void employeeControllerShouldPerformCrudAgainstMySql() throws Exception {
        String suffix = suffix();
        MvcResult created = mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson("Test", "Employee", "AMP-" + suffix, "2025-01-10")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.ampNumber").value("AMP-" + suffix))
                .andReturn();
        long id = id(created);

        mockMvc.perform(get("/api/employees/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Test"));

        mockMvc.perform(put("/api/employees/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson("Updated", "Employee", "AMP-" + suffix, "2025-02-10")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.hireDate").value("2025-02-10"));

        mockMvc.perform(delete("/api/employees/{id}", id)).andExpect(status().isOk());
        assertThat(employeeRepository.existsById(id)).isFalse();
    }

    @Test
    void componentControllerShouldPerformCrudAgainstMySql() throws Exception {
        String name = "COMP-" + suffix();
        MvcResult created = mockMvc.perform(post("/api/components")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(namedEntityJson(name, "Test component")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name))
                .andReturn();
        long id = id(created);

        mockMvc.perform(get("/api/components/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Test component"));

        mockMvc.perform(put("/api/components/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(namedEntityJson(name, "Updated component")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated component"));

        mockMvc.perform(delete("/api/components/{id}", id)).andExpect(status().isOk());
        assertThat(componentRepository.existsById(id)).isFalse();
    }

    @Test
    void accessRightControllerShouldPerformCrudAgainstMySql() throws Exception {
        String name = "RIGHT-" + suffix();
        MvcResult created = mockMvc.perform(post("/api/accessrights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(namedEntityJson(name, "Test right")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name))
                .andReturn();
        long id = id(created);

        mockMvc.perform(get("/api/accessrights/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Test right"));

        mockMvc.perform(put("/api/accessrights/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(namedEntityJson(name, "Updated right")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated right"));

        mockMvc.perform(delete("/api/accessrights/{id}", id)).andExpect(status().isOk());
        assertThat(accessRightRepository.existsById(id)).isFalse();
    }

    @Test
    void workOrderControllerShouldPerformCrudAndExportAgainstMySql() throws Exception {
        String suffix = suffix();
        Employee employee = employeeRepository.save(employee("AMP-" + suffix));
        Component component = componentRepository.save(component("COMP-" + suffix));
        AccessRight right = accessRightRepository.save(right("RIGHT-" + suffix));

        String number = "WO-" + suffix;
        MvcResult created = mockMvc.perform(post("/api/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(workOrderJson(employee.getId(), component.getId(), right.getId(),
                                number, "2026-08-20")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workOrderNumber").value(number))
                .andExpect(jsonPath("$.employeeId").value(employee.getId()))
                .andReturn();
        long id = id(created);

        MvcResult list = mockMvc.perform(get("/api/assignments"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();
        JsonNode assignments = objectMapper.readTree(list.getResponse().getContentAsString());
        boolean containsCreatedAssignment = StreamSupport.stream(assignments.spliterator(), false)
                .anyMatch(node -> node.get("id").asLong() == id);
        assertThat(containsCreatedAssignment).isTrue();

        mockMvc.perform(put("/api/assignments/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(workOrderJson(employee.getId(), component.getId(), right.getId(),
                                number, "2026-08-21")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workOrderDate").value("2026-08-21"));

        mockMvc.perform(get("/api/assignments/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=workorder-management.xlsx"))
                .andExpect(content().contentType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(result -> assertThat(result.getResponse().getContentAsByteArray()).isNotEmpty());

        mockMvc.perform(delete("/api/assignments/{id}", id)).andExpect(status().isOk());
        assertThat(workOrderRepository.existsById(id)).isFalse();
    }

    @Test
    void employeeControllerShouldRejectInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson("", "Employee", "AMP-TEST", "2025-01-10")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void componentControllerShouldRejectBlankName() throws Exception {
        mockMvc.perform(post("/api/components")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(namedEntityJson("", "Invalid")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void accessRightControllerShouldRejectBlankName() throws Exception {
        mockMvc.perform(post("/api/accessrights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(namedEntityJson("", "Invalid")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void workOrderControllerShouldRejectMissingDate() throws Exception {
        String payload = """
                {"employeeId":1,"componentId":1,"workOrderNumber":"WO-INVALID","accessRightId":1}
                """;
        mockMvc.perform(post("/api/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    private long id(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private String employeeJson(String firstName, String lastName, String ampNumber, String hireDate)
            throws Exception {
        JsonNode node = objectMapper.createObjectNode()
                .put("firstName", firstName)
                .put("lastName", lastName)
                .put("ampNumber", ampNumber)
                .put("hireDate", hireDate);
        return objectMapper.writeValueAsString(node);
    }

    private String namedEntityJson(String name, String description) throws Exception {
        JsonNode node = objectMapper.createObjectNode()
                .put("name", name)
                .put("description", description);
        return objectMapper.writeValueAsString(node);
    }

    private String workOrderJson(Long employeeId, Long componentId, Long rightId,
            String number, String date) throws Exception {
        JsonNode node = objectMapper.createObjectNode()
                .put("employeeId", employeeId)
                .put("componentId", componentId)
                .put("accessRightId", rightId)
                .put("workOrderNumber", number)
                .put("workOrderDate", date);
        return objectMapper.writeValueAsString(node);
    }

    private String suffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private Employee employee(String ampNumber) {
        Employee employee = new Employee();
        employee.setFirstName("Test");
        employee.setLastName("Employee");
        employee.setAmpNumber(ampNumber);
        employee.setHireDate(LocalDate.of(2025, 1, 10));
        return employee;
    }

    private Component component(String name) {
        Component component = new Component();
        component.setName(name);
        component.setDescription("Test component");
        return component;
    }

    private AccessRight right(String name) {
        AccessRight right = new AccessRight();
        right.setName(name);
        right.setDescription("Test right");
        return right;
    }
}
