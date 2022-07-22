package com.work.plugin.impl;

import com.work.plugin.ao.StrEmployee;
import com.work.plugin.ao.StrEmployeeService;
import com.work.plugin.api.EmployeeService;
import lombok.RequiredArgsConstructor;

import java.util.List;

//import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
	private final StrEmployeeService StrEmployeeService;



public 	List<StrEmployee> getSubEmployee(String employeeKey)
	{
		Integer employeeId;
		StrEmployee strEmployee=	StrEmployeeService.getByJiraUserKey(employeeKey);
		if(strEmployee!=null) {
			return StrEmployeeService.getSubEmployee(strEmployee.getJiraId());
		}
		else
		{	return null;
		}
	}



}
