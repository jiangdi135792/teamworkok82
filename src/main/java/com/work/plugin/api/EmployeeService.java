package com.work.plugin.api;

import com.atlassian.activeobjects.tx.Transactional;
import com.work.plugin.ao.StrEmployee;

import java.util.List;

@Transactional
    public interface EmployeeService {



    List<StrEmployee> getSubEmployee(String employeeKey);
    }
