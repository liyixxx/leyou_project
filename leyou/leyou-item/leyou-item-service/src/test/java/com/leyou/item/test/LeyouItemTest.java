package com.leyou.item.test;

import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.pojo.SpecGroup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author 柒
 * @date 2020-02-22 12:37
 * @Description:
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class LeyouItemTest {

    @Autowired
    private SpecGroupMapper groupMapper ;

    @Test
    public void testIns(){
        SpecGroup group = new SpecGroup();
        group.setCid((long) 500);
        group.setName("testName");
        groupMapper.insertSelective(group);
    }
}
