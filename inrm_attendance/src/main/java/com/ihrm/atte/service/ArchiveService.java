package com.ihrm.atte.service;

import com.alibaba.fastjson.JSONObject;
import com.ihrm.atte.dao.*;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.common.exception.CommonException;
import com.ihrm.common.utils.IdWorker;
import com.ihrm.domain.atte.entity.ArchiveMonthly;
import com.ihrm.domain.atte.entity.ArchiveMonthlyInfo;
import com.ihrm.domain.atte.bo.AtteReportMonthlyBO;
import com.ihrm.domain.atte.vo.ArchiveInfoVO;
import com.ihrm.domain.atte.vo.ArchiveItemVO;
import com.ihrm.domain.atte.vo.ArchiveVO;
import com.ihrm.domain.atte.vo.ReportVO;
import com.ihrm.domain.system.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
public class ArchiveService {

	@Autowired
	private AttendanceDao attendanceDao;

	@Autowired
	private ArchiveMonthlyDao atteArchiveMonthlyDao;

	@Autowired
	private ArchiveMonthlyInfoDao archiveMonthlyInfoDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private IdWorker idWorkker;

	//数据归档
	public void saveArchive(String archiveDate,String companyId) {

		//1.查询所有企业用户
		List<User> users = userDao.findByCompanyId(companyId);

		//1.保存归档主表数据
		ArchiveMonthly archiveMonthly = new ArchiveMonthly();
		archiveMonthly.setId(idWorkker.nextId()+"");
		archiveMonthly.setCompanyId(companyId);
		archiveMonthly.setArchiveYear(archiveDate.substring(0,4)); //201908
		archiveMonthly.setArchiveMonth(archiveDate.substring(4));


		//2.保存归档明细表数据
		for (User user : users) {
			ArchiveMonthlyInfo info = new ArchiveMonthlyInfo(user);
			//统计每个用户的考勤记录
			Map map = attendanceDao.statisByUser(user.getId(),archiveDate +"%");
			info.setStatisData(map);
			info.setId(idWorkker.nextId()+"");
			info.setAtteArchiveMonthlyId(archiveMonthly.getId());
			archiveMonthlyInfoDao.save(info);
		}

		//总人数
		archiveMonthly.setTotalPeopleNum(users.size());
		archiveMonthly.setFullAttePeopleNum(users.size());
		archiveMonthly.setIsArchived(0);

		atteArchiveMonthlyDao.save(archiveMonthly);
	}
}
