package com.leyou.search.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 柒
 * @date 2020-02-24 15:47
 * @Description: 搜索
 */
@Service
public class SearchService {

    @Autowired
    private BrandClient brandClient ;

    @Autowired
    private CategoryClient categoryClient ;

    @Autowired
    private GoodsClient goodsClient ;

    @Autowired
    private SpecificationClient specificationClient ;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private GoodsRepository goodsRepository ;

    /**
     * 构建goods
     * ObjectMapper.wariteValueToString(Object) 将内容序列化为json字段
     * ObjectMapper.readValue(String.new TypeReference<T>(){}) 将json数据转换为对应的object
     * @param spu
     * @return
     * @throws JsonProcessingException
     */
    public Goods buildGoods(Spu spu) throws IOException {
        Goods goods = new Goods();

        // 根据ids查询分类名称
        List<String> names = this.categoryClient.queryNamesByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        // 根据品牌id查询品牌信息
        Brand brand = this.brandClient.queryBrandById(spu.getBrandId());

        // 根据spuId查询所有的sku
        List<Sku> skus = this.goodsClient.querySkusBySpuId(spu.getId());
        // 初始化一个价格集合，收集所有sku的价格
        List<Long> prices = new ArrayList<>();
        // 新建集合用于收集sku的字段信息：id / title / price / image
        List<Map<String, Object>> skuMapList = new ArrayList<>();
        // 遍历赋值
        skus.forEach(sku -> {
            // 添加price
            prices.add(sku.getPrice());
            Map<String, Object> map = new HashMap<>();

            // 收集sku字段信息
            map.put("id", sku.getId());
            map.put("title", sku.getTitle());
            map.put("price", sku.getPrice());
            // 获取sku中的图片，数据库的图片为多张时,以“,”分隔，以逗号来切割返回图片数组，获取第一张图片
            map.put("image", StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(), ",")[0]);

            skuMapList.add(map);
        });

        // 根据spu中的cid3（商品类目id）查询出所有的搜索规格参数
        List<SpecParam> params = this.specificationClient.queryParams(null, spu.getCid3(), null, true);

        // 根据spuId查询spuDetail
        SpuDetail spuDetail = this.goodsClient.querySpuDetailBySpuId(spu.getId());

        // spuDetail的genericSpec和specialSpec字段都是json数据格式，其中的key=规格参数的id，value=具体的规格参数。但是存放的具体内容不同(单个值，数组)
        // eg ：genericSpec -- "1":"其它","2":"G9青春版（全网通版）","3":"2018" ...
        // eg ：specialSpec -- {"4":["白色","金色","玫瑰金"],"12":["3GB","4GB"],"13":["16GB"]}

        // 将通用规格参数进行反序列化
        Map<String, Object> genericSpecMap = MAPPER.readValue(spuDetail.getGenericSpec(), new TypeReference<Map<String, Object>>(){});
        // 将特殊规格参数进行反序列化
        Map<String, List<Object>> specialSpecMap = MAPPER.readValue(spuDetail.getSpecialSpec(), new TypeReference<Map<String, List<Object>>>(){});

        // 创建map 接收<name,value>
        Map<String, Object> specs = new HashMap<>();
        params.forEach(param -> {
            // 判断规格参数的类型，是否是通用的规格参数
            if (param.getGeneric()) {
                // 如果是通用类型的参数，从genericSpecMap获取规格参数值
                String value = genericSpecMap.get(param.getId().toString()).toString();
                // 判断是否是数值类型，如果是数值类型，应该返回一个区间
                if (param.getNumeric()) {
                    value = chooseSegment(value, param);
                }
                specs.put(param.getName(), value);
            } else {
                // 如果是特殊的规格参数，从specialSpecMap中获取值
                List<Object> value = specialSpecMap.get(param.getId().toString());
                specs.put(param.getName(), value);
            }
        });

