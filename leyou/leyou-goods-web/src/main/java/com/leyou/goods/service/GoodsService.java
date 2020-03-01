package com.leyou.goods.service;

import com.leyou.goods.client.BrandClient;
import com.leyou.goods.client.CategoryClient;
import com.leyou.goods.client.GoodsClient;
import com.leyou.goods.client.SpecificationClient;
import com.leyou.item.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author 柒
 * @date 2020-02-26 13:16
 * @Description: 商品详情页数据模型
 */
@Service
public class GoodsService {

    @Autowired
    private BrandClient brandClient ;

    @Autowired
    private CategoryClient categoryClient ;

    @Autowired
    private GoodsClient goodsClient ;

    @Autowired
    private SpecificationClient specificationClient ;

    /**
     * 根据spuId 查询所有需要的商品信息
     * @param spuId
     * @return
     */
    public Map<String,Object> loadData(Long spuId){
        Map<String,Object> map = new HashMap<>();

        // 1. 获取数据

        // 根据spuId获取spu信息
        Spu spu = this.goodsClient.querySpuById(spuId);

        // 查询spuDetail
        SpuDetail detail = this.goodsClient.querySpuDetailBySpuId(spuId);

        // 查询skus信息
        List<Sku> skus = this.goodsClient.querySkusBySpuId(spuId);

        // 查询分类信息
        List<Long> cids = Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3());
        List<String> names = this.categoryClient.queryNamesByIds(cids);
        List<Map<String,Object>> categories = new ArrayList<>();
        for (int i = 0; i < cids.size(); i++) {
            Map<String,Object> categoryMap = new HashMap<>();
            categoryMap.put("id",cids.get(i));
            categoryMap.put("name",names.get(i));
            categories.add(categoryMap);
        }

        // 查询品牌信息
        Brand brand = this.brandClient.queryBrandById(spu.getBrandId());

        // 查询规格参数组
        List<SpecGroup> groups = this.specificationClient.queryGroupsWithParam(spu.getCid3());

        // 查询该分类下的特殊的规格参数信息：generic = false
        List<SpecParam> params = this.specificationClient.queryParams(null, spu.getCid3(), false, null);
        // 将规格参数信息封装成Map<id,name>的形式
        Map<Long, String> paramMap = new HashMap<>();
        params.forEach(param -> {
            paramMap.put(param.getId(),param.getName());
        });

        // 2. 封装需要渲染的数据模型

        // spu信息
        map.put("spu",spu);
        // spuDetail信息
        map.put("spuDetail",detail);
        // skus信息
        map.put("skus",skus);
        // 分类信息
        map.put("categories",categories);
        // 品牌信息
        map.put("brand",brand);
        // 规格参数信息
        map.put("groups",groups);
        // 特殊规格参数信息
        map.put("paramMap",paramMap);

        // 3. 返回结果
        return map;
    }
}
