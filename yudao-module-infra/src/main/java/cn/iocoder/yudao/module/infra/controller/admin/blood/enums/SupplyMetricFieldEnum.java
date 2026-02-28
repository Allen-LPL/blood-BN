package cn.iocoder.yudao.module.infra.controller.admin.blood.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum SupplyMetricFieldEnum implements ArrayValuable<Integer> {

    STAR(1, "*"),
    DONATION_CODE(2, "donation_code"),
    PRODUCT_CODE(3, "product_code"),
    BASE_UNIT_VALUE(4, "base_unit_value");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(SupplyMetricFieldEnum::getCode).toArray(Integer[]::new);

    private final Integer code;
    private final String column;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static SupplyMetricFieldEnum valueOfCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SupplyMetricFieldEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

}
