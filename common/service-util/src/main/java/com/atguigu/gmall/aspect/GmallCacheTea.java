package com.atguigu.gmall.aspect;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//@Target(ElementType.METHOD)
//@Retention(RetentionPolicy.RUNTIME)
public @interface GmallCacheTea {

    String skuCache() default "sku";

    String skuType() default "str";

}
