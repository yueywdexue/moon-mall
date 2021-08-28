package com.yueyedexue.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 * @description:  ListValue 0, 1 校验器
 * @author: MoonNightSnow
 * @createTime: 2021/7/30 10:21
 **/
public class ListValueConstraintValidatorForInt implements ConstraintValidator<ListValue, Integer> {

    private HashSet<Integer> set = new HashSet<>();

    @Override
    public void initialize(ListValue constraintAnnotation) {
        int[] value = constraintAnnotation.value();
        for (int val : value) {
            set.add(val);
        }
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return set.contains(value);
    }
}
