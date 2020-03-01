package com.leyou.item.service;

import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author: 柒
 * @createDate: 2020/2/22
 * @TODO: 规格参数
 */
@Service
public class SpecificationService {

    @Autowired
    private SpecParamMapper paramMapper;

    @Autowired
    private SpecGroupMapper groupMapper;


    /**
     * 根据分类id查询分组信息
     *
     * @param cid
     * @return
     */
    public List<SpecGroup> queryGroupsByCid(Long cid) {
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);
        return this.groupMapper.select(specGroup);
    }

    /**
     * 多条件查询 使用不同的查询条件完成规格参数详情的查询
     *
     * @param gid
     * @param cid
     * @param generic
     * @param searching
     * @return
     */
    public List<SpecParam> queryParamByGid(Long gid, Long cid, Boolean generic, Boolean searching) {
        SpecParam specParam = new SpecParam();
        specParam.setCid(cid);
        specParam.setGroupId(gid);
        specParam.setGeneric(generic);
        specParam.setSearching(searching);
        return this.paramMapper.select(specParam);
    }

    /**
     * 新增分组
     * -- 添加事务
     *
     * @param group
     */
    @Transactional
    public void saveGroup(SpecGroup group) {
        this.groupMapper.insertSelective(group);
    }

    /**
     * 新增参数信息
     *
     * @param param
     */
    @Transactional
    public void saveParam(SpecParam param) {
        this.paramMapper.insertSelective(param);
    }

    /**
     * 修改参数信息
     *
     * @param param
     */
    @Transactional
    public void updateParam(SpecParam param) {
        this.paramMapper.updateByPrimaryKeySelective(param);
    }

    /**
     * 根据主键id删除参数信息
     *
     * @param id
     */
    @Transactional
    public void deleteParamById(Long id) {
        this.paramMapper.deleteByPrimaryKey(id);
    }

    /**
     * 根据cid查询所有的规格参数信息
     *
     * @param cid
     * @return
     */
    public List<SpecGroup> queryGroupsWithParam(Long cid) {
        List<SpecGroup> groups = this.queryGroupsByCid(cid);
        groups.forEach(group -> {
            List<SpecParam> list = this.queryParamByGid(group.getId(), null, null, null);
            group.setParams(list);
        });
        return groups;
    }
}
