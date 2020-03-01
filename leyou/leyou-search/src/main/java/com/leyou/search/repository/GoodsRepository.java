package com.leyou.search.repository;

import com.leyou.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author 柒
 * @date 2020-02-24 19:00
 * @Description: 操作goods索引库
 */
public interface GoodsRepository extends ElasticsearchRepository<Goods,Long>{
}
