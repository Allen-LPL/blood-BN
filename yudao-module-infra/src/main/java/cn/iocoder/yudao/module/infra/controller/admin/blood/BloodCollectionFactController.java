package cn.iocoder.yudao.module.infra.controller.admin.blood;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.BloodCollectionFactAggReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.BloodCollectionFactAggRespVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.BloodCollectionFactPageReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.blood.vo.BloodCollectionFactRespVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.blood.BloodCollectionFactDO;
import cn.iocoder.yudao.module.infra.service.blood.BloodCollectionFactService;
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
@RequestMapping("/infra/blood-collection-fact")
@Validated
public class BloodCollectionFactController {

    @Resource
    private BloodCollectionFactService bloodCollectionFactService;

    @GetMapping("/page")
    @Operation(summary = "获得供血事实分页")
    @PreAuthorize("@ss.hasPermission('infra:blood-collection-fact:query')")
    public CommonResult<PageResult<BloodCollectionFactRespVO>> getBloodCollectionFactPage(@Valid BloodCollectionFactPageReqVO pageReqVO) {
        PageResult<BloodCollectionFactDO> pageResult = bloodCollectionFactService.getBloodCollectionFactPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, BloodCollectionFactRespVO.class));
    }

    @PostMapping("/aggregate")
    @Operation(summary = "供血事实聚合查询")
    @PreAuthorize("@ss.hasPermission('infra:blood-collection-fact:aggregate')")
    public CommonResult<BloodCollectionFactAggRespVO> aggregate(@Valid @RequestBody BloodCollectionFactAggReqVO reqVO) {
        return success(bloodCollectionFactService.aggregate(reqVO));
    }

}
