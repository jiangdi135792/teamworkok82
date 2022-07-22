package com.work.plugin.validator;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 电话番号验证
 *
 * @author Masato Morita
 */
public class PhoneNumberValidator implements Validator
{
    /**
     * This pattern is intended for searching for things that look like they
     * might be phone numbers in arbitrary text, not for validating whether
     * something is in fact a phone number.  It will miss many things that
     * are legitimate phone numbers.
     *
     * <p> The pattern matches the following:
     * <ul>
     * <li>Optionally, a + sign followed immediately by one or more digits. Spaces, dots, or dashes
     * may follow.
     * <li>Optionally, sets of digits in parentheses, separated by spaces, dots, or dashes.
     * <li>A string starting and ending with a digit, containing digits, spaces, dots, and/or dashes.
     * </ul>
     *
     * cited from core.java.android.util.Patterns package
     */
    @SuppressWarnings("Annotator")
    private static final Pattern PHONE
            = Pattern.compile(                                  // sdd = space, dot, or dash
            "(\\+[0-9]+[\\- \\.]*)?"                            // +<digits><sdd>*
                    + "(\\([0-9]+\\)[\\- \\.]*)?"               // (<digits>)<sdd>*
                    + "([0-9][0-9\\- \\.][0-9\\- \\.]+[0-9])"); // <digit><digit|sdd>+<digit>

    /**
     * {@inheritDoc}
     */
    @Override
    public Pair<Boolean, ValidatorException> validate(@Nullable String value)
    {
        if (Objects.isNull(value))
            return ImmutablePair.of(null, new ValidatorException("value is null."));

        if (! PHONE.matcher(value).matches())
            return ImmutablePair.of(null, new ValidatorException("value is invalid."));

        return ImmutablePair.of(true, null);
    }
}
