package com.ihrm.social.service;

import com.alibaba.fastjson.JSON;
import com.ihrm.common.entity.Result;
import com.ihrm.common.utils.BeanMapUtils;

import com.ihrm.common.utils.IdWorker;
import com.ihrm.domain.employee.UserCompanyPersonal;
import com.ihrm.domain.social_security.*;

import com.ihrm.social.client.EmployeeFeignClient;
import com.ihrm.social.dao.ArchiveDao;
import com.ihrm.social.dao.ArchiveDetailDao;
import com.ihrm.social.dao.UserSocialSecurityDao;
import org.apache.commons.lang.StringUtils;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ArchiveService {

	@Autowired
	private ArchiveDao archiveDao;

	@Autowired
	private EmployeeFeignClient employeeFeignClient;

	@Autowired
	private ArchiveDetailDao archiveDetailDao;

	@Autowired
	private UserSocialSecurityDao userSocialSecurityDao;

	@Autowired
	private UserSocialService userSocialService;

	@Autowired
	private PaymentItemService paymentItemService;

	@Autowired
	private IdWorker idWorker;


	public List<ArchiveDetail> getReports(String yearMonth, String companyId) {
		//查询用户的社保列表 (用户和基本社保数据)
		Page<Map> userSocialSecurityItemPage = userSocialSecurityDao.findPage(companyId,null);

		List<ArchiveDetail> list = new ArrayList<>();

		for (Map map : userSocialSecurityItemPage) {
			String userId = (String)map.get("userId");
			String mobile = (String)map.get("mobile");
			String username = (String)map.get("username");
			String departmentName = (String)map.get("departmentName");
			ArchiveDetail vo = new ArchiveDetail(userId,mobile,username,departmentName);
			vo.setTimeOfEntry((Date) map.get("timeOfEntry"));
			//获取个人信息
			Result personalResult = employeeFeignClient.findPersonalInfo(vo.getUserId());
			if (personalResult.isSuccess()) {
				UserCompanyPersonal userCompanyPersonal = JSON.parseObject(JSON.toJSONString(personalResult.getData()), UserCompanyPersonal.class);
				vo.setUserCompanyPersonal(userCompanyPersonal);
			}
			//社保相关信息
			getOtherData(vo, yearMonth);
			list.add(vo);
		}
		return list;
	}

	public Archive findArchive(String companyId, String yearMonth) {
		return archiveDao.findByCompanyIdAndYearsMonth(companyId,yearMonth);
	}

	public List<ArchiveDetail> findAllDetailByArchiveId(String id) {
		return archiveDetailDao.findByArchiveId(id);
	}

	public void getOtherData(ArchiveDetail vo, String yearMonth) {

		UserSocialSecurity userSocialSecurity = userSocialService.findById(vo.getUserId());
		if(userSocialSecurity == null) {
			return;
		}

		BigDecimal socialSecurityCompanyPay = new BigDecimal(0);

		BigDecimal socialSecurityPersonalPay = new BigDecimal(0);

		List<CityPaymentItem> cityPaymentItemList = paymentItemService.findAllByCityId(userSocialSecurity.getProvidentFundCityId());

		for (CityPaymentItem cityPaymentItem : cityPaymentItemList) {
			if (cityPaymentItem.getSwitchCompany()) {
				BigDecimal augend;
				if (cityPaymentItem.getPaymentItemId().equals("4") && userSocialSecurity.getIndustrialInjuryRatio() != null) {
					augend = userSocialSecurity.getIndustrialInjuryRatio().multiply(userSocialSecurity.getSocialSecurityBase());
				} else {
					augend = cityPaymentItem.getScaleCompany().multiply(userSocialSecurity.getSocialSecurityBase());
				}
				BigDecimal divideAugend = augend.divide(new BigDecimal(100));
				socialSecurityCompanyPay = socialSecurityCompanyPay.add(divideAugend);
			}
			if (cityPaymentItem.getSwitchPersonal()) {
				BigDecimal augend = cityPaymentItem.getScalePersonal().multiply(userSocialSecurity.getSocialSecurityBase());
				BigDecimal divideAugend = augend.divide(new BigDecimal(100));
				socialSecurityPersonalPay = socialSecurityPersonalPay.add(divideAugend);
			}
		}

		vo.setSocialSecurity(socialSecurityCompanyPay.add(socialSecurityPersonalPay));
		vo.setSocialSecurityEnterprise(socialSecurityCompanyPay);
		vo.setSocialSecurityIndividual(socialSecurityPersonalPay);
		vo.setUserSocialSecurity(userSocialSecurity);
		vo.setSocialSecurityMonth(yearMonth);
		vo.setProvidentFundMonth(yearMonth);
	}

	public void archive(String yearMonth, String companyId) {
		//1.查询归档明细数据
		List<ArchiveDetail> archiveDetails = getReports(yearMonth, companyId);
		//1.1 计算当月,企业与员工支出的所有社保金额
		BigDecimal enterMoney = new BigDecimal(0);
		BigDecimal personMoney = new BigDecimal(0);
		for (ArchiveDetail archiveDetail : archiveDetails) {
			BigDecimal t1 = archiveDetail.getProvidentFundEnterprises() == null ? new BigDecimal(0): archiveDetail.getProvidentFundEnterprises();
			BigDecimal t2 = archiveDetail.getSocialSecurityEnterprise() == null ? new BigDecimal(0): archiveDetail.getSocialSecurityEnterprise();
			BigDecimal t3 = archiveDetail.getProvidentFundIndividual() == null ? new BigDecimal(0): archiveDetail.getProvidentFundIndividual();
			BigDecimal t4 = archiveDetail.getSocialSecurityIndividual() == null ? new BigDecimal(0): archiveDetail.getSocialSecurityIndividual();
			enterMoney = enterMoney.add(t1).add(t2);
			personMoney = enterMoney.add(t3).add(t4);
		}
		//2.查询当月是否已经归档
		Archive archive = this.findArchive(companyId,yearMonth);
		//3.不存在已归档的数据,保存
		if(archive == null) {
			archive = new Archive();
			archive.setCompanyId(companyId);
			archive.setYearsMonth(yearMonth);
			archive.setCreationTime(new Date());
			archive.setId(idWorker.nextId()+"");
		}
		//4.如果存在已归档数据,覆盖
		archive.setEnterprisePayment(enterMoney);
		archive.setPersonalPayment(personMoney);
		archive.setTotal(enterMoney.add(personMoney));
		archiveDao.save(archive);
		for (ArchiveDetail archiveDetail : archiveDetails) {
			archiveDetail.setId(idWorker.nextId() + "");
			archiveDetail.setArchiveId(archive.getId());
			archiveDetail.setYearsMonth(yearMonth);
			archiveDetailDao.save(archiveDetail);
		}
	}

	public List<Archive> findArchiveByYear(String companyId, String year) {
		return archiveDao.findByCompanyIdAndYearsMonthLike(companyId,year+"%");
	}

	public ArchiveDetail findUserArchiveDetail(String userId, String yearMonth) {
		//根据用户id和年月查询用户归档明细
		return archiveDetailDao.findByUserIdAndYearsMonth(userId,yearMonth);
	}
}
