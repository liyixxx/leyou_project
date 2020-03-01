package com.leyou.goods.listener;

import com.leyou.goods.service.GoodsHtmlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author 柒
 * @date 2020-02-26 20:47
 * @Description: 消息事件监听器
 * ctrl + p : 查看方法/注解参数
 */
@Component
public class GoodsListener {

    @Autowired
    private GoodsHtmlService goodsHtmlService;

    private static final Logger LOGGER = LoggerFactory.getLogger(GoodsListener.class);

    /**
     * 监听保存和更新
     *
     * @param id
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "LEYOU.ITEM.SAVE.QUEUE", durable = "true"),
            exchange = @Exchange(value = "LEYOU.ITEM.EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"item.insert", "item.update"}
    ))
    public void save(Long id) {
        if (id == null) {
            return;
        }
        LOGGER.warn("监听到商品修改操作，id = {id}",id);
        this.goodsHtmlService.createHtml(id);
    }

    /**
     * 监听删除
     *
     * @param id
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "LEYOU.ITEM.DELETE.QUEUE", durable = "true"),
            exchange = @Exchange(value = "LEYOU.ITEM.EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"item.delete"}
    ))
    public void delete(Long id) {
        if (id == null) {
            return;
        }
        LOGGER.warn("监听到商品删除操作，id = {id}",id);
        this.goodsHtmlService.deleteHtml(id);
    }

}
