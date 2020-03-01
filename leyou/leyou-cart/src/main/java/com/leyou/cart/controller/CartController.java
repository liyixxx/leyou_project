package com.leyou.cart.controller;

import com.leyou.cart.pojo.Cart;
import com.leyou.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author 柒
 * @date 2020-02-29 15:55
 * @Description: 购物车
 */
@Controller
public class CartController {

    @Autowired
    private CartService cartService ;

    /**
     * 新增购物车
     * @param cart
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart){
        this.cartService.addCart(cart);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 查询购物车信息
     * @return
     */
    @GetMapping
    public ResponseEntity<List<Cart>> queryCartList(){
        List<Cart> carts = this.cartService.queryCartList();
        if(CollectionUtils.isEmpty(carts)){
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(carts);
    }

    /**
     * 修改
     * @param cart
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateCart(@RequestBody Cart cart){
        this.cartService.updateCart(cart);
        return ResponseEntity.noContent().build();
    }

    /**
     * 删除
     * @param skuId
     * @return
     */
    @DeleteMapping("{skuId}")
    public ResponseEntity<Void> deleteCart(@PathVariable("skuId") String skuId) {
        this.cartService.deleteCart(skuId);
        return ResponseEntity.ok().build();
    }
}
