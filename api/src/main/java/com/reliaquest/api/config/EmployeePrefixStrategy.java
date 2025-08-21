package com.reliaquest.api.config;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;

/**
 * @author Hussain Rana
 */
public class EmployeePrefixStrategy extends PropertyNamingStrategies.NamingBase {
    @Override
    public String translate(String propertyName) {
        if ("id".equals(propertyName)) {
            return propertyName; // don't prefix id
        }
        return "employee_" + propertyName;
    }
}
