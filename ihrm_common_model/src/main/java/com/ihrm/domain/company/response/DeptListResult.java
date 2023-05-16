package com.ihrm.domain.company.response;

import com.ihrm.domain.company.Company;
import com.ihrm.domain.company.Department;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
public class DeptListResult {
    private String companyId;
    private String companyName;
    private String companyManage;
    private List<Department> depts;

    public DeptListResult(Company company,List depts){
        this.companyId=company.getId();
        this.companyName=company.getName();
        this.companyManage=company.getLegalRepresentative();//公司联系人
        this.depts=depts;
    }
}
