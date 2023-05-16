package com.ihrm.company.dao;

import com.ihrm.domain.company.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
/**
 * @Author yao
 * @Description 部门dao接口
 * @Date 10:12 2022/12/4
 * @Param null
 * @Return
**/
public interface DepartmentDao extends JpaRepository<Department,String>, JpaSpecificationExecutor<Department> {
    Department findByCodeAndCompanyId(String code, String companyId);
}
