package com.work.plugin.api;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Employee
{
    private final int id;
    private final String email;
    private final String name;
    private final String no;
    private final String jiraUserName;
    private final String jiraUserKey;
    private final int jiraId;
    private final String sex;
    private final String phone;
    private final String otherPhone;
    private final String status;
    private final String entryTime;
    private final String leaveTime;
    private final String memo;
    private final String owner;
    private final String createDate;
    private final String modifier;
    private final String modifierDate;
}
