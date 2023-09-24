package fun.linhui.serialize.processor;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import java.lang.reflect.Method;

/**
 * @author linhui
 *  2022/1/24
 */
public abstract class BaseProcessor extends AbstractProcessor {

    /**
     * 用于在编译器打印消息的组件
     */
    public Messager messager;

    /**
     * 语法树
     */
    public JavacTrees trees;

    /**
     * 用来构造语法树节点
     */
    public TreeMaker treeMaker;

    /**
     * 用于创建标识符的对象
     */
    public Names names;

    @Override
    public final synchronized void init(ProcessingEnvironment processingEnv) {
        ProcessingEnvironment unwrappedprocessingEnv = jbUnwrap(ProcessingEnvironment.class, processingEnv);
        super.init(unwrappedprocessingEnv);
        this.messager = unwrappedprocessingEnv.getMessager();
        this.trees = JavacTrees.instance(unwrappedprocessingEnv);
        Context context = ((JavacProcessingEnvironment) unwrappedprocessingEnv).getContext();
        this.treeMaker = TreeMaker.instance(context);
        this.names = Names.instance(context);
    }
    private static <T> T jbUnwrap(Class<? extends T> iface, T wrapper) {
        T unwrapped = null;
        try {
            final Class<?> apiWrappers = wrapper.getClass().getClassLoader().loadClass("org.jetbrains.jps.javac.APIWrappers");
            final Method unwrapMethod = apiWrappers.getDeclaredMethod("unwrap", Class.class, Object.class);
            unwrapped = iface.cast(unwrapMethod.invoke(null, iface, wrapper));
        }
        catch (Throwable ignored) {}
        return unwrapped != null? unwrapped : wrapper;
    }




}
