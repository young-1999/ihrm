package com.ihrm.company;

import com.ihrm.company.dao.CompanyDao;
import com.ihrm.domain.company.Company;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class CompanyDaoTest {
    @Autowired
    private CompanyDao companyDao;

    @Test
    public void test(){
        //companyDao.save(company);//保存或更新
        //companyDao.deleteById(11);//根据id删除数据
        //companyDao.findById("1");根据id查询
        //companyDao.findAll();查询全部数据
        Company company = companyDao.findById("1").get();
        System.out.println(company);
    }

}
