package com.reliaquest.api.service;

import com.reliaquest.api.client.EmployeeRestApiService;
import com.reliaquest.api.dto.EmployeeRestClientDTO.*;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.dto.EmployeeRequest;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import com.reliaquest.api.dto.EmployeeRestClientDTO.EmployeeListResponse;

/**
 * @author Hussain Rana
 */
@Service
@Slf4j
public class EmployeeService {

    private final EmployeeRestApiService employeeRestApiService;
    private final CircuitBreaker circuitBreaker;

    public EmployeeService(EmployeeRestApiService employeeRestApiService, CircuitBreakerRegistry registry) {
        this.employeeRestApiService = employeeRestApiService;
        this.circuitBreaker = registry.circuitBreaker("employeeApiBreaker");
    }

    public List<Employee> getAllEmployees(){
        Supplier<List<Employee>> decoratedSupplier =
                CircuitBreaker.decorateSupplier(circuitBreaker, () -> {
                    EmployeeListResponse employeeDTO = employeeRestApiService.findAll();
                    return employeeDTO.data();
                });

        return decoratedSupplier.get();
    }

    public List<Employee> getAllEmployeesByName(String employeeNameSearch){
        List<Employee> employeeDTO = getAllEmployees();
        return employeeDTO.stream().filter(
                emp -> emp.getName().toLowerCase().contains(employeeNameSearch.toLowerCase()))
                .collect(Collectors.toList());
    }

    public Employee getEmployeeById(String id){
        Supplier<Employee> decoratedSupplier =
                CircuitBreaker.decorateSupplier(circuitBreaker, () -> {
                    EmployeeResponse employeeDTO = employeeRestApiService.findById(id);
                    return employeeDTO.data();
                });
     return decoratedSupplier.get();
    }

    public Integer getHighestSalaryOfEmployees(){
        List<Employee> employeeDTO =  getAllEmployees();
        return employeeDTO.stream().mapToInt(Employee::getSalary).max().getAsInt();
    }

    public List<String> getTopTenHighestEarningEmployeeNames(){
        List<Employee> employeeDTO =  getAllEmployees();
        return employeeDTO.stream()
                .sorted(Comparator.comparingInt(Employee::getSalary).reversed()).limit(10)
                .map(Employee::getName).toList();
    }

    public Employee createEmployee(EmployeeRequest employeeRequest){
        Supplier<Employee> decoratedSupplier =
                CircuitBreaker.decorateSupplier(circuitBreaker, () -> {
                    EmployeeResponse employeeDTO = employeeRestApiService.create(employeeRequest);
                    return employeeDTO.data();
                });
        return decoratedSupplier.get();
    }

    public String  deleteEmployeeById(String id){
        Employee emp = getEmployeeById(id);
        String empName = emp.getName();
        employeeRestApiService.delete(new DeleteEmployeeRequest(empName));
        return empName;
    }

}
