package com.work.plugin.util;

/**
 * Created by admin on 2021/6/27.
 */
public enum BranchCharacterEnum {
	COMMON_SUBCOMPANY(0, "一般子公司"),
	LEGAL_PERSON(1, "法人");

	private int id;
	private String name;

	BranchCharacterEnum(int id, String name) {
		this.id = id;
		this.name = name;
	}
}
