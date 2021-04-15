package com.lqy.community;

import com.lqy.community.dao.DiscussPostMapper;
import com.lqy.community.dao.elasticsearch.DiscussPostRepository;
import com.lqy.community.entity.DiscussPost;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchTests {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticTemplate;

    @Test
    public void testInsert(){
        discussRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussRepository.save(discussPostMapper.selectDiscussPostById(243));
    }

    @Test
    public void testInsertList(){
        discussRepository.saveAll(discussPostMapper.selectDiscussPosts(101, 0, 100));
        discussRepository.saveAll(discussPostMapper.selectDiscussPosts(102, 0, 100));
        discussRepository.saveAll(discussPostMapper.selectDiscussPosts(103, 0, 100));
        discussRepository.saveAll(discussPostMapper.selectDiscussPosts(111, 0, 100));
        discussRepository.saveAll(discussPostMapper.selectDiscussPosts(112, 0, 100));
        discussRepository.saveAll(discussPostMapper.selectDiscussPosts(131, 0, 100));
        discussRepository.saveAll(discussPostMapper.selectDiscussPosts(132, 0, 100));
        discussRepository.saveAll(discussPostMapper.selectDiscussPosts(133, 0, 100));
        discussRepository.saveAll(discussPostMapper.selectDiscussPosts(134, 0, 100));
    }

    @Test
    public void testUpdate(){
        DiscussPost post = discussPostMapper.selectDiscussPostById(211);
        post.setContent("憨包憨包憨包");
        discussRepository.save(post);
    }

    @Test
    public void testDelete(){
//        discussRepository.deleteById(211);
        discussRepository.deleteAll();
    }

    @Test
    public void testSearchByRepository() {
        //构造查询方法
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                //构造搜索条件
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                //构造排序条件
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                //分页
                .withPageable(PageRequest.of(0, 10))
                //高亮显示字段
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();


        //底层获取到了高亮显示的值，但是没有返回 怎么处理？还不会
        //解析结果
        Page<DiscussPost> page = discussRepository.search(searchQuery);
        System.out.println(page.getTotalElements());
        System.out.println(page.getTotalPages());
        System.out.println(page.getNumber());
        System.out.println(page.getSize());
        for (DiscussPost post : page){
            System.out.println(post);
        }
    }

    @Test
    public void testSearchByTemplate(){
        //构造查询方法
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                //构造搜索条件
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                //构造排序条件
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                //分页
                .withPageable(PageRequest.of(0, 10))
                //高亮显示字段
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

//        elasticTemplate.queryForPage(searchQuery, DiscussPost.class, )

    }
}
