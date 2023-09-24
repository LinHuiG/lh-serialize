package fun.linhui.serialize.utils;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Names;

import javax.lang.model.element.Modifier;
import java.util.Set;
import java.util.function.Function;

public class ProcessUtil {
    public static final String BYTE_ARRAY = "byte[]";
    public static final String VOID = "void";
    public static final String INT = "int";
    public static final String SUPER = "super";

    public static final String THIS = "this";
    public static final String CLASS = "class";
    public static final String OBJECT = "Object";
    public static final String JAVA_LANG_OBJECT = "java.lang.Object";
    public static final String JAVA_LANG_STRING = "java.lang.String";

    /**
     * 判断是否是合法的字段
     *
     * @param jcTree 语法树节点
     * @return 是否是合法字段
     */
    private static boolean isValidField(JCTree jcTree) {
        if (jcTree.getKind().equals(JCTree.Kind.VARIABLE)) {
            JCTree.JCVariableDecl jcVariable = (JCTree.JCVariableDecl) jcTree;

            Set<Modifier> flagSets = jcVariable.mods.getFlags();
            return (!flagSets.contains(Modifier.STATIC)
                    && !flagSets.contains(Modifier.FINAL));
        }

        return false;
    }

    /**
     * 获取字段的语法树节点的集合
     *
     * @param jcClass 类的语法树节点
     * @return 字段的语法树节点的集合
     */
    public static List<JCTree.JCVariableDecl> getJCVariables(JCTree.JCClassDecl jcClass, Function<JCTree.JCVariableDecl,Boolean> flitter) {
        ListBuffer<JCTree.JCVariableDecl> jcVariables = new ListBuffer<>();

        //遍历jcClass的所有内部节点，可能是字段，方法等等
        for (JCTree jcTree : jcClass.defs) {
            //找出所有set方法节点，并添加
            if (isValidField(jcTree)) {
                //注意这个com.sun.tools.javac.util.List的用法，不支持链式操作，更改后必须赋值
                if (flitter.apply((JCTree.JCVariableDecl) jcTree)){
                    jcVariables.append((JCTree.JCVariableDecl) jcTree);
                }
            }
        }

        return jcVariables.toList();
    }


    public static JCTree.JCMethodDecl getMethod(JCTree.JCClassDecl jcClass, String methodName, String returnType, String[] paramsType) {

        for (JCTree jcTree : jcClass.defs) {
            if (jcTree.getKind().equals(JCTree.Kind.METHOD)) {
                JCTree.JCMethodDecl jcMethod = (JCTree.JCMethodDecl) jcTree;
                if (methodName.equals(jcMethod.name.toString())
                        && returnType.equals(jcMethod.restype.type.toString())
                        && jcMethod.params.size() == paramsType.length) {
                    for (int i = 0; i <= paramsType.length; i++) {
                        if (i == paramsType.length) return jcMethod;
                        if (!paramsType[i].equals(jcMethod.params.get(i).vartype.type.toString())) break;
                    }
                }
            }
        }
        return null;
    }

    public static JCTree.JCMethodDecl defSimpleMethod(TreeMaker treeMaker, Names names, int methodFlag, JCTree.JCExpression returnType, String methodName, String... params) {
        List<JCTree.JCVariableDecl> paramsList;
        if (params!=null){
            JCTree.JCVariableDecl[] parameterList = new JCTree.JCVariableDecl[params.length];
            for (int i = 0; i < params.length; i++) {
                String[] param = params[i].split(" ");
                parameterList[i] = treeMaker.VarDef(
                        treeMaker.Modifiers(Flags.PARAMETER), //访问标志。极其坑爹！！！
                        names.fromString(param[1]), //名字
                        memberAccess(treeMaker, names, param[0]), //类型
                        null //初始化语句
                );
            }
            paramsList=List.from(parameterList);
        }else {
            paramsList=List.nil();
        }
        if (returnType == null) {
            returnType = treeMaker.TypeIdent(TypeTag.VOID);
        }
        return treeMaker.MethodDef(
                treeMaker.Modifiers(methodFlag),
                names.fromString(methodName),
                returnType,
                List.nil(), //泛型形参列表
                paramsList, //参数列表
                List.nil(), //异常列表
                treeMaker.Block(
                        0 //访问标志
                        , List.of(treeMaker.Return(
                                treeMaker.Literal(TypeTag.BOT, null)
                        )) //所有的语句
                ),
                null
        );
    }

    public static JCTree.JCExpression memberAccess(TreeMaker treeMaker, Names names, String components) {
        String[] componentArray = components.split("\\.");
        JCTree.JCExpression expr = treeMaker.Ident(names.fromString(componentArray[0]));
        for (int i = 1; i < componentArray.length; i++) {
            expr = treeMaker.Select(expr, names.fromString(componentArray[i]));
        }
        return expr;
    }

    public static String getParamStr(String type, String name) {
        return String.format("%s %s", type, name);
    }
}