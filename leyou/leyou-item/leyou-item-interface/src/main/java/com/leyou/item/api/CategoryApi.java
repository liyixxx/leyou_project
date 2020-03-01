package com.leyou.item.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
/**
 * @author 柒
 * @date 2020-02-24 15:37
 * @Description: 商品分类服务接口
 */
@RequestMapping("category")
public interface CategoryApi {

    @GetMapping
    public List<String> queryNamesByIds(@RequestParam("ids") List<Long> ids);
}
