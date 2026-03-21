package cn.iocoder.yudao.module.infra.controller.admin.blood.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 采血点坐标查询 Request VO")
@Data
public class BloodCollectionSiteCoordinateReqVO {

    @Schema(description = "运行机构名称（精确匹配）", example = "北京市红十字血液中心")
    private String operatingOrg;

    @Schema(description = "所属区（精确匹配）", example = "朝阳区")
    private String district;

    @Schema(description = "类型（采血点类型，精确匹配）", example = "献血车")
    private String type;
}

