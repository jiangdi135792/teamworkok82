package com.work.plugin.util;

import lombok.Data;

/**
 * Created by admin on 2021/6/22.
 */
@Data
public class Entity<T> {
	private String id;
	private String parentId;
	private T node;
}
