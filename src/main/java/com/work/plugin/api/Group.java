package com.work.plugin.api;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Group
{
    private final int id;
    private final String no;
    private final String name;
    private final String parent;
    private final int status;
    private final int type;
    private final String mappingCode;
    private final String memo;
    private final String owner;
    private final String createDate;
    private final String modifier;
    private final String modifierDate;
}
