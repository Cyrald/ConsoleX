package command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * Annotation to define command aliases.
 * A command can have multiple aliases that can be used to invoke it.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandAlias {
    
    /**
     * Array of aliases for the command.
     * 
     * @return String array of command aliases
     */
    String[] value();
}
