package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 柒
 * @date 2020-02-22 15:09
 * @Description: 商品业务层
 */
@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private SpuDetailMapper detailMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RabbitTemplate amqpTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(GoodsService.class);

    /**
     * 分页显示商品信息
     *
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    public PageResult<SpuBo> querySpuBoByPage(String key, Boolean saleable, Integer page, Integer rows) {
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();

        // 1.确定搜索条件 进行模糊查询
        if (StringUtils.isNotBlank(key)) {
            // 根据key 模糊搜索商品名
            criteria.andLike("title", "%" + key + "%");
        }
        if (saleable != null) {
            // 查询模糊查询结果中的满足对应状态(上架/下架/删除)的结果
            criteria.andEqualTo("saleable", saleable);
        }
        // 2.进行分页
        PageHelper.startPage(page, rows);
        // 3.执行查询
        List<Spu> spus = this.spuMapper.selectByExample(example);
        PageInfo<Spu> pageInfo = new PageInfo<>(spus);
        // 4.将查询结果封装到spubo中
        /**
         * Java8新特性：stream - 箭头函数（遍历处理）
         * list.stream().map(a->{箭头函数体，需要有返回值}).collect(Collectors.toList/Set...)
         * 可以将一个集合复制到另一个集合，对新的集合做一个换值/添加等操作..
         */
        List<SpuBo> spuBos = spus.stream().map(spu -> {
            SpuBo spuBo = new SpuBo();
            // bean工具类：将源对象属性复制到新对象中
            BeanUtils.copyProperties(spu, spuBo);
            // 根据主键 查询品牌名称
            Brand brand = this.brandMapper.selectByPrimaryKey(spuBo.getBrandId());
            // 查询商品名
            List<String> names = this.categoryService.queryNamesByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            spuBo.setBname(brand.getName());
            spuBo.setCname(StringUtils.join(names, "-"));
            return spuBo;
        }).collect(Collectors.toList());
       /* spus.forEach(spu->{
            SpuBo spuBo = new SpuBo();
            // copy共同属性的值到新的对象
            BeanUtils.copyProperties(spu, spuBo);
            // 查询分类名称
            List<String> names = this.categoryService.queryNamesByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            spuBo.setCname(StringUtils.join(names, "/"));

            // 查询品牌的名称
            spuBo.setBname(this.brandMapper.selectByPrimaryKey(spu.getBrandId()).getName());

            spuBos.add(spuBo);
        });*/
        // 返回
        return new PageResult<>(pageInfo.getTotal(), spuBos);
    }

    /**
     * 新增商品信息
     *
     * @param spuBo
     */
    @Transactional
    public void saveGoods(SpuBo spuBo) {
        Date currentDate = new Date();
        // 1. 新增 spu
        spuBo.setId(null);
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setCreateTime(currentDate);
        spuBo.setLastUpdateTime(currentDate);
        this.spuMapper.insertSelective(spuBo);
        // 2. 新增 spuDetatis
        SpuDetail spuDetail = spuBo.getSpuDetail();
        spuDetail.setSpuId(spuBo.getId());
        this.detailMapper.insertSelective(spuDetail);
        // 3. 循环新增 skus和stock - 抽取方法
        saveSkuAndStock(spuBo, currentDate);

        sendMessage("insert",spuBo.getId());
    }

    /**
     * 发送消息
     * @param type
     * @param id
     */
    private void sendMessage(String type , Long id) {
        try {
            // 发送消息
            this.amqpTemplate.convertAndSend("LEYOU.ITEM.EXCHANGE","item."+type , id);
            LOGGER.error("{} 商品操作，发送消息，商品id：{}", type, id);
        } catch (AmqpException e) {
            LOGGER.error("{}商品消息发送异常，商品id：{}", type, id, e);
        }
    }

    /**
     * 保存sku和stock表信息
     *
     * @param spuBo
     * @param currentDate
     */
    private void saveSkuAndStock(SpuBo spuBo, Date currentDate) {
        spuBo.getSkus().forEach(sku -> {
            // 新增sku
            sku.setId(null);
            sku.setSpuId(spuBo.getId());
            sku.setCreateTime(currentDate);
            sku.setLastUpdateTime(currentDate);
            this.skuMapper.insertSelective(sku);
            // 新增stock
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            this.stockMapper.insertSelective(stock);
        });
    }

    /**
     * 根据id查询描述信息
     *
     * @param id
     * @return
     */
    public SpuDetail queryDeatilById(Long id) {
        return this.detailMapper.selectByPrimaryKey(id);
    }

    /**
     * 根据spuId查询skus的信息
     *
     * @param id
     * @return
     */
    public List<Sku> querySkusByPid(Long id) {
        Sku record = new Sku();
        record.setSpuId(id);
        List<Sku> skus = this.skuMapper.select(record);
        skus.forEach(sku -> {
            sku.setStock(this.stockMapper.selectByPrimaryKey(sku.getId()).getStock());
        });
        return skus;
    }

    /**
     * 修改商品信息
     * ctrl alt l ： 代码格式化
     *
     * @param spuBo
     */
    @Transactional
    public void updateGoods(SpuBo spuBo) {
        // 根据spuId查询出sku的信息
        Sku record = new Sku();
        record.setSpuId(spuBo.getId());
        // 1. 删除sku和stock表信息
        List<Sku> skus = this.skuMapper.select(record);
        if (!CollectionUtils.isEmpty(skus)) {
            skus.forEach(sku -> {
                this.stockMapper.deleteByPrimaryKey(sku.getId());
            });
        }
        this.skuMapper.delete(record);

        // 2. 重新添加sku和stock信息
        Date date = new Date();
        this.saveSkuAndStock(spuBo, date);

        // 3. 修改spu和detail
        spuBo.setLastUpdateTime(date);
        spuBo.setCreateTime(null);
        spuBo.setSaleable(null);
        spuBo.setValid(null);
        this.spuMapper.updateByPrimaryKeySelective(spuBo);
        this.detailMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());

        sendMessage("update",spuBo.getId());
    }

    /**
     * 根据spuId查询spu信息
     *
     * @param id
     * @return
     */
    public Spu querySpuById(Long id) {
        return this.spuMapper.selectByPrimaryKey(id);
    }

    /**
     * 根据skuId查询sku信息
     * @param skuId
     * @return
     */
    public Sku querySkuById(Long skuId) {
        return this.skuMapper.selectByPrimaryKey(skuId);
    }
}
