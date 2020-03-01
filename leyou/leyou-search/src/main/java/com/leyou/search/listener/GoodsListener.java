package com.leyou.search.listener;

import com.leyou.search.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author 柒
 * @date 2020-02-26 21:16
 * @Description: 搜索的消息监听
 */
@Component
public class GoodsListener {

    @Autowired
    private SearchService searchService;

    private static final Logger LOGGER = LoggerFactory.getLogger(GoodsListener.class);

    /**
     * 监听更新和保存
     * springAOP 会根据方法是否发生了异常来决定是否手动ACK
     *
     * @param id
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "LEYOU.SEARCH.SAVE.QUEUE", durable = "true"),
            exchange = @Exchange(value = "LEYOU.ITEM.EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"item.insert", "item.update"}
    ))
    public void save(Long id) throws IOException {
        LOGGER.info("监听到商品修改操作，商品id = {id}",id);
        if (id == null) {
            return ;
        }
        this.searchService.save(id);
    }

    /**
     * 监听删除操作
     *
     * @param id
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "LEYOU.SEARCH.DELETE.QUEUE", durable = "true"),
            exchange = @Exchange(value = "LEYOU.ITEM.EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"item.delete"}
    ))
    public void delete(Long id) throws IOException {
        LOGGER.info("监听到商品删除操作，商品id = ",id);
        if (id == null) {
            return ;
        }
        this.searchService.delete(id);
    }

}
