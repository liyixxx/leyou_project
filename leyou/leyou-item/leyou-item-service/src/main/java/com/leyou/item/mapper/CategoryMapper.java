package com.leyou.item.mapper;

import com.leyou.item.pojo.Category;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author 柒
 * @date 2020-02-19 12:26
 * SelectByIdListMapper：多主键查询
 */
public interface CategoryMapper extends Mapper<Category>,SelectByIdListMapper<Category,Long>{

}
