package com.aerometal.assignmentmanager.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.aerometal.assignmentmanager.dto.WorkOrderRegisterRequest;
import com.aerometal.assignmentmanager.entity.WorkOrderRegister;
import com.aerometal.assignmentmanager.exception.StaleEntityException;
import com.aerometal.assignmentmanager.service.WorkOrderRegisterService;

@WebMvcTest(WorkOrderRegisterController.class)
class WorkOrderRegisterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkOrderRegisterService service;

    @Test
    void outdatedRequestVersionShouldReturn409() throws Exception {
        when(service.update(
                eq(10L),
                any(WorkOrderRegisterRequest.class)
        )).thenThrow(
            new StaleEntityException(
                "WorkOrderRegister with id 10 "
                    + "was modified by another user."
            )
        );

        mockMvc.perform(
            put("/api/assignments/{id}", 10L)
                .contentType("application/json")
                .content("""
                    {
                      "employeeId": 1,
                      "componentId": 2,
                      "workOrderNumber": "WO-2026-001",
                      "workOrderDate": "2026-09-15",
                      "accessRightId": 3,
                      "version": 0
                    }
                    """)
        )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.error").value("Conflict"))
            .andExpect(
                jsonPath("$.message")
                    .value(
                        "WorkOrderRegister with id 10 "
                            + "was modified by another user."
                    )
            )
            .andExpect(
                jsonPath("$.path")
                    .value("/api/assignments/10")
            );
    }

    @Test
    void hibernateOptimisticLockFailureShouldReturn409()
            throws Exception {

        when(service.update(
                eq(10L),
                any(WorkOrderRegisterRequest.class)
        )).thenThrow(
            new ObjectOptimisticLockingFailureException(
                WorkOrderRegister.class,
                10L
            )
        );

        mockMvc.perform(
            put("/api/assignments/{id}", 10L)
                .contentType("application/json")
                .content("""
                    {
                      "employeeId": 1,
                      "componentId": 2,
                      "workOrderNumber": "WO-2026-001",
                      "workOrderDate": "2026-09-15",
                      "accessRightId": 3,
                      "version": 0
                    }
                    """)
        )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.error").value("Conflict"))
            .andExpect(
                jsonPath("$.message").value(
                    "This record was modified by another user. "
                        + "Reload the latest data before saving again."
                )
            )
            .andExpect(
                jsonPath("$.path")
                    .value("/api/assignments/10")
            );
    }
}
