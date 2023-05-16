package com.ihrm.atte.service;

import com.alibaba.fastjson.JSONObject;
import com.ihrm.atte.dao.*;
import com.ihrm.common.entity.PageResult;
import com.ihrm.common.entity.Result;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.common.exception.CommonException;
import com.ihrm.common.utils.DateUtil;
import com.ihrm.common.utils.IdWorker;
import com.ihrm.domain.atte.bo.*;
import com.ihrm.domain.atte.entity.*;
import com.ihrm.domain.atte.enums.DeductionEnum;
import com.ihrm.domain.atte.vo.AttePageVO;
import com.ihrm.domain.atte.vo.AtteSalaryStatisticsVO;
import com.ihrm.domain.employee.EmployeeArchive;
import com.ihrm.domain.system.User;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AtteService  {

	@Autowired
	private IdWorker idWorker;

    @Autowired
    private AttendanceDao attendanceDao;

    @Autowired
    private DeductionDictDao deductionDictDao;
    @Autowired
	private ArchiveMonthlyDao archiveMonthlyDao;

    @Autowired
	private ArchiveMonthlyInfoDao archiveMonthlyInfoDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private AttendanceConfigDao attendanceConfigDao;

    @Autowired
    private CompanySettingsDao companySettingsDao;

	/**
	 * 获取用户的考勤数据
	 *      1.考勤月
	 *      2.分页
	 *      3.查询
	 */
	public Map getAtteDate(String companyId, int page, int pageSize) throws ParseException {
		//1.考勤月
		CompanySettings settings = companySettingsDao.findById(companyId).get();
		String dataMonth = settings.getDataMonth();
		//2.分页查询用户
		Page<User> page1 = userDao.findPage(companyId, new PageRequest(page - 1, pageSize));
		List<AtteItemBO> list = new ArrayList<>();
		//3.循环所有的用户,获取每个用户每天的考勤情况
		for(User user : page1.getContent()) {
			AtteItemBO bo = new AtteItemBO();
			BeanUtils.copyProperties(user,bo);
			List<Attendance> attendanceRecord = new ArrayList<>();
			//获取当前月所有的天数
			String[] days = DateUtil.getDaysByYearMonth(dataMonth);//传递20230201,
			//循环每天查询考勤记录
			for (String day : days) {
				Attendance attendance = attendanceDao.findByUserIdAndDay(user.getId(), day);
				if(attendance == null) {
					attendance = new Attendance();
					attendance.setAdtStatu(2); //如果当天不存在考勤记录,旷工
					attendance.setId(user.getId());
					attendance.setDay(day);
				}
				attendanceRecord.add(attendance);
			}
			//封装到attendanceRecord
			bo.setAttendanceRecord(attendanceRecord);
			list.add(bo);
		}

		Map map = new HashMap();
		//1.数据,分页对象
		PageResult pr = new PageResult(page1.getTotalElements(),list);
		map.put("data",pr);
		//2.待处理的考勤数量
		map.put("tobeTaskCount",0);
		//3.当前考勤的月份
		int i = Integer.parseInt(dataMonth.substring(4));
		map.put("monthOfReport",i);
		return map;
	}

	//编辑考勤
	public void editAtte(Attendance attendance) {
		//1.查询考勤是否存在,更新
		Attendance vo = attendanceDao.findByUserIdAndDay(attendance.getUserId(), attendance.getDay());
		//2.如果不存在,设置对象id,保存
		if(vo == null) {
			attendance.setId(idWorker.nextId() + "");
		}else{
			attendance.setId(vo.getId());
		}
		attendanceDao.save(attendance);
	}

	//查询归档数据
	public List<ArchiveMonthlyInfo> getReports(String atteDate, String companyId) {
		//1.查询所有企业用户
		List<User> users = userDao.findByCompanyId(companyId);
		//2.循环遍历用户列表,统计每个用户当月的考勤记录
		List<ArchiveMonthlyInfo> list = new ArrayList<>();
		for (User user : users) {
			ArchiveMonthlyInfo info = new ArchiveMonthlyInfo(user);
			//统计每个用户的考勤记录
			Map map = attendanceDao.statisByUser(user.getId(),atteDate +"%");
			info.setStatisData(map);
			list.add(info);
		}
		return list;
	}
	//新建报表
	public void newReports(String yearMonth, String companyId) {
		CompanySettings settings = companySettingsDao.findById(companyId).get();
		settings.setDataMonth(yearMonth);
		companySettingsDao.save(settings);
	}
	//根据年份查询所有考勤历史
	public List<ArchiveMonthly> findByYear(String year,String companyId) {
		return archiveMonthlyDao.findByCompanyIdAndArchiveYear(companyId,year);
	}

	/**
	 * 查询归档详情
	 * @param id
	 * @return
	 */
	public List<ArchiveMonthlyInfo> findMonthlyInfoByAmid(String id) {
		return archiveMonthlyInfoDao.findByAtteArchiveMonthlyId(id);
	}
}
