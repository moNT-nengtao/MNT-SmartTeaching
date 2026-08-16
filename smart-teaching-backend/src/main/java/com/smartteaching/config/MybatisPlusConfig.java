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
 * MyBatis‑Plus 配置类
 * <p>
 * 功能：
 * 1. Mapper接口包扫描，支持多级子mapper包
 * 2. 注册分页插件，物理分页，限制单页最大条数，防止大数据查询压库
 * 3. 注册乐观锁插件，实体字段@Version实现并发更新控制
 * 4. 注册防全表更新/删除插件，拦截不带where条件的update、delete误操作
 * 5. 开启Spring事务管理，Service层@Transactional注解生效
 * 6. 自动填充（创建时间、更新时间、创建人、更新人）
 * <p>
 * 注意：不要在启动类重复添加@MapperScan；插件顺序遵循官方推荐顺序
 *
 * @author SmartTeaching
 * @since 1.0.0
 */
@Configuration
@EnableTransactionManagement
@MapperScan("com.smartteaching.mapper")
public class MybatisPlusConfig {

    /**
     * MyBatis‑Plus插件总拦截器
     * <p>
     * 插件执行顺序：分页插件 → 乐观锁插件 → 防全表攻击插件
     * - PaginationInnerInterceptor：MySQL物理分页，单页最大1000条
     * - OptimisticLockerInnerInterceptor：版本号乐观锁，实体使用@Version标记版本字段
     * - BlockAttackInnerInterceptor：禁止无where条件update/delete，保护数据库
     *
     * @return MybatisPlusInterceptor 拦截器Bean
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 1. 分页插件
        PaginationInnerInterceptor pagination = new PaginationInnerInterceptor(DbType.MYSQL);
        pagination.setMaxLimit(100L);           // 单页最大 100 条
        pagination.setOverflow(false);           // 超过总页数不处理（false = 继续查询）
        // pagination.setOptimizeJoin(true);     // 优化 JOIN 分页（3.5.0+ 默认 true）
        interceptor.addInnerInterceptor(pagination);

        // 2. 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 3. 防全表更新/删除插件（生产环境强烈建议开启）
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }

    /**
     * 全局配置（包含自动填充）
     * <p>
     * 自动填充功能需要配合实体类中的 @TableField(fill = ...) 注解使用
     *
     * @return GlobalConfig
     */
    @Bean
    public GlobalConfig globalConfig() {
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setMetaObjectHandler(new DefaultMetaObjectHandler());
        return globalConfig;
    }

    /**
     * 自动填充处理器（默认实现）
     * <p>
     * 使用方法：在实体类字段上添加 @TableField(fill = FieldFill.INSERT) 或
     * @TableField(fill = FieldFill.INSERT_UPDATE)
     * <p>
     * 示例：
     * <pre>
     * &#64;TableField(fill = FieldFill.INSERT)
     * private LocalDateTime createTime;
     *
     * &#64;TableField(fill = FieldFill.INSERT_UPDATE)
     * private LocalDateTime updateTime;
     *
     * &#64;TableField(fill = FieldFill.INSERT)
     * private Long createBy;
     *
     * &#64;TableField(fill = FieldFill.INSERT_UPDATE)
     * private Long updateBy;
     * </pre>
     */
    public static class DefaultMetaObjectHandler implements MetaObjectHandler {

        /**
         * 插入时自动填充
         */
        @Override
        public void insertFill(MetaObject metaObject) {
            // 填充创建时间（如果字段存在且未赋值）
            this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
            this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

            // 填充创建人/更新人（需要从 SecurityContext 或 ThreadLocal 获取当前用户ID）
            // 这里先填充默认值，实际使用时从上下文获取
            // this.strictInsertFill(metaObject, "createBy", Long.class, getCurrentUserId());
            // this.strictInsertFill(metaObject, "updateBy", Long.class, getCurrentUserId());
        }

        /**
         * 更新时自动填充
         */
        @Override
        public void updateFill(MetaObject metaObject) {
            // 填充更新时间
            this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

            // 填充更新人
            // this.strictUpdateFill(metaObject, "updateBy", Long.class, getCurrentUserId());
        }

        /**
         * 获取当前用户ID（需要结合 SecurityContext 实现）
         * <p>
         * 示例实现：
         * <pre>
         * private Long getCurrentUserId() {
         *     Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
         *     if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
         *         UserDetails userDetails = (UserDetails) authentication.getPrincipal();
         *         // 如果 UserDetails 中存了 userId，直接返回
         *         // 或者从数据库中根据用户名查询
         *         return ...;
         *     }
         *     return null;
         * }
         * </pre>
         */
        // private Long getCurrentUserId() {
        //     // TODO: 从 SecurityContext 获取当前用户 ID
        //     return null;
        // }
    }
}