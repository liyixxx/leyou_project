package com.leyou.cart.service;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.cart.client.CartClient;
import com.leyou.cart.interceptor.LoginInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.pojo.Sku;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 柒
 * @date 2020-02-29 15:55
 * @Description:
 */
@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate ;

    @Autowired
    private CartClient cartClient ;

    static final String KEY_PREFIX = "leyou:cart:";

    /**
     * 新增购物车
     * @param cart
     */
    public void addCart(Cart cart) {
        // 获取用户信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        // 查询购物车记录 redis数据类型:(userId, map<skuId,item>) map里面是redis的hash类型数据
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userInfo.getId());
        // redis中hash的key = cart的skuId
        String hashKey = cart.getSkuId().toString();
        // 获取传入的num值
        Integer num = cart.getNum();
        // 判断商品是否在购物车中
        if(hashOps.hasKey(hashKey)){
            // 如果存在则值增加数量

            // 获取购物车信息 json类型
            String cartJson = hashOps.get(hashKey).toString();
            cart = JsonUtils.parse(cartJson, Cart.class);
            // 增加数量
            cart.setNum(cart.getNum() + num);
        } else {
            // 如果不存在则新增一个购物车

            // 根据skuId查询出sku信息
            Sku sku = this.cartClient.querySkuBySkuId(cart.getSkuId());
            cart.setImage(StringUtils.isBlank(sku.getImages())? "" : StringUtils.split(sku.getImages(),",")[0]);
            cart.setOwnSpec(sku.getOwnSpec());
            cart.setPrice(sku.getPrice());
            cart.setTitle(sku.getTitle());
            cart.setUserId(userInfo.getId());
        }
        // 存入到redis
        hashOps.put(hashKey,JsonUtils.serialize(cart));
    }

    /**
     * 查询购物车信息
     * @return
     */
    public List<Cart> queryCartList() {
        // 获取用户信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        // 判断该用户是否存在购物车
        String key = KEY_PREFIX + userInfo.getId();
        if(!this.redisTemplate.hasKey(key)){
            return null ;
        }
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);

        // 获取value集合（购物车集合）
        List<Object> carts = hashOps.values();
        if(CollectionUtils.isEmpty(carts)){
            return null;
        }
        // 查询购物车数据 转换为cart集合
        List<Cart> list = carts.stream().map(cart -> {
            return JsonUtils.parse(cart.toString(), Cart.class);
        }).collect(Collectors.toList());

        return list ;
    }

    /**
     * 修改
     * @param cart
     */
    public void updateCart(Cart cart) {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String key = KEY_PREFIX + userInfo.getId();
        // 判断该用户是否存在购物车
        if(!this.redisTemplate.hasKey(key)){
            return;
        }
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        Integer num = cart.getNum();
        // 获取redis中购物车信息
        String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
        cart = JsonUtils.parse(cartJson, Cart.class);
        // 修改
        cart.setNum(num);
        // 重新放回到redis
        hashOps.put(cart.getSkuId().toString(),JsonUtils.serialize(cart));
    }

    /**
     * 删除
     * @param skuId
     */
    public void deleteCart(String skuId) {
        // 获取登录用户
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String key = KEY_PREFIX + userInfo.getId();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        hashOps.delete(skuId);
    }
}
