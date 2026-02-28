package cn.iocoder.yudao.module.infra.controller.admin.blood.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum GroupByFieldEnum implements ArrayValuable<Integer> {

    COLLECTION_DEPARTMENT(10, "collectionDepartment", "collection_department"),
    COLLECTION_SITE(11, "collectionSite", "collection_site"),
    ORGANIZATION_MODE(12, "organizationMode", "organization_mode"),
    DONATION_TYPE(13, "donationType", "donation_type"),
    GENDER(14, "gender", "gender"),
    UNIT_ADMIN_REGION(15, "unitAdminRegion", "unit_admin_region"),
    SYSTEM_NAME(16, "systemName", "system_name"),
    UNIT_PROPERTY(17, "unitProperty", "unit_property"),
    UNIT_LEVEL(18, "unitLevel", "unit_level"),
    FULL_VOLUME_FLAG(19, "fullVolumeFlag", "full_volume_flag"),
    ARCHIVE_BLOOD_TYPE(20, "archiveBloodType", "archive_blood_type"),
    PRECHECK_BLOOD_TYPE(21, "precheckBloodType", "precheck_blood_type"),
    TIME_DAY(30, "timeDay", "DATE_FORMAT(blood_collection_time, '%Y-%m-%d')"),
    TIME_MONTH(31, "timeMonth", "DATE_FORMAT(blood_collection_time, '%Y-%m')");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(GroupByFieldEnum::getCode).toArray(Integer[]::new);

    private final Integer code;
    private final String alias;
    private final String expression;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static GroupByFieldEnum valueOfCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (GroupByFieldEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

}
