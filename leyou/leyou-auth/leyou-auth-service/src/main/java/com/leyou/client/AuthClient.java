package com.leyou.client;

import com.leyou.user.api.UserApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author 柒
 * @date 2020-02-28 19:10
 * @Description: 远程调用user服务
 */
@FeignClient("user-service")
public interface AuthClient extends UserApi {
}
