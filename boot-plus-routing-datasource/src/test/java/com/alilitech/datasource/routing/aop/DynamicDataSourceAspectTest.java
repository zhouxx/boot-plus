package com.alilitech.datasource.routing.aop;

import com.alilitech.datasource.routing.DataSourceContextHolder;
import com.alilitech.datasource.routing.annotation.DynamicSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 同一个切面里，方法注解和类注解两段通知的行为。在 IDE 里对单个测试方法 Debug 即可。
 */
class DynamicDataSourceAspectTest {

    private static AnnotationConfigApplicationContext context;

    private static MethodLevelService methodLevelService;
    private static ClassLevelService classLevelService;
    private static OuterService outerService;

    @BeforeAll
    static void start() {
        context = new AnnotationConfigApplicationContext(Config.class);
        methodLevelService = context.getBean(MethodLevelService.class);
        classLevelService = context.getBean(ClassLevelService.class);
        outerService = context.getBean(OuterService.class);
    }

    @AfterAll
    static void stop() {
        if (context != null) {
            context.close();
        }
    }

    @AfterEach
    void clearHolder() {
        DataSourceContextHolder.clearDataSource();
    }

    @Test
    @DisplayName("方法注解：进入时切到声明的数据源，返回后清空")
    void methodLevelSwitchesAndClears() {
        assertEquals("ds_method", methodLevelService.literal());
        assertNull(DataSourceContextHolder.getDataSource());
    }

    @Test
    @DisplayName("方法注解 runtime=true：不改、也不清调用方已经放进去的数据源")
    void methodLevelRuntimeLeavesExternalValue() {
        DataSourceContextHolder.setDataSource("ds_external");

        assertEquals("ds_external", methodLevelService.runtime());
        assertEquals("ds_external", DataSourceContextHolder.getDataSource());
    }

    @Test
    @DisplayName("类注解：没有方法注解的方法走类切面")
    void classLevelSwitchesAndClears() {
        assertEquals("ds_class", classLevelService.inherit());
        assertNull(DataSourceContextHolder.getDataSource());
    }

    @Test
    @DisplayName("类和方法都有注解：方法注解生效，且只切一次")
    void methodAnnotationOverridesClass() {
        assertEquals("ds_method_override", classLevelService.override());
        assertNull(DataSourceContextHolder.getDataSource());
    }

    @Test
    @DisplayName("方法抛异常：异常抛出，ThreadLocal 仍然被清空")
    void exceptionStillClearsHolder() {
        IllegalStateException thrown = assertThrows(IllegalStateException.class, methodLevelService::throwing);
        assertEquals("boom", thrown.getMessage());
        assertNull(DataSourceContextHolder.getDataSource());
    }

    @Test
    @DisplayName("外层 runtime=true 再调内层字面量：内层切面的 finally 会清掉外层的值")
    void innerSwitchClearsCallerRuntimeValue() {
        DataSourceContextHolder.setDataSource("ds_external");

        assertEquals("ds_method", outerService.outer());
        assertNull(DataSourceContextHolder.getDataSource());
    }

    @Test
    @DisplayName("类切点排除带方法注解的方法，避免同一次调用被通知两次")
    void classPointcutExcludesAnnotatedMethod() throws Exception {
        AspectJExpressionPointcut classLevel = new AspectJExpressionPointcut();
        classLevel.setExpression("@within(com.alilitech.datasource.routing.annotation.DynamicSource)"
                + " && !@annotation(com.alilitech.datasource.routing.annotation.DynamicSource)");

        assertEquals(true, classLevel.matches(ClassLevelService.class.getMethod("inherit"), ClassLevelService.class));
        assertEquals(false, classLevel.matches(ClassLevelService.class.getMethod("override"), ClassLevelService.class));
        assertEquals(false, classLevel.matches(MethodLevelService.class.getMethod("literal"), MethodLevelService.class));
    }

    public static class MethodLevelService {

        @DynamicSource("ds_method")
        public String literal() {
            return DataSourceContextHolder.getDataSource();
        }

        @DynamicSource(runtime = true)
        public String runtime() {
            return DataSourceContextHolder.getDataSource();
        }

        @DynamicSource("ds_method")
        public String throwing() {
            throw new IllegalStateException("boom");
        }
    }

    @DynamicSource("ds_class")
    public static class ClassLevelService {

        public String inherit() {
            return DataSourceContextHolder.getDataSource();
        }

        @DynamicSource("ds_method_override")
        public String override() {
            return DataSourceContextHolder.getDataSource();
        }
    }

    public static class OuterService {

        private final MethodLevelService inner;

        OuterService(MethodLevelService inner) {
            this.inner = inner;
        }

        @DynamicSource(runtime = true)
        public String outer() {
            return inner.literal();
        }
    }

    @Configuration
    @EnableAspectJAutoProxy
    public static class Config {

        @Bean
        public DynamicDataSourceAspect dynamicDataSourceAspect() {
            return new DynamicDataSourceAspect();
        }

        @Bean
        public MethodLevelService methodLevelService() {
            return new MethodLevelService();
        }

        @Bean
        public ClassLevelService classLevelService() {
            return new ClassLevelService();
        }

        @Bean
        public OuterService outerService(MethodLevelService methodLevelService) {
            return new OuterService(methodLevelService);
        }
    }
}
