package com.ihrm.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
/**
 * @Author yao
 * @Description 分页
 * @Date 16:25 2022/12/2
 * @Param null
 * @Return 
**/

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> {
    private Long total;
    private List<T> rows;
}
