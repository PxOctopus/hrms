package com.cagri.hrms.entity.expense;

import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.entity.employee.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_assignments",
        uniqueConstraints = @UniqueConstraint(name = "uq_project_employee", columnNames = {"project_id","employee_id"}),
        indexes = @Index(name="ix_project_assignment_project", columnList="project_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectAssignment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Employee employee;

    private boolean accepted = false;

    private LocalDateTime assignedAt;
    private LocalDateTime respondedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_user_id")
    private User assignedBy;

    @PrePersist void prePersist() {
        this.assignedAt = LocalDateTime.now();
    }
}
