package com.leyou.goods.client;

import com.leyou.item.api.BrandApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author 柒
 * @date 2020-02-24 15:47
 * @Description: 品牌
 */
@FeignClient("item-service")
public interface BrandClient extends BrandApi {
}
