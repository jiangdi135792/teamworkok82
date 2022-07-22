package com.work.plugin.validator;

/**
 * 验证例外
 *
 * @author Masato Morita
 */
public class ValidatorException extends RuntimeException
{
    public ValidatorException(String message)
    {
        super(message);
    }
}
