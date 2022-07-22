package com.work.plugin.view;

import lombok.RequiredArgsConstructor;

/**
 * Created by admin on 2021/6/21.
 */
@RequiredArgsConstructor
public class ViewStrOrganizeBean {
	private  String name;
	private  String type;
	private	  String status;
	private	  String character;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCharacter() {
		return character;
	}

	public void setCharacter(String character) {
		this.character = character;
	}
}
