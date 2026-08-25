package com.aerometal.assignmentmanager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.aerometal.assignmentmanager.entity.WorkOrderRegister;

public interface WorkOrdertRepository extends JpaRepository<WorkOrderRegister, Long> {
	boolean existsByEmployeeIdAndComponentIdAndRightId(Long employeeId, Long componentId, Long rightId);

	@EntityGraph(attributePaths = { "employee", "component", "right" })
	List<WorkOrderRegister> findAllByOrderByIdAsc();
}
