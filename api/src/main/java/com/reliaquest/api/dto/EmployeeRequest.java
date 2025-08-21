package com.reliaquest.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

/**
 * @author Hussain Rana  Created on  8/20/2025 at 3:27 PM
 */
@Data
@Builder
public class EmployeeRequest {

    @NotBlank
    private String name;

    @Positive
    @NotNull
    private Integer salary;

    @Min(16)
    @Max(75)
    @NotNull private Integer age;

    @NotBlank
    private String title;

    private String email;
}
