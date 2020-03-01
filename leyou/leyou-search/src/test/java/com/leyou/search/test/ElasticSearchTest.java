package com.leyou.search.test;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.service.SearchService;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 柒
 * @date 2020-02-24 18:59
 * @Description:
 */
@SpringBootTest
public class ElasticSearchTest {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private SearchService searchService;

    @Autowired
    private GoodsClient goodsClient;

    @Test
    public void test(){

        this.elasticsearchTemplate.createIndex(Goods.class);
        this.elasticsearchTemplate.putMapping(Goods.class);

        Integer page = 1;
        Integer rows = 100;

        do {
            // 分页查询spu，获取分页结果集
            PageResult<SpuBo> result = this.goodsClient.querySpuByPage(null, null, page, rows);
            // 获取当前页的数据
            List<SpuBo> items = result.getItems();
            // 处理List<SpuBo> ==> List<Goods>
            List<Goods> goodsList = items.stream().map(spuBo -> {
                try {
                    return this.searchService.buildGoods(spuBo);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());

            // 执行新增数据的方法
            this.goodsRepository.saveAll(goodsList);

            rows = items.size();
            page++;

        } while(rows == 100);
    }

    @Test
    public void testSelect(){
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 查询条件
        MatchQueryBuilder query = QueryBuilders.matchQuery("all","手机");
        // 添加查询条件
        queryBuilder.withQuery(query);
        // 分页
        queryBuilder.withPageable(PageRequest.of(1,5));
        // 排序
        queryBuilder.withSort(new FieldSortBuilder("price").order(SortOrder.DESC));
        // 过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(null,new String[]{}));
        // 查询 - 普通查询
        Page<Goods> search = this.goodsRepository.search(queryBuilder.build());
        // 处理结果集
//        System.out.println(search.getTotalElements());

    }

}
