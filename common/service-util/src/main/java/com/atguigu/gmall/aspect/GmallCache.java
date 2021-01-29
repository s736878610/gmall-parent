package com.atguigu.gmall.aspect;

import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;

import javax.validation.constraints.NotNull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)// 生效时机 runtime
@Target({ElementType.METHOD})// 作用范围 method
public @interface GmallCache {

    @NotNull
    String prefix();
}
