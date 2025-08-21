package com.reliaquest.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.reliaquest.api.model.Employee;

import java.util.List;

public class EmployeeRestClientDTO{

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record EmployeeListResponse(List<Employee> data, String status){}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record EmployeeResponse(Employee data, String status){}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record DeleteEmployeeRequest(String name){}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record DeleteEmployeeResponse(Boolean data,String status){}
}
