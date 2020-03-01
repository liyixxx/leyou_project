package com.leyou.search.client;

import com.leyou.item.api.SpecificationApi;
import org.springframework.cloud.openfeign.FeignClient;
/**
 * @author 柒
 * @date 2020-02-24 15:49
 * @Description: 规格参数
 */
@FeignClient("item-service")
public interface SpecificationClient extends SpecificationApi {
}
