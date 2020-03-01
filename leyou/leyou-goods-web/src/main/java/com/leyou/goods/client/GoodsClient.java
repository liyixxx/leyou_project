package com.leyou.goods.client;

import com.leyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author 柒
 * @date 2020-02-24 15:48
 * @Description: 商品
 * spring.main.allow-bean-definition-overriding: true：允许多个@FeignClient上的服务同名。
 */
@FeignClient("item-service")
public interface GoodsClient extends GoodsApi {

}
