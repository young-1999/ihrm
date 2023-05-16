package com.ihrm.system.client;

import com.ihrm.common.entity.Result;
import com.ihrm.domain.company.Department;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author yao
 * @Description 声明接口，通过feign调用其他微服务
 * @Date 14:57 2023/3/9
 * @Param null
 * @Return
**/
@FeignClient("ihrm-company")
public interface DepartmentFeignClient {
    /**
     * 调用微服务的接口
     */
    @GetMapping(value = "/company/department/{id}")
    public Result findById(@PathVariable(value = "id")String id);

    @PostMapping(value =("/company/department/search"))
    public Department findById(@RequestParam(value ="code")String code, @RequestParam(value ="companyId")String companyId);

}
