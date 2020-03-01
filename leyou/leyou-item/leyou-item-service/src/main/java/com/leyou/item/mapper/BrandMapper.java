package com.leyou.item.mapper;

import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author 柒
 * @date 2020-02-19 15:09
 *
 * 商品品牌-mapper
 */
public interface BrandMapper extends Mapper<Brand>{

    /**
     * 双表新增
     * @param cid
     * @param bid
     */
    @Insert("INSERT INTO tb_category_brand (category_id, brand_id) values (#{cid}, #{bid})")
    void insertCategoryAndBrand(@Param("cid") Long cid, @Param("bid") Long bid);

    /**
     * 多表联合查询 通过cid查询所属的品牌信息
     * @param cid
     * @return
     */
    @Select("SELECT * FROM tb_brand b INNER JOIN tb_category_brand c ON b.id=c.brand_id WHERE c.category_id=#{cid}")
    List<Brand> selectBrandByCid(Long cid);

    /**
     * 根据bid删除所有的cid
     * @param id
     */
    @Delete("DELETE FROM tb_category_brand WHERE brand_id = #{id}")
    void deleteCategoryByBid(Long id);
}
