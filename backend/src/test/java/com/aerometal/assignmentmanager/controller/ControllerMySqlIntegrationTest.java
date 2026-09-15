package com.aerometal.assignmentmanager.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.node.ObjectNode;

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
import com.aerometal.assignmentmanager.repository.WorkOrderRegisterRepository;
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
    @Autowired WorkOrderRegisterRepository workOrderRepository;

    @Test
    void employeeControllerShouldPerformCrudAgainstMySql()
            throws Exception {

        String suffix = suffix();

        MvcResult created = mockMvc.perform(
            post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(employeeJson(
                    "Test",
                    "Employee",
                    "AMP-" + suffix,
                    "2025-01-10",
                    null
                ))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.version").isNumber())
            .andExpect(
                jsonPath("$.ampNumber")
                    .value("AMP-" + suffix)
            )
            .andReturn();

        long id = id(created);
        long currentVersion = version(created);

        mockMvc.perform(
            get("/api/employees/{id}", id)
        )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.firstName").value("Test")
            );

        mockMvc.perform(
            put("/api/employees/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(employeeJson(
                    "Updated",
                    "Employee",
                    "AMP-" + suffix,
                    "2025-02-10",
                    currentVersion
                ))
        )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.firstName").value("Updated")
            )
            .andExpect(
                jsonPath("$.hireDate").value("2025-02-10")
            )
            .andExpect(
                jsonPath("$.version")
                    .value(currentVersion + 1)
            );

        mockMvc.perform(
            delete("/api/employees/{id}", id)
        )
            .andExpect(status().isOk());

        assertThat(
            employeeRepository.existsById(id)
        ).isFalse();
    }
    
    @Test
    void componentControllerShouldPerformCrudAgainstMySql()
            throws Exception {

        String name = "COMP-" + suffix();

        MvcResult created = mockMvc.perform(
            post("/api/components")
                .contentType(MediaType.APPLICATION_JSON)
                .content(namedEntityJson(
                    name,
                    "Test component",
                    null
                ))
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value(name))
            .andExpect(jsonPath("$.version").isNumber())
            .andReturn();

        long id = id(created);
        long currentVersion = version(created);

        mockMvc.perform(
            get("/api/components/{id}", id)
        )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.description")
                    .value("Test component")
            );

        MvcResult updated = mockMvc.perform(
            put("/api/components/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(namedEntityJson(
                    name,
                    "Updated component",
                    currentVersion
                ))
        )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.description")
                    .value("Updated component")
            )
            .andExpect(
                jsonPath("$.version")
                    .value(currentVersion + 1)
            )
            .andReturn();

        long updatedVersion = version(updated);

        mockMvc.perform(
            delete("/api/components/{id}", id)
                .queryParam(
                    "version",
                    Long.toString(updatedVersion)
                )
        )
            .andExpect(status().isNoContent());

        assertThat(
            componentRepository.existsById(id)
        ).isFalse();
    }

    @Test
    void accessRightControllerShouldPerformCrudAgainstMySql()
            throws Exception {

        String name = "RIGHT-" + suffix();

        MvcResult created = mockMvc.perform(
            post("/api/accessrights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(namedEntityJson(
                    name,
                    "Test right",
                    null
                ))
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value(name))
            .andExpect(jsonPath("$.version").isNumber())
            .andReturn();

        long id = id(created);
        long currentVersion = version(created);

        mockMvc.perform(
            get("/api/accessrights/{id}", id)
        )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.description").value("Test right")
            );

        mockMvc.perform(
            put("/api/accessrights/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(namedEntityJson(
                    name,
                    "Updated right",
                    currentVersion
                ))
        )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.description")
                    .value("Updated right")
            )
            .andExpect(
                jsonPath("$.version")
                    .value(currentVersion + 1)
            );

        mockMvc.perform(
            delete("/api/accessrights/{id}", id)
        )
            .andExpect(status().isNoContent());

        assertThat(
            accessRightRepository.existsById(id)
        ).isFalse();
    }

    @Test
    void workOrderControllerShouldPerformCrudAndExportAgainstMySql() throws Exception {
        String suffix = suffix();
        Employee employee = employeeRepository.save(employee("AMP-" + suffix));
        Component component = componentRepository.save(component("COMP-" + suffix));
        AccessRight right = accessRightRepository.save(right("RIGHT-" + suffix));

        String number = "WO-" + suffix;
        MvcResult created = mockMvc.perform(
        	    post("/api/assignments")
        	        .contentType(MediaType.APPLICATION_JSON)
        	        .content(workOrderJson(
        	            employee.getId(),
        	            component.getId(),
        	            right.getId(),
        	            number,
        	            "2026-08-20",
        	            null
        	        ))
        	)
        	    .andExpect(status().isOk())
        	    .andExpect(
        	        jsonPath("$.workOrderNumber").value(number)
        	    )
        	    .andExpect(
        	        jsonPath("$.employeeId").value(employee.getId())
        	    )
        	    .andExpect(jsonPath("$.version").isNumber())
        	    .andReturn();

        	long id = id(created);
        	long currentVersion = version(created);

        MvcResult list = mockMvc.perform(get("/api/assignments"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();
        JsonNode assignments = objectMapper.readTree(list.getResponse().getContentAsString());
        boolean containsCreatedAssignment = StreamSupport.stream(assignments.spliterator(), false)
                .anyMatch(node -> node.get("id").asLong() == id);
        assertThat(containsCreatedAssignment).isTrue();

        mockMvc.perform(
        	    put("/api/assignments/{id}", id)
        	        .contentType(MediaType.APPLICATION_JSON)
        	        .content(workOrderJson(
        	            employee.getId(),
        	            component.getId(),
        	            right.getId(),
        	            number,
        	            "2026-08-21",
        	            currentVersion
        	        ))
        	)
        	    .andExpect(status().isOk())
        	    .andExpect(
        	        jsonPath("$.workOrderDate")
        	            .value("2026-08-21")
        	    )
        	    .andExpect(
        	        jsonPath("$.version")
        	            .value(currentVersion + 1)
        	    );

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
                        .content(employeeJson("", "Employee", "AMP-TEST", "2025-01-10", null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void componentControllerShouldRejectBlankName() throws Exception {
        mockMvc.perform(post("/api/components")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(namedEntityJson("", "Invalid", null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void accessRightControllerShouldRejectBlankName()
            throws Exception {

        mockMvc.perform(
            post("/api/accessrights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(namedEntityJson(
                    "",
                    "Invalid",
                    null
                ))
        )
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.message")
                    .value("Validation failed")
            );
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
    
    private long version(MvcResult result) throws Exception {
        return objectMapper
                .readTree(
                    result.getResponse().getContentAsString()
                )
                .get("version")
                .asLong();
    }

    private String employeeJson(String firstName, String lastName, String ampNumber, String hireDate, Long version)
            throws Exception {
    	 ObjectNode node = objectMapper.createObjectNode();

    	    node.put("firstName", firstName);
    	    node.put("lastName", lastName);
    	    node.put("ampNumber", ampNumber);
    	    node.put("hireDate", hireDate);

    	    if (version != null) {
    	        node.put("version", version);
    	    }

    	    return objectMapper.writeValueAsString(node);
    }

    private String namedEntityJson(String name, String description, Long version) throws Exception {
    	ObjectNode node = objectMapper.createObjectNode();

        node.put("name", name);
        node.put("description", description);

        if (version != null) {
            node.put("version", version);
        }

        return objectMapper.writeValueAsString(node);
    }

    private String workOrderJson(
            Long employeeId,
            Long componentId,
            Long rightId,
            String number,
            String date,
            Long version
    ) throws Exception {

        ObjectNode node = objectMapper.createObjectNode();

        node.put("employeeId", employeeId);
        node.put("componentId", componentId);
        node.put("accessRightId", rightId);
        node.put("workOrderNumber", number);
        node.put("workOrderDate", date);

        if (version != null) {
            node.put("version", version);
        }

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
