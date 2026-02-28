package cn.iocoder.yudao.module.infra.controller.admin.blood;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.BloodSupplyFactAggReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.BloodSupplyFactAggRespVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.BloodSupplyFactPageReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.BloodSupplyFactRespVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.blood.BloodSupplyFactDO;
import cn.iocoder.yudao.module.infra.service.blood.BloodSupplyFactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 供血事实")
@RestController
@RequestMapping("/infra/blood-supply-fact")
@Validated
public class BloodSupplyFactController {

    @Resource
    private BloodSupplyFactService bloodSupplyFactService;

    @GetMapping("/page")
    @Operation(summary = "获得供血事实分页")
    @PreAuthorize("@ss.hasPermission('infra:blood-supply-fact:query')")
    public CommonResult<PageResult<BloodSupplyFactRespVO>> getBloodSupplyFactPage(@Valid BloodSupplyFactPageReqVO pageReqVO) {
        PageResult<BloodSupplyFactDO> pageResult = bloodSupplyFactService.getBloodSupplyFactPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, BloodSupplyFactRespVO.class));
    }

    @PostMapping("/aggregate")
    @Operation(summary = "供血事实聚合查询")
    @PreAuthorize("@ss.hasPermission('infra:blood-supply-fact:aggregate')")
    public CommonResult<BloodSupplyFactAggRespVO> aggregate(@Valid @RequestBody BloodSupplyFactAggReqVO reqVO) {
        return success(bloodSupplyFactService.aggregate(reqVO));
    }

}
