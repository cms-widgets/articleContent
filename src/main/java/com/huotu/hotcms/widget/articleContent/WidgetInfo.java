/*
 * 版权所有:杭州火图科技有限公司
 * 地址:浙江省杭州市滨江区西兴街道阡陌路智慧E谷B幢4楼
 *
 * (c) Copyright Hangzhou Hot Technology Co., Ltd.
 * Floor 4,Block B,Wisdom E Valley,Qianmo Road,Binjiang District
 * 2013-2016. All rights reserved.
 */

package com.huotu.hotcms.widget.articleContent;

import com.huotu.hotcms.service.common.ArticleSource;
import com.huotu.hotcms.service.common.ContentType;
import com.huotu.hotcms.service.common.PageType;
import com.huotu.hotcms.service.entity.Article;
import com.huotu.hotcms.service.entity.Category;
import com.huotu.hotcms.service.entity.Site;
import com.huotu.hotcms.service.repository.ArticleRepository;
import com.huotu.hotcms.service.repository.CategoryRepository;
import com.huotu.hotcms.widget.CMSContext;
import com.huotu.hotcms.widget.ComponentProperties;
import com.huotu.hotcms.widget.PreProcessWidget;
import com.huotu.hotcms.widget.Widget;
import com.huotu.hotcms.widget.WidgetStyle;
import me.jiangcai.lib.resource.service.ResourceService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.util.NumberUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;


/**
 * @author CJ
 */
public class WidgetInfo implements Widget, PreProcessWidget {
    public static final String CONTENT_ID="contentId";
    public static final String CONTENT="abstractContent";

    @Override
    public String groupId() {
        return "com.huotu.hotcms.widget.articleContent";
    }

    @Override
    public String widgetId() {
        return "articleContent";
    }

    @Override
    public String name(Locale locale) {
        if (locale.equals(Locale.CHINA)) {
            return "文章内容";
        }
        return "articleContent";
    }

    @Override
    public String description(Locale locale) {
        if (locale.equals(Locale.CHINA)) {
            return "这是一个文章内容控件，你可以对组件进行自定义修改。";
        }
        return "This is a article Content,  you can make custom change the component.";
    }

    @Override
    public String dependVersion() {
        return "1.0-SNAPSHOT";
    }

    @Override
    public WidgetStyle[] styles() {
        return new WidgetStyle[]{new DefaultWidgetStyle()};
    }

    @Override
    public Resource widgetDependencyContent(MediaType mediaType) {
        if (mediaType.equals(Widget.Javascript))
            return new ClassPathResource("js/widgetInfo.js", getClass().getClassLoader());
        return null;
    }

    @Override
    public Map<String, Resource> publicResources() {
        Map<String, Resource> map = new HashMap<>();
        map.put("thumbnail/defaultStyleThumbnail.png", new ClassPathResource("thumbnail/defaultStyleThumbnail.png"
                , getClass().getClassLoader()));
        return map;
    }

    @Override
    public void valid(String styleId, ComponentProperties componentProperties) throws IllegalArgumentException {
        WidgetStyle style = WidgetStyle.styleByID(this, styleId);
        //加入控件独有的属性验证

    }

    @Override
    public Class springConfigClass() {
        return null;
    }


    @Override
    public ComponentProperties defaultProperties(ResourceService resourceService) throws IOException {
        ComponentProperties properties = new ComponentProperties();
        Site site = CMSContext.RequestContext().getSite();
        ArticleRepository articleRepository = CMSContext.RequestContext().getWebApplicationContext()
                .getBean(ArticleRepository.class);
        List<Article> articles = articleRepository.findByCategory_Site(site);
        if (articles.isEmpty()) {
            CategoryRepository categoryRepository = CMSContext.RequestContext().getWebApplicationContext()
                    .getBean(CategoryRepository.class);
            Category category = new Category();
            category.setSite(site);
            category.setContentType(ContentType.Article);
            category.setSerial(UUID.randomUUID().toString());
            category.setName("文章数据源");
            category.setCreateTime(LocalDateTime.now());
            category = categoryRepository.save(category);
            Article article = new Article();
            article.setTitle("文章标题");
            article.setContent("文章内容");
            article.setCategory(category);
            article.setCreateTime(LocalDateTime.now());
            article.setAuthor("system");
            article.setDescription("文章描述");
            article.setArticleSource(ArticleSource.ORIGINAL);
            article.setLauds(120);
            article.setUnlauds(10);
            article.setScans(130);
            article = articleRepository.saveAndFlush(article);
            properties.put(CONTENT_ID, article.getId());
        } else {
            properties.put(CONTENT_ID, articles.get(0).getId());
        }
        return properties;
    }

    @Override
    public PageType supportedPageType() {
        return PageType.DataContent;
    }

    @Override
    public void prepareContext(WidgetStyle style, ComponentProperties properties, Map<String, Object> variables
            , Map<String, String> parameters) {
        if (!variables.containsKey(CONTENT) || variables.get(CONTENT)==null){
            Long contentId = NumberUtils.parseNumber(properties.get(CONTENT_ID).toString(),Long.class);
            ArticleRepository articleRepository = CMSContext.RequestContext().getWebApplicationContext().getBean(ArticleRepository.class);
            Article article = articleRepository.findOne(contentId);
            variables.put(CONTENT,article);
        }
    }
}
