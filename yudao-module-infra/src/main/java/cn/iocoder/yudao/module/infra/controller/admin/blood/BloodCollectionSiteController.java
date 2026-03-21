package cn.iocoder.yudao.module.infra.controller.admin.blood;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.BloodCollectionSiteCoordinateReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.BloodCollectionSiteCoordinateRespVO;
import cn.iocoder.yudao.module.infra.service.blood.BloodCollectionSiteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 采血点信息")
@RestController
@RequestMapping("/infra/blood-collection-site")
@Validated
@PermitAll
@TenantIgnore
public class BloodCollectionSiteController {

    @Resource
    private BloodCollectionSiteService bloodCollectionSiteService;

    @GetMapping("/coordinates")
    @Operation(summary = "获取正在运行的采血点坐标数据", description = "根据运行机构或所属区精确匹配，查询state=1的采血点坐标")
    public CommonResult<BloodCollectionSiteCoordinateRespVO> getActiveCollectionSiteCoordinates(
            @Valid BloodCollectionSiteCoordinateReqVO reqVO) {
        return success(bloodCollectionSiteService.getActiveCollectionSiteCoordinates(reqVO.getOperatingOrg(),
                reqVO.getDistrict(), reqVO.getType()));
    }

}
