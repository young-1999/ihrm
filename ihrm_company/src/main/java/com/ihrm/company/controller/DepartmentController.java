package com.ihrm.company.controller;

import com.ihrm.common.controller.BaseController;
import com.ihrm.common.entity.Result;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.company.service.CompanyService;
import com.ihrm.company.service.DepartmentService;
import com.ihrm.domain.company.Company;
import com.ihrm.domain.company.Department;
import com.ihrm.domain.company.response.DeptListResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//1.解决跨域
@CrossOrigin
//2.声明restController
@RestController
//3.设置父路径
@RequestMapping("/company")
public class DepartmentController extends BaseController {
    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private CompanyService companyService;

    /**
     * 保存
     */
    @PostMapping(value = "/department")
    public Result save(@RequestBody Department department){
        //1.设置保存的企业id
        department.setCompanyId(companyId);
        //2.调用service保存部门
        departmentService.save(department);
        //3.构造返回结果
        return new Result(ResultCode.SUCCESS);
    }

    /**
     *查询企业的部门列表
     * 指定企业id
     */
    @GetMapping(value = "/department")
    public Result findAll(){
        //1.指定企业id
        Company company = companyService.findById(companyId);
        //2.完成查询
        List<Department> list = departmentService.findAll(companyId);
        //3.构造返回结果
        DeptListResult deptListResult = new DeptListResult(company,list);
        return new Result(ResultCode.SUCCESS,deptListResult);
    }
    /**
     * 根据id查询department
     */
    @GetMapping(value = "/department/{id}")
    public Result findById(@PathVariable(value = "id")String id){
        Department department = departmentService.findById(id);
        return new Result(ResultCode.SUCCESS,department);
    }

    /**
     * 修改department
     */
    @PutMapping(value = "/department/{id}")
    public Result update(@PathVariable(value = "id")String id,@RequestBody Department department){
        //设置修改的部门id
        department.setId(id);
        //2.调用service更新
        departmentService.update(department);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 根据id删除
     */
    @DeleteMapping(value = "/department/{id}")
    public Result delete(@PathVariable(value = "id")String id){
        departmentService.deleteById(id);
        return new Result(ResultCode.SUCCESS);
    }

    @PostMapping(value =("/department/search"))
    public Department findById(@RequestParam(value ="code")String code,@RequestParam(value ="companyId")String companyId){
        Department dept=departmentService.findByCode(code,companyId);
        return dept;
    }

}
