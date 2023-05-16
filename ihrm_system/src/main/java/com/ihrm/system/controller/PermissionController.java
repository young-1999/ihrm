package com.ihrm.system.controller;

import com.ihrm.common.entity.PageResult;
import com.ihrm.common.entity.Result;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.common.exception.CommonException;
import com.ihrm.domain.system.Permission;
import com.ihrm.system.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @Author yao
 * @Description
 * @Date 10:49 2022/12/9
 * @Param null
 * @Return
**/
//1.解决跨域
@CrossOrigin
//2.声明restController
@RestController
//3.设置父路径
@RequestMapping("/sys")
public class PermissionController {
    @Autowired
    private PermissionService permissionService;

    /**
     * 保存
     */
    @PostMapping(value = "/permission")
    public Result save(@RequestBody Map<String,Object> map) throws Exception {
        permissionService. save(map);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 修改
     */
    @PutMapping(value = "/permission/{id}")
    public Result update(@PathVariable(value = "id")String id, @RequestBody Map<String,Object> map) throws Exception {
        //构造id
        map.put("id",id);
        permissionService.update(map);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     *查询列表
     */
    @GetMapping(value = "/permission")
    public Result findAll(@RequestParam Map<String,Object> map){
        List<Permission> list= permissionService.findAll(map);
        return new Result(ResultCode.SUCCESS,list);
    }
    /**
     * 根据id查询
     */
    @GetMapping(value = "/permission/{id}")
    public Result findById(@PathVariable(value = "id")String id) throws CommonException {
        Map map=permissionService.findById(id);
        return new Result(ResultCode.SUCCESS,map);
    }


    /**
     * 根据id删除
     */
    @DeleteMapping(value = "/permission/{id}")
    public Result delete(@PathVariable(value = "id")String id) throws CommonException {
        permissionService.deleteById(id);
        return new Result(ResultCode.SUCCESS);
    }

}
