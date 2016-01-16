package me.olivervscreeper.networkutilities.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created on 28/11/2014. Basic interface holding the command Annotation. Requires a command name,
 * and a permission set.
 *
 * @author OliverVsCreeper
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

    String label();

    String permission() default "none";

    int priority() default 0;

}
