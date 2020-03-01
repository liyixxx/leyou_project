package com.leyou.goods.service;

import com.leyou.goods.utils.ThreadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * @author 柒
 * @date 2020-02-26 15:12
 * @Description: 页面静态化处理
 */
@Service
public class GoodsHtmlService {

    @Autowired
    private TemplateEngine engine;

    @Autowired
    private GoodsService goodsService;

    /**
     * 进行页面静态化处理 将页面保存到本地服务器上
     *
     * @param spuId
     */
    public void createHtml(Long spuId) {

        // 初始化运行上下文
        Context context = new Context();
        // 设置数据模型
        context.setVariables(goodsService.loadData(spuId));
        PrintWriter writer = null ;
        try {
            // 将静态文件生成到服务器本地
            File file = new File("D:\\nginx-1.14.0\\html\\item\\" + spuId + ".html");
            writer = new PrintWriter(file);
            // 参数：模板名称 上下文对象 流
            this.engine.process("item", context, writer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            // 关闭io流
            if (writer != null){
                writer.close();
            }
        }
    }

    /**
     * 新建线程处理页面静态化
     * @param spuId
     */
    public void asyncExcute(Long spuId) {
        ThreadUtils.execute(()->createHtml(spuId));
        /*ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                createHtml(spuId);
            }
        });*/
    }

    /**
     * 删除页面
     * @param id
     */
    public void deleteHtml(Long id) {
        File file = new File("D:\\nginx-1.14.0\\html\\item\\" + id + ".html");
        file.deleteOnExit();
    }
}
