package com.reliaquest.api.service;

import com.reliaquest.api.model.Employee;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author Hussain Rana
 */
public class EmployeeDataUtil {

    public static List<Employee> getEmployeesList(){
        return Arrays.asList(
                new Employee(UUID.randomUUID(), "Tom", 5000,24,"Lead Eng","test@test.com"),
                new Employee(UUID.randomUUID(), "Jerry", 5001,25,"Lead Eng","test@test2.com")
        );
    }

    public static List<Employee> getEmployeesList(Integer highestSalary){
        return Arrays.asList(
                new Employee(UUID.randomUUID(), "Tom", 5000,24,"Lead Eng","test@test.com"),
                new Employee(UUID.randomUUID(), "Jerry", highestSalary,25,"Lead Eng","test@test2.com")
        );
    }

}
