package com.leyou.item.controller;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author 柒
 * @date 2020-02-22 11:56
 * 商品规格参数
 */
@Controller
@RequestMapping("spec")
public class SpecificationController {

    @Autowired
    private SpecificationService specificationService;

    /**
     * 根据类目ID查询分组信息
     * @param cid
     * @return
     */
    @GetMapping("/groups/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupsByCid(
            @PathVariable("cid") Long cid
    ){
       List<SpecGroup> list = this.specificationService.queryGroupsByCid(cid);
        if(CollectionUtils.isEmpty(list)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(list);
    }

    /**
     * 查询详细规格参数信息
     * 扩展方法 可以使用不同的参数完成不同的查询条件
     * @param gid
     * @return
     */
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> queryParam(
            @RequestParam(value = "gid",required = false) Long gid,
            @RequestParam(value = "cid",required = false) Long cid,
            @RequestParam(value = "generic",required = false) Boolean generic,
            @RequestParam(value = "searching",required = false) Boolean searching
    ){
        List<SpecParam> list = this.specificationService.queryParamByGid(gid,cid,generic,searching);
        if(CollectionUtils.isEmpty(list)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(list);
    }

    /**
     * 新增分组 -- 前台数据是以json的形式传递的
     * @param group
     * @return
     */
    @PostMapping("group")
    @Transactional
    public ResponseEntity<Void> saveGroup(@RequestBody SpecGroup group){
        this.specificationService.saveGroup(group);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据组新增参数信息
     * @param param
     * @return
     */
    @PostMapping("param")
    public ResponseEntity<Void> saveParam(@RequestBody SpecParam param){
        this.specificationService.saveParam(param);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 修改参数信息
     * @param param
     * @return
     */
    @PutMapping("param")
    public ResponseEntity<Void> updateParam(@RequestBody SpecParam param){
        this.specificationService.updateParam(param);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据id删除规格参数信息
     * @param id
     * @return
     */
    @DeleteMapping("param/{id}")
    public ResponseEntity<Void> updateParam(@PathVariable("id") Long id){
        this.specificationService.deleteParamById(id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据cid查询所有规格参数信息
     * @param cid
     * @return
     */
    @GetMapping("group/param/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupsWithParam(@PathVariable("cid") Long cid){
        List<SpecGroup> groups = this.specificationService.queryGroupsWithParam(cid);
        if(CollectionUtils.isEmpty(groups)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(groups);
    }
}
