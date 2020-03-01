package com.leyou.goods.client;

import com.leyou.item.api.CategoryApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author 柒
 * @date 2020-02-24 15:48
 * @Description: 分类
 */
@FeignClient("item-service")
public interface CategoryClient extends CategoryApi {
}
