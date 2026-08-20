package com.smartteaching.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.LocalDateTime;

/**
 * MyBatis‑Plus配置
 * 分页、乐观锁、防止全表更新删除、自动填充、开启事务
 */
@Configuration
@EnableTransactionManagement
@MapperScan("com.smartteaching.mapper")
public class MybatisPlusConfig {

    /**
     * MP插件集合
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 分页插件，mysql数据库，单页最多100条
        PaginationInnerInterceptor pagination = new PaginationInnerInterceptor(DbType.MYSQL);
        pagination.setMaxLimit(100L);
        pagination.setOverflow(false);
        interceptor.addInnerInterceptor(pagination);

        // 乐观锁，实体字段加@Version
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 禁止不带where条件update/delete，防止误删全表
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }

    /**
     * MP全局配置，注册自动填充处理器
     */
    @Bean
    public GlobalConfig globalConfig() {
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setMetaObjectHandler(new DefaultMetaObjectHandler());
        return globalConfig;
    }

    /**
     * 自动填充处理器
     * 实体字段上加 @TableField(fill = xxx) 才会生效
     */
    public static class DefaultMetaObjectHandler implements MetaObjectHandler {

        /**
         * 新增数据时自动填充
         */
        @Override
        public void insertFill(MetaObject metaObject) {
            // 创建时间、更新时间自动赋值当前时间
            this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
            this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

            // createBy、updateBy 需要拿到登录用户ID，这里先注释
            // this.strictInsertFill(metaObject, "createBy", Long.class, getCurrentUserId());
            // this.strictInsertFill(metaObject, "updateBy", Long.class, getCurrentUserId());
        }

        /**
         * 修改数据时自动填充
         */
        @Override
        public void updateFill(MetaObject metaObject) {
            // 更新时间自动刷新
            this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

            // this.strictUpdateFill(metaObject, "updateBy", Long.class, getCurrentUserId());
        }

    }
}