        // 设值
        goods.setId(spu.getId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setBrandId(spu.getBrandId());
        goods.setCreateTime(spu.getCreateTime());
        goods.setSubTitle(spu.getSubTitle());
        // 拼接all字段，需要分类名称以及品牌名称
        goods.setAll(spu.getTitle() + " " + StringUtils.join(names, " ") + " " + brand.getName());
        // 获取spu下的所有sku的价格
        goods.setPrice(prices);
        // 获取spu下的所有sku，并转化成json字符串
        goods.setSkus(MAPPER.writeValueAsString(skuMapList));
        // 获取所有查询的规格参数{name:value}
        goods.setSpecs(specs);
        return goods;
    }

    /**
     * 区间选择判断
     * @param value
     * @param p
     * @return
     */
    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    /**
     * 查询
     * @param request 查询条件
     * @return
     */
    public SearchResult search(SearchRequest request) {
        // 判断是否有搜索条件，如果没有，直接返回null。不允许搜索全部商品
        if (StringUtils.isBlank(request.getKey())) {
            return null;
        }
        // 自定义构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 添加查询条件：match匹配查询，对key进行and匹配
//        QueryBuilder basicQuery = QueryBuilders.matchQuery("all",request.getKey()).operator(Operator.AND);
        // 添加过滤条件的查询
        BoolQueryBuilder basicQuery = buildBoolQueryBuilder(request);
        queryBuilder.withQuery(basicQuery);
        // 添加分页
        queryBuilder.withPageable(PageRequest.of(request.getPage()-1,request.getSize()));
        // 添加排序
        String sortBy = request.getSortBy();
        Boolean desc = request.getDescending();
        if(StringUtils.isNotBlank(sortBy)){
            // 当排序字段不为空时 进行排序 按照desc字段决定升序还是降序
            queryBuilder.withSort(SortBuilders.fieldSort(sortBy).order(desc ? SortOrder.DESC:SortOrder.ASC));
        }
        // 结果集过滤 只保留id，skus，subTitle
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","skus","subTitle"},null));

        // 添加分类和品牌的聚合
        String categoryAggName = "categories";
        String brandAggName = "brands";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));

        // 进行查询 获取结果集
        // Page<Goods> search = this.goodsRepository.search(queryBuilder.build());
        AggregatedPage<Goods> search = (AggregatedPage<Goods>)this.goodsRepository.search(queryBuilder.build());

        // 获取聚合结果集并解析成对应的结果集
        List<Map<String,Object>> categories = getCategoryAggResult(search.getAggregation(categoryAggName));
        List<Brand> brands = getBrandAggResult(search.getAggregation(brandAggName));

        // 定义规格参数结果集
        List<Map<String, Object>> specs = null ;
        // 根据解析的分类，判断是否只有一个分类，进行规格参数的聚合
        if(!CollectionUtils.isEmpty(categories) && categories.size() == 1){
            // 对规格参数进行聚合：分类id，match匹配查询条件
            specs = getParamAggResult((Long)categories.get(0).get("id"),basicQuery);
        }

        // 返回结果集
        return new SearchResult(search.getTotalElements(),search.getTotalPages(),search.getContent(),categories,brands,specs);
    }

    /**
     * 使用bool查询根据筛选条件进行过滤
     * @param request
     * @return
     */
    private BoolQueryBuilder buildBoolQueryBuilder(SearchRequest request) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // 添加基本查询条件
        boolQueryBuilder.must(QueryBuilders.matchQuery("all", request.getKey()).operator(Operator.AND));

        // 添加过滤条件
        if (CollectionUtils.isEmpty(request.getFilter())){
            return boolQueryBuilder;
        }
        for (Map.Entry<String, Object> entry : request.getFilter().entrySet()) {

            String key = entry.getKey();
            // 如果过滤条件是“品牌”, 过滤的字段名：brandId
            if (StringUtils.equals("品牌", key)) {
                key = "brandId";
            } else if (StringUtils.equals("分类", key)) {
                // 如果是“分类”，过滤字段名：cid3
                key = "cid3";
            } else {
                // 如果是规格参数名，过滤字段名：specs.key.keyword
                key = "specs." + key + ".keyword";
            }
            boolQueryBuilder.filter(QueryBuilders.termQuery(key, entry.getValue()));
        }

        return boolQueryBuilder;
    }

    /**
     * 根据查询条件聚合规格参数
     * @param cid
     * @param basicQuery
     * @return
     */
    private List<Map<String,Object>> getParamAggResult(Long cid, QueryBuilder basicQuery) {
        // 自定义查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        // 基于基本的查询条件，聚合规格参数
        queryBuilder.withQuery(basicQuery);

        // 查询要聚合的规格参数
        List<SpecParam> params = this.specificationClient.queryParams(null, cid, null, true);

        // 队规格参数进行聚合
        params.forEach(param -> {
            // 根据规格参数名进行聚合，不进行分词
            queryBuilder.addAggregation(AggregationBuilders.terms(param.getName()).field("specs." + param.getName() + ".keyword"));
        });

        // 添加结果集过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{}, null));

        // 执行聚合查询
        AggregatedPage<Goods> goodsPage = (AggregatedPage<Goods>)this.goodsRepository.search(queryBuilder.build());

        // 解析聚合结果集：
        // 定义一个集合，收集聚合结果集
        List<Map<String, Object>> paramMapList = new ArrayList<>();
        //  获取结果集map：key=id（规格参数id） ， value=聚合结果对象
        Map<String, Aggregation> aggregationMap = goodsPage.getAggregations().asMap();

        // 遍历获取的map：map.entrySet() --> 将map转换为set集合
        for (Map.Entry<String, Aggregation> entry : aggregationMap.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            // 放入规格参数名
            map.put("k", entry.getKey());
            // 收集规格参数值
            List<Object> options = new ArrayList<>();
            // 解析聚合Aggregation：entry.getValue()
            StringTerms terms = (StringTerms)entry.getValue();
            // 解析桶 将桶中的key放入到集合中
            terms.getBuckets().forEach(bucket -> options.add(bucket.getKeyAsString()));
            // 放入规格参数
            map.put("options", options);
            // 添加到结果集中
            paramMapList.add(map);
        }

        return paramMapList;
    }

    /**
     * 解析品牌聚合结果集
     * @param aggregation
     * @return
     */
    private List<Brand> getBrandAggResult(Aggregation aggregation) {
        // 结果字段：long  分类条件：terms
        LongTerms terms = (LongTerms) aggregation;
        // 获取聚合中的桶
        List<LongTerms.Bucket> buckets = terms.getBuckets();
        // 将桶内元素转换到List<Brand>中
        return buckets.stream().map(bucket -> {
            // 获取桶内id（品牌id）：Long
            long l = bucket.getKeyAsNumber().longValue();
            // 根据id查询品牌信息
            return this.brandClient.queryBrandById(l);
        }).collect(Collectors.toList());
    }

    /**
     * 解析分类聚合集
     * @param aggregation
     * @return
     */
    private List<Map<String,Object>> getCategoryAggResult(Aggregation aggregation) {
        LongTerms terms = (LongTerms) aggregation;
        // 获取桶
        List<LongTerms.Bucket> buckets = terms.getBuckets();
        // 将桶内信息封装到Map<String,Object>中 返回结果
        return buckets.stream().map(bucket -> {
            Map<String,Object> map = new HashMap<>();
            // 获取id（分类id：[cid3]）
            long id = bucket.getKeyAsNumber().longValue();
            // 根据cid3查询品牌名称
            List<String> names = this.categoryClient.queryNamesByIds(Arrays.asList(id));
            map.put("id",id);
            map.put("name",names.get(0));
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 保存索引
     * @param id
     */
    public void save(Long id) throws IOException {
        Spu spu = this.goodsClient.querySpuById(id);
        Goods goods = this.buildGoods(spu);
        this.goodsRepository.save(goods);
    }

    /**
     * 根据id 删除商品索引
     * @param id
     */
    public void delete(Long id) {
        this.goodsRepository.deleteById(id);
    }
}
