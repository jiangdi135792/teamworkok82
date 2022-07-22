package com.work.plugin.api;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Organize
{
    private final int id;
    private final String name;
    private final String type;
    private final Integer parent;
    private final String status;
    private final Integer level;
    private final String mappingCode;
    private final Integer character;
    private final String memo;
    private final Integer owner;
    private final String createDate;
    private final Integer modifier;
    private final String modifierDate;
}
