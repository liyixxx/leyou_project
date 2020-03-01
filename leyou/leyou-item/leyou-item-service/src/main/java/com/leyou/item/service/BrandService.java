package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @author 柒
 * @date 2020-02-19 15:10
 */
@Service
public class BrandService {

    @Autowired
    private BrandMapper brandMapper;

    /**
     * 根据查询条件分页并排序查询品牌信息
     * 工具util:
     * org.apache.commons.lang.StringUtils.isNotBlank(str) 非空判断<字符串>
     * org.springframework.util.CollectionUtils.isEmpty(obj) 非空判断<集合>
     *
     * @param key
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @return
     */
    public PageResult<Brand> queryBrandsByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {
        // 初始化example
        Example example = new Example(Brand.class);
        // 模糊条件
        Example.Criteria criteria = example.createCriteria();

        // 模糊查询
        if (StringUtils.isNotBlank(key)) {
            // 查询name中含有key(输入的条件)的结果 或者是首字母(letter)和key相等的结果
            criteria.andLike("name", "%" + key + "%").orEqualTo("letter", key);
        }

        // 添加分页条件
        PageHelper.startPage(page, rows);
        // 添加排序条件
        if (StringUtils.isNotBlank(sortBy)) {
            example.setOrderByClause(sortBy + " " + (desc ? "desc" : "asc"));
        }
        // 返回查询结果
        List<Brand> brands = this.brandMapper.selectByExample(example);
        // 包装为PageInfo
        PageInfo<Brand> info = new PageInfo<>(brands);
        // 返回分页结果集
        return new PageResult<>(info.getTotal(), info.getList());
    }

    /**
     * 新增品牌信息：需要新增两个表 品牌表和品牌-分类中间表
     * 添加事务
     *
     * @param brand
     * @param cids
     */
    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        // 先新增brand
        this.brandMapper.insertSelective(brand);
        // 在新增中间表 :
        /**
         *  java8 的forEach循环写法
         *  list.forEach(node ->{
         *      遍历执行的操作
         *  })
         */
        cids.forEach(cid -> {
            this.brandMapper.insertCategoryAndBrand(cid, brand.getId());
        });
    }

    /**
     * 根据商品类目id查询所有的品牌信息
     *
     * @param cid
     * @return
     */
    public List<Brand> queryBrandByCid(Long cid) {
        return this.brandMapper.selectBrandByCid(cid);
    }

    /**
     * 修改品牌
     *
     * @param brand
     * @param cids
     */
    @Transactional
    public void updateBrand(Brand brand, List<Long> cids) {
        this.brandMapper.updateByPrimaryKeySelective(brand);
        // 根据bid查询以前的cid 如果存在就删除
        this.brandMapper.deleteCategoryByBid(brand.getId());
        // 重新增加cids
        cids.forEach(cid -> {
            this.brandMapper.insertCategoryAndBrand(cid,brand.getId());
        });
    }

    /**
     * 根据id查询品牌
     * @param id
     * @return
     */
    public Brand queryBrandById(Long id) {
        return this.brandMapper.selectByPrimaryKey(id);
    }
}
