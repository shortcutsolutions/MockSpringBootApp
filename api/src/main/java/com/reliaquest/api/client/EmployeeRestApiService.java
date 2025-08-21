package com.reliaquest.api.client;

import com.reliaquest.api.dto.EmployeeRestClientDTO.*;
import com.reliaquest.api.dto.EmployeeRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * @author Hussain Rana
 */
@HttpExchange(url = "api/v1/employee", accept = "application/json", contentType = "application/json")
public interface EmployeeRestApiService {

    @GetExchange()
    EmployeeListResponse findAll();

    @GetExchange("/{id}")
    EmployeeResponse findById(@PathVariable("id")  String id);

    @PostExchange()
    EmployeeResponse create(@RequestBody EmployeeRequest employeeRequest);

    @DeleteExchange()
    DeleteEmployeeResponse delete(@RequestBody DeleteEmployeeRequest name);
}
