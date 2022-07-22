package com.work.plugin.util.exception;

/**
 * 列的唯一值约束
 * Created by admin on 2021/7/17.
 */
public class DuplicateKeyException extends Exception {
	public DuplicateKeyException(String msg) {
		super(msg);
	}

	public DuplicateKeyException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
