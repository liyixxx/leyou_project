package com.leyou.goods.controller;

import com.leyou.goods.service.GoodsHtmlService;
import com.leyou.goods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author 柒
 * @date 2020-02-26 12:09
 * @Description: 商品详情
 */
@Controller
@RequestMapping("item")
public class GoodsController {

    @Autowired
    private GoodsService goodsService ;

    @Autowired
    private GoodsHtmlService goodsHtmlService ;
    /**
     * 跳转到商品详情页
     * @param model
     * @param id spuId
     * @return
     */
    @GetMapping("{id}.html")
    public String toItemPage(Model model, @PathVariable("id")Long id){
        model.addAllAttributes(goodsService.loadData(id));
        // 静态化页面处理
        goodsHtmlService.createHtml(id);
        return "item";
    }
}
