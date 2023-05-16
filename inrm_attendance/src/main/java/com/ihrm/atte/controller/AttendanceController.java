package com.ihrm.atte.controller;

import com.ihrm.atte.service.ArchiveService;
import com.ihrm.atte.service.AtteService;
import com.ihrm.atte.service.ExcelImportService;
import com.ihrm.common.controller.BaseController;
import com.ihrm.common.entity.Result;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.common.utils.DateUtil;
import com.ihrm.domain.atte.entity.ArchiveMonthly;
import com.ihrm.domain.atte.entity.ArchiveMonthlyInfo;
import com.ihrm.domain.atte.entity.Attendance;
import com.ihrm.domain.atte.entity.AttendanceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/attendances")
public class AttendanceController extends BaseController {

	@Autowired
	private ExcelImportService excelImportService;

	@Autowired
	private AtteService atteService;

	@Autowired
	private ArchiveService archiveService;

	/**
	 * 上传考勤数据
	 */
	@RequestMapping(value = "/import" ,method = RequestMethod.POST)
	public Result importExcel(@RequestParam(name="file") MultipartFile file) throws Exception {
		excelImportService.importAttendaneExcel(file,companyId);
		return new Result(ResultCode.SUCCESS);
	}


	/**
	 * 查询考勤数据列表
	 *
	 */
	@RequestMapping(value = "" ,method = RequestMethod.GET)
	public Result importExcel(int page,int pagesize) throws Exception {
		Map map = atteService.getAtteDate(companyId,page,pagesize);
		return new Result(ResultCode.SUCCESS,map);
	}

	/**
	 * 编辑用户的考勤记录
	 */
	@RequestMapping(value = "/{id}" ,method = RequestMethod.PUT)
	public Result editAtte(@RequestBody Attendance attendance) throws Exception {
		atteService.editAtte(attendance);
		return new Result(ResultCode.SUCCESS);
	}

	/**
	 * 获取月报表归档数据
	 * /attendances/archive/item?archiveDate=202305
	 */
	@RequestMapping(value = "/reports" ,method = RequestMethod.GET)
	public Result reports(String atteDate) throws Exception {
		List<ArchiveMonthlyInfo> reports = atteService.getReports(atteDate, companyId);
		return new Result(ResultCode.SUCCESS,reports);
	}


	/**
	 * 归档数据
	 * /attendances/archive/item?archiveDate=202305
	 */
	@RequestMapping(value = "/archive/item" ,method = RequestMethod.GET)
	public Result item(String archiveDate) throws Exception {
		archiveService.saveArchive(archiveDate,companyId);
		return new Result(ResultCode.SUCCESS);
	}

	/**
	 * 新建考勤报表
	 */
	@PostMapping(value = "/newReports")
	public Result newReports(@RequestBody Map map) throws Exception {
		String yearMonth =(String)map.get("atteDate");
		String nextMonth = DateUtil.nextMonth(yearMonth);
		atteService.newReports(nextMonth,companyId);
		return new Result(ResultCode.SUCCESS);
	}

	/**
	 * 历史归档
	 */
	@GetMapping("/reports/year")
	public Result findReportsByYear(String year){
		List<ArchiveMonthly> list=atteService.findByYear(year,companyId);
		return new Result(ResultCode.SUCCESS,list);
	}
	/**
	 * 查询归档详情
	 */
	@PostMapping("/reports/{id}")
	public Result findInfosById(@PathVariable String id){
		List<ArchiveMonthlyInfo> list=atteService.findMonthlyInfoByAmid(id);
		return new Result(ResultCode.SUCCESS,list);
	}

}
