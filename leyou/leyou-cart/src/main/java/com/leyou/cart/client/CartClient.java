package com.leyou.cart.client;

import com.leyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author 柒
 * @date 2020-02-29 16:16
 * @Description:
 */
@FeignClient("item-service")
public interface CartClient extends GoodsApi{
}
