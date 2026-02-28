package cn.iocoder.yudao.module.infra.controller.admin.blood.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum MetricFieldEnum implements ArrayValuable<Integer> {

    STAR(1, "*") ,
    DONATION_CODE(2, "donation_code"),
    ARCHIVE_ID(3, "archive_id"),
    BASE_UNIT_VALUE(4, "base_unit_value"),
    AGE(5, "age");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MetricFieldEnum::getCode).toArray(Integer[]::new);

    private final Integer code;
    private final String column;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static MetricFieldEnum valueOfCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (MetricFieldEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

}
