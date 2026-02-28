package cn.iocoder.yudao.module.infra.controller.admin.blood.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum SupplyGroupByFieldEnum implements ArrayValuable<Integer> {

    BLOOD_PRODUCT_NAME(10, "bloodProductName", "blood_product_name"),
    ABO(11, "abo", "abo"),
    RHD(12, "rhd", "rhd"),
    ISSUE_TYPE(13, "issueType", "issue_type"),
    ISSUING_ORG(14, "issuingOrg", "issuing_org"),
    RECEIVING_ORG(15, "receivingOrg", "receiving_org"),
    RECEIVING_ORG_ADMIN_REGION(16, "receivingOrgAdminRegion", "receiving_org_admin_region"),
    TIME_DAY(30, "timeDay", "DATE_FORMAT(issue_time, '%Y-%m-%d')"),
    TIME_MONTH(31, "timeMonth", "DATE_FORMAT(issue_time, '%Y-%m')");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(SupplyGroupByFieldEnum::getCode).toArray(Integer[]::new);

    private final Integer code;
    private final String alias;
    private final String expression;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static SupplyGroupByFieldEnum valueOfCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SupplyGroupByFieldEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

}
