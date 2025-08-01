package com.cagri.hrms.entity.core;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_leave_definitions")
public class LeaveDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;          // e.g. "Annual Leave", "Sick Leave"

    private Integer maxDays;      // max number of leave days allowed per year

    private boolean active;       // whether the leave type is currently available
}
