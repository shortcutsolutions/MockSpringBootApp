package com.reliaquest.api.service;

import com.reliaquest.api.client.EmployeeRestApiService;
import com.reliaquest.api.dto.EmployeeRestClientDTO.*;
import com.reliaquest.api.model.Employee;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import com.reliaquest.api.dto.EmployeeRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EmployeeServiceTest {

    private static final String SUCCESS_MESSAGE = "Request proceeded successfully";

    private EmployeeRestApiService employeeRestApiService;
    private CircuitBreakerRegistry circuitBreakerRegistry;
    private CircuitBreaker circuitBreaker;
    private EmployeeService employeeService;


    @BeforeEach
    void setUp() {
        employeeRestApiService = mock(EmployeeRestApiService.class);
        circuitBreakerRegistry = mock(CircuitBreakerRegistry.class);

        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(2)
                .minimumNumberOfCalls(2)
                .failureRateThreshold(100)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .build();

        circuitBreaker = CircuitBreaker.of("employeeApiBreaker", config);

        when(circuitBreakerRegistry.circuitBreaker("employeeApiBreaker"))
                .thenReturn(circuitBreaker);

        employeeService = new EmployeeService(employeeRestApiService, circuitBreakerRegistry);
    }


    // Positive cases

    @Test
    public void testGetAllEmployees() {
        List<Employee> employees = EmployeeDataUtil.getEmployeesList();

        when(employeeRestApiService.findAll()).thenReturn(new EmployeeListResponse(employees,SUCCESS_MESSAGE));

        List<Employee> result = employeeService.getAllEmployees();

        assertEquals(2, result.size());
        Mockito.verify(employeeRestApiService, Mockito.times(1)).findAll();
    }

    @Test
    public void testGetAllEmployeesByName() {
        List<Employee> employees = EmployeeDataUtil.getEmployeesList();
        when(employeeRestApiService.findAll()).thenReturn(new EmployeeListResponse(employees,SUCCESS_MESSAGE));

        List<Employee> result = employeeService.getAllEmployeesByName("to");

        assertEquals(1, result.size());
        assertEquals("Tom", result.get(0).getName());
    }

    @Test
    public void testGetEmployeeById() {
        UUID id = UUID.randomUUID();
        Employee emp = new Employee(id, "Tom", 5000,24,"Lead Eng","test@test.com");
        when(employeeRestApiService.findById(id.toString())).thenReturn(new EmployeeResponse(emp,SUCCESS_MESSAGE));

        Employee result = employeeService.getEmployeeById(id.toString());

        Assertions.assertNotNull(result);
        assertEquals("Tom", result.getName());
    }

    @Test
    public void testGetHighestSalaryOfEmployees() {
        List<Employee> employees = EmployeeDataUtil.getEmployeesList(7000);
        when(employeeRestApiService.findAll()).thenReturn(new EmployeeListResponse(employees,SUCCESS_MESSAGE));

        int highestSalary = employeeService.getHighestSalaryOfEmployees();

        assertEquals(7000, highestSalary);
    }

    @Test
    public void testGetTopTenHighestEarningEmployeeNames() {
        List<Employee> employees = EmployeeDataUtil.getEmployeesList();
        when(employeeRestApiService.findAll()).thenReturn(new EmployeeListResponse(employees,SUCCESS_MESSAGE));

        List<String> topEarning = employeeService.getTopTenHighestEarningEmployeeNames();

        assertEquals(Arrays.asList("Jerry", "Tom"), topEarning);
    }

    @Test
    public void testCreateEmployee() {
        EmployeeRequest req = EmployeeRequest.builder()
                .name("Thomas")
                .age(25)
                .salary(786110)
                .title("Sr Product Manager")
                .build();
        Employee emp = new Employee(UUID.randomUUID(), "Thomas", 786110,25,"Sr Product Manager","test@test132.com");

        when(employeeRestApiService.create(req)).thenReturn(new EmployeeResponse(emp,SUCCESS_MESSAGE));

        Employee result = employeeService.createEmployee(req);

        Assertions.assertNotNull(result);
        assertEquals("Thomas", result.getName());
    }

    @Test
    public void testDeleteEmployeeById() {
        UUID id = UUID.randomUUID();
        Employee emp = new Employee(id, "Thomas", 786110,25,"Sr Product Manager","test@test132.com");
        when(employeeRestApiService.findById(id.toString())).thenReturn(new EmployeeResponse(emp,SUCCESS_MESSAGE));

        String deletedName = employeeService.deleteEmployeeById(id.toString());

        assertEquals("Thomas", deletedName);

        ArgumentCaptor<DeleteEmployeeRequest> captor = ArgumentCaptor.forClass(DeleteEmployeeRequest.class);
        Mockito.verify(employeeRestApiService).delete(captor.capture());
        assertEquals("Thomas", captor.getValue().name());
    }

    // Negative CASES

    @Test
    public void testGetAllEmployees_whenApiThrowsException() {
        when(employeeRestApiService.findAll()).thenThrow(new RestClientResponseException(
                "HTTP error: " + HttpStatus.BAD_GATEWAY,
                HttpStatus.BAD_GATEWAY.value(),
                "Bad gateway",
                null,
                null,
                null
        ));

        RestClientResponseException ex = Assertions.assertThrows(RestClientResponseException.class,
                () -> employeeService.getAllEmployees());

        assertEquals("HTTP error: " + HttpStatus.BAD_GATEWAY, ex.getMessage());
    }

    @Test
    public void testGetEmployeeById_whenApiThrowsException() {
        UUID id = UUID.randomUUID();
        when(employeeRestApiService.findById(id.toString())).thenThrow(new RestClientResponseException(
                "HTTP error: " + HttpStatus.NOT_FOUND,
                HttpStatus.NOT_FOUND.value(),
                "No record found",
                null,
                null,
                null
        ));
        RestClientResponseException ex = Assertions.assertThrows(RestClientResponseException.class,
                () -> employeeService.getEmployeeById(id.toString()));

        assertEquals("HTTP error: " + HttpStatus.NOT_FOUND, ex.getMessage());
    }


    @Test
    public void testCircuitBreakerTrips() {
        // Cause multiple failures to trip the breaker
        when(employeeRestApiService.findAll()).thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS,HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase()));

        Assertions.assertThrows(HttpClientErrorException.class, () -> employeeService.getAllEmployees());
        Assertions.assertThrows(HttpClientErrorException.class, () -> employeeService.getAllEmployees());

        // CircuitBreaker should now be OPEN
        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());
    }
}
