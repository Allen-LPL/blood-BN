package cn.iocoder.yudao.module.infra.controller.admin.blood.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum MetricOpEnum implements ArrayValuable<Integer> {

    COUNT(1),
    COUNT_DISTINCT(2),
    SUM(3),
    AVG(4);

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MetricOpEnum::getCode).toArray(Integer[]::new);

    private final Integer code;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static MetricOpEnum valueOfCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (MetricOpEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

}
