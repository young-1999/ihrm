package com.ihrm.atte.controller;
/**
 * @Author yao
 * @Description
 * @Date 11:39 2023/3/25
 * @Param null
 * @Return
**/

import com.ihrm.atte.service.ConfigurationService;
import com.ihrm.common.controller.BaseController;
import com.ihrm.common.entity.Result;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.domain.atte.entity.AttendanceConfig;
import com.ihrm.domain.company.Department;
import org.apache.poi.util.StringUtil;
import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 配置考勤设置
 */
@RestController
@RequestMapping("/cfg")
public class ConfigController extends BaseController {
    String departmentId;
    @Autowired
    private ConfigurationService configurationService;
    /**
     * 获取考勤设置
     *  cfg/atte/item
     *  post
     *    参数 departmentId
     */
    @RequestMapping(value = "/atte/item" ,method = RequestMethod.POST)
    public Result atteConfig(@RequestBody Map dept) {
        //deptId = departmentId.substring(17,36);
        departmentId = (String)dept.get("departmentId");
        AttendanceConfig ac = configurationService.getAtteConfig(companyId,departmentId);
        return new Result(ResultCode.SUCCESS,ac);
    }

    /**
     * 保存考勤设置
     *  cfg/atte
     *  put
     *    参数 AttendanceConfig
     */
    @RequestMapping(value = "/atte" ,method = RequestMethod.PUT)
    public Result saveAtteConfig(@RequestBody AttendanceConfig attendanceConfig) {
        attendanceConfig.setCompanyId(companyId);
        attendanceConfig.setDepartmentId(departmentId);
        configurationService.saveAtteConfig(attendanceConfig);
        return new Result(ResultCode.SUCCESS);
    }

}
