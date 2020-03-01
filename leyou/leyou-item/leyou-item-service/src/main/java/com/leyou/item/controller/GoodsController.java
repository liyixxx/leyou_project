package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author 柒
 * @date 2020-02-22 15:11
 * @Description: 商品操作
 */
@Controller
public class GoodsController {

    @Autowired
    private GoodsService goodsService ;

    /**
     * 分页显示商品信息
     * @param key  搜索条件
     * @param saleable  上下架信息
     * @param page  当前页数
     * @param rows  每页显示数据
     * @return
     */
    @GetMapping("/spu/page")
    public ResponseEntity<PageResult<SpuBo>> querySpuBoByPage(
        @RequestParam(value = "key",required = false) String key,
        @RequestParam(value = "saleable",required = false) Boolean saleable ,
        @RequestParam(value = "page" , defaultValue = "1") Integer page ,
        @RequestParam(value = "rows" , defaultValue = "5") Integer rows
    ){
        PageResult<SpuBo> pageResult= this.goodsService.querySpuBoByPage(key, saleable, page, rows);
        if(null == pageResult || CollectionUtils.isEmpty(pageResult.getItems())){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 新增商品信息
     * @param spuBo
     * @return
     */
    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods (@RequestBody SpuBo spuBo){
        this.goodsService.saveGoods(spuBo);
        // 新增成功 返回状态码201
        // 修改/删除成功 返回状态码204
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据id查询商品描述信息
     * @param id
     * @return
     */
    @GetMapping("spu/detail/{id}")
    public ResponseEntity<SpuDetail> queryDetailById(@PathVariable("id") Long id){
        SpuDetail detail = this.goodsService.queryDeatilById(id);
        if(null == detail){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(detail);
    }

    /**
     * 根据spuId查询skus的信息
     * ctrl alt t ：surround with
     * ctrl alt m ：抽取方法
     * @param id
     * @return
     */
    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>> querySkusByPid(@RequestParam("id") Long id){
        List<Sku> skus = this.goodsService.querySkusByPid(id);
        if(CollectionUtils.isEmpty(skus)){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(skus);
    }

    /**
     * 修改商品信息
     * @param spuBo
     * @return
     */
    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuBo spuBo){
        this.goodsService.updateGoods(spuBo);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 根据spuId 查询spu信息
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<Spu> querySpuById(@PathVariable("id") Long id){
        Spu spu = this.goodsService.querySpuById(id);
        if(null == spu){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(spu);
    }

    /**
     * 根据skuId查询sku信息
     * @param skuId
     * @return
     */
    @GetMapping("sku/{skuId}")
    public ResponseEntity<Sku> querySkuById(@PathVariable("skuId") Long skuId){
        Sku sku = this.goodsService.querySkuById(skuId);
        if(null == sku){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(sku);
    }
}
