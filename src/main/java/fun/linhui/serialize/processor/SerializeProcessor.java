package fun.linhui.serialize.processor;

import com.google.auto.service.AutoService;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Pair;
import fun.linhui.serialize.annotations.LHSerialize;
import fun.linhui.serialize.annotations.SerializeField;
import fun.linhui.serialize.annotations.SerializeListField;
import fun.linhui.serialize.annotations.SerializeObjField;
import fun.linhui.serialize.enums.SERIALIZE_TYPE;
import fun.linhui.serialize.utils.*;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author linhui
 * date 2023-08-27
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("fun.linhui.serialize.annotations.LHSerialize")
@AutoService(Processor.class)
public class SerializeProcessor extends BaseProcessor {
    private static final String BYTE_BUFFER_CLASS = "java.nio.ByteBuffer";
    private static final String BYTE_BUF_CLASS = "io.netty.buffer.ByteBuf";
    private static final String FIELD_BIT_SET_CLASS = "fun.linhui.serialize.utils.FieldBitSet";

    private static final String FIELD_BIT_SET_NAME = "fieldBitSet";
    private static final String FIELD_BIT_SET_SET_METHOD = ".set";
    private static final String FIELD_BIT_SET_INIT_METHOD = ".init";
    private static final String FIELD_BIT_SET_SERIALIZE_METHOD = ".serialize";
    private static final String BYTE_BUFFER_NAME = "byteBuffer";
    private static final String serializeMethodName = "serialize";
    private static final String serializeLEMethodName = "serializeLE";
    private static final String deSerializeMethodName = "deserialize";
    private static final String deSerializeLEMethodName = "deserializeLE";
    private static final String getByteLengthMethodName = "getByteLength";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //首先获取被LHSerialize注解标记的元素
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(LHSerialize.class);
        if (set.isEmpty()) return true;
        set.forEach(element -> {
            //获取当前元素的JCTree对象
            JCTree jcTree = trees.getTree(element);
            //JCTree利用的是访问者模式，将数据与数据的处理进行解耦，TreeTranslator就是访问者，这里我们重写访问类时的逻辑
            jcTree.accept(new TreeTranslator() {

                public void visitClassDef(JCTree.JCClassDecl jcClass) {
                    messager.printMessage(Diagnostic.Kind.NOTE, "@EmtSerialize process [" + jcClass.name.toString() + "] begin!");
                    //进行一些初始化工作
                    before(jcClass, element);
                    //增强方法
                    enhanceMethods();
                    //进行一些清理工作
                    after();
                    messager.printMessage(Diagnostic.Kind.NOTE, "@EmtSerialize process [" + jcClass.name.toString() + "] end!");
                }
            });
        });
        return true;
    }

    /**
     * 类的语法树节点
     */
    private JCTree.JCClassDecl jcClass;

    private LHSerialize emtSerialize;

    /**
     * 字段的语法树节点的集合
     */
    private List<JCTree.JCVariableDecl> fieldJCVariables;
    private int fieldNum;
    private JCTree.JCMethodDecl serializeMethodJCMethod_ByteBuffer;
    private JCTree.JCMethodDecl deSerializeMethodJCMethod_ByteBuffer;
    private JCTree.JCMethodDecl serializeMethodJCMethod_ByteBuf;
    private JCTree.JCMethodDecl deSerializeMethodJCMethod_ByteBuf;
    private JCTree.JCMethodDecl serializeLEMethodJCMethod_ByteBuf;
    private JCTree.JCMethodDecl deSerializeLEMethodJCMethod_ByteBuf;
    private JCTree.JCMethodDecl getByteLengthMethod;

    private final Map<String, Integer> genericIndexMap = new HashMap<>();

    private JCTree.JCExpression getGenericClass(String tClassName) {
        JCTree.JCExpression objFieldClz;
        if (genericIndexMap.containsKey(tClassName)) {
            //是泛型
            objFieldClz = treeMaker.Apply(
                    List.nil(),
                    ProcessUtil.memberAccess(treeMaker, names, SerializeUtils.GET_GENERIC_CLASS_NAME_BY_INDEX),
                    List.of(treeMaker.Ident(names.fromString(ProcessUtil.THIS)),
                            treeMaker.Select(
                                    ProcessUtil.memberAccess(treeMaker, names, jcClass.name.toString()),
                                    names.fromString(ProcessUtil.CLASS)
                            ),
                            treeMaker.Literal(genericIndexMap.get(tClassName)))
            );
        } else {
            objFieldClz = treeMaker.Select(
                    ProcessUtil.memberAccess(treeMaker, names, tClassName),
                    names.fromString(ProcessUtil.CLASS));
        }
        return objFieldClz;
    }

    private void before(JCTree.JCClassDecl jcClass, Element element) {
        this.emtSerialize = element.getAnnotationsByType(LHSerialize.class)[0];
        this.jcClass = jcClass;
        this.fieldJCVariables = ProcessUtil.getJCVariables(jcClass, jcVariableDecl -> {
            for (JCTree.JCAnnotation annotation : jcVariableDecl.mods.annotations) {
                Type type = annotation.annotationType.type;
                if (type != null) {
                    if (isSerializeAnnotation(type)) {
                        return true;
                    }
                }
            }
            return false;
        });
        this.fieldNum = this.fieldJCVariables.size();
        this.treeMaker.at(jcClass.pos);
        //泛型表初始化
        for (int i = 0; i < jcClass.typarams.size(); i++) {
            genericIndexMap.put(jcClass.typarams.get(i).name.toString(), i);
        }
        //初始化
        this.serializeMethodJCMethod_ByteBuffer = ProcessUtil.getMethod(jcClass, serializeMethodName, ProcessUtil.VOID, new String[]{BYTE_BUFFER_CLASS});
        this.deSerializeMethodJCMethod_ByteBuffer = ProcessUtil.getMethod(jcClass, deSerializeMethodName, ProcessUtil.VOID, new String[]{BYTE_BUFFER_CLASS});

        this.serializeMethodJCMethod_ByteBuf = ProcessUtil.getMethod(jcClass, serializeMethodName, ProcessUtil.VOID, new String[]{BYTE_BUF_CLASS});
        this.deSerializeMethodJCMethod_ByteBuf = ProcessUtil.getMethod(jcClass, deSerializeMethodName, ProcessUtil.VOID, new String[]{BYTE_BUF_CLASS});

        this.serializeLEMethodJCMethod_ByteBuf = ProcessUtil.getMethod(jcClass, serializeLEMethodName, ProcessUtil.VOID, new String[]{BYTE_BUF_CLASS});
        this.deSerializeLEMethodJCMethod_ByteBuf = ProcessUtil.getMethod(jcClass, deSerializeLEMethodName, ProcessUtil.VOID, new String[]{BYTE_BUF_CLASS});
        this.getByteLengthMethod = ProcessUtil.getMethod(jcClass, getByteLengthMethodName, ProcessUtil.INT, new String[]{});

        if (serializeMethodJCMethod_ByteBuffer == null) {
            serializeMethodJCMethod_ByteBuffer = ProcessUtil.defSimpleMethod(treeMaker, names,
                    Flags.PUBLIC, null, serializeMethodName, ProcessUtil.getParamStr(BYTE_BUFFER_CLASS, BYTE_BUFFER_NAME));
            jcClass.defs = jcClass.defs.append(serializeMethodJCMethod_ByteBuffer);
        }
        if (serializeMethodJCMethod_ByteBuf == null) {
            serializeMethodJCMethod_ByteBuf = ProcessUtil.defSimpleMethod(treeMaker, names,
                    Flags.PUBLIC, null, serializeMethodName, ProcessUtil.getParamStr(BYTE_BUF_CLASS, BYTE_BUFFER_NAME));
            jcClass.defs = jcClass.defs.append(serializeMethodJCMethod_ByteBuf);
        }
        if (serializeLEMethodJCMethod_ByteBuf == null) {
            serializeLEMethodJCMethod_ByteBuf = ProcessUtil.defSimpleMethod(treeMaker, names,
                    Flags.PUBLIC, null, serializeLEMethodName, ProcessUtil.getParamStr(BYTE_BUF_CLASS, BYTE_BUFFER_NAME));
            jcClass.defs = jcClass.defs.append(serializeLEMethodJCMethod_ByteBuf);
        }

        if (deSerializeMethodJCMethod_ByteBuffer == null) {
            deSerializeMethodJCMethod_ByteBuffer = ProcessUtil.defSimpleMethod(treeMaker, names,
                    Flags.PUBLIC, null, deSerializeMethodName, ProcessUtil.getParamStr(BYTE_BUFFER_CLASS, BYTE_BUFFER_NAME));
            jcClass.defs = jcClass.defs.append(deSerializeMethodJCMethod_ByteBuffer);
        }
        if (deSerializeMethodJCMethod_ByteBuf == null) {
            deSerializeMethodJCMethod_ByteBuf = ProcessUtil.defSimpleMethod(treeMaker, names,
                    Flags.PUBLIC, null, deSerializeMethodName, ProcessUtil.getParamStr(BYTE_BUF_CLASS, BYTE_BUFFER_NAME));
            jcClass.defs = jcClass.defs.append(deSerializeMethodJCMethod_ByteBuf);
        }
        if (deSerializeLEMethodJCMethod_ByteBuf == null) {
            deSerializeLEMethodJCMethod_ByteBuf = ProcessUtil.defSimpleMethod(treeMaker, names,
                    Flags.PUBLIC, null, deSerializeLEMethodName, ProcessUtil.getParamStr(BYTE_BUF_CLASS, BYTE_BUFFER_NAME));
            jcClass.defs = jcClass.defs.append(deSerializeLEMethodJCMethod_ByteBuf);
        }

        if (getByteLengthMethod == null) {
            getByteLengthMethod = ProcessUtil.defSimpleMethod(treeMaker, names,
                    Flags.PUBLIC, treeMaker.TypeIdent(TypeTag.INT), getByteLengthMethodName);
            jcClass.defs = jcClass.defs.append(getByteLengthMethod);
        }

    }


    private void enhanceMethods() {
        ListBuffer<JCTree.JCStatement> serializeJcS_ByteBuffer = new ListBuffer<>();
        ListBuffer<JCTree.JCStatement> serializeJcS_ByteBuf = new ListBuffer<>();
        ListBuffer<JCTree.JCStatement> serializeLEJcS_ByteBuf = new ListBuffer<>();

        ListBuffer<JCTree.JCStatement> deSerializeJcS_ByteBuffer = new ListBuffer<>();
        ListBuffer<JCTree.JCStatement> deSerializeJcS_ByteBuf = new ListBuffer<>();
        ListBuffer<JCTree.JCStatement> deSerializeLEJcS_ByteBuf = new ListBuffer<>();
        ListBuffer<JCTree.JCExpression> byteLengthAddJcS = new ListBuffer<>();

        //计算固定长度
        int byteLength = (fieldNum + 7) / 8;
        if (this.emtSerialize.type() == SERIALIZE_TYPE.INHERIT) {
            //如果需要序列化父类，则添加super项
            serializeJcS_ByteBuffer.append(treeMaker.Exec(generateSuperApply(serializeMethodName, BYTE_BUFFER_NAME)));
            serializeJcS_ByteBuf.append(treeMaker.Exec(generateSuperApply(serializeMethodName, BYTE_BUFFER_NAME)));
            serializeLEJcS_ByteBuf.append(treeMaker.Exec(generateSuperApply(serializeLEMethodName, BYTE_BUFFER_NAME)));
            deSerializeJcS_ByteBuffer.append(treeMaker.Exec(generateSuperApply(deSerializeMethodName, BYTE_BUFFER_NAME)));
            deSerializeJcS_ByteBuf.append(treeMaker.Exec(generateSuperApply(deSerializeMethodName, BYTE_BUFFER_NAME)));
            deSerializeLEJcS_ByteBuf.append(treeMaker.Exec(generateSuperApply(deSerializeLEMethodName, BYTE_BUFFER_NAME)));
            byteLengthAddJcS.append(generateSuperApply(getByteLengthMethodName));
        }
        //序列化BitSet
        initSerializeFieldBitSet(serializeJcS_ByteBuffer);
        initSerializeFieldBitSet(serializeJcS_ByteBuf);
        initSerializeFieldBitSet(serializeLEJcS_ByteBuf);
        initDeserializeFieldBitSet(deSerializeJcS_ByteBuffer);
        initDeserializeFieldBitSet(deSerializeJcS_ByteBuf);
        initDeserializeFieldBitSet(deSerializeLEJcS_ByteBuf);
        //序列化字段
        int fieldIndex = 0;
        for (JCTree.JCVariableDecl fieldJCVariable : fieldJCVariables) {
            for (JCTree.JCAnnotation annotation : fieldJCVariable.mods.annotations) {
                Type annType = annotation.annotationType.type;
                if (annType != null) {
                    if (isSerializeAnnotation(annType)) {
                        Type fieldType = fieldJCVariable.vartype.type;
                        int fieldByteLength = 0;
                        if (annotation.attribute.values.size() > 0) {
                            List<Pair<Symbol.MethodSymbol, Attribute>> annVals = annotation.attribute.values;
                            fieldByteLength = (int) ((Attribute.Constant) annVals.get(0).snd).value;
                        }
                        if (SerializeField.class.getName().equals(annType.toString())) {
                            if (fieldType.toString().equals(ProcessUtil.JAVA_LANG_STRING)) {
                                byteLengthAddJcS.append(generateApply(SerializeStringFieldUtils.GET_STRING_LENGTH_METHOD,
                                        List.of(getThisField(fieldJCVariable), treeMaker.Literal(fieldByteLength))));
                                serializeJcS_ByteBuffer.append(treeMaker.Exec(generateApply(SerializeStringFieldUtils.SERIALIZE_METHOD,
                                        List.of(getIdent(BYTE_BUFFER_NAME), getThisField(fieldJCVariable), treeMaker.Literal(fieldByteLength)))));
                                serializeJcS_ByteBuf.append(treeMaker.Exec(generateApply(SerializeStringFieldUtils.SERIALIZE_METHOD,
                                        List.of(getIdent(BYTE_BUFFER_NAME), getThisField(fieldJCVariable), treeMaker.Literal(fieldByteLength)))));
                                serializeLEJcS_ByteBuf.append(treeMaker.Exec(generateApply(SerializeStringFieldUtils.SERIALIZE_LE_METHOD,
                                        List.of(getIdent(BYTE_BUFFER_NAME), getThisField(fieldJCVariable), treeMaker.Literal(fieldByteLength)))));

                                deSerializeJcS_ByteBuffer.append(treeMaker.Exec(treeMaker.Assign(getThisField(fieldJCVariable), generateApply(SerializeStringFieldUtils.DESERIALIZE_METHOD,
                                        List.of(getIdent(FIELD_BIT_SET_NAME), treeMaker.Literal(fieldIndex), getIdent(BYTE_BUFFER_NAME), treeMaker.Literal(fieldByteLength))))));

                                deSerializeJcS_ByteBuf.append(treeMaker.Exec(treeMaker.Assign(getThisField(fieldJCVariable), generateApply(SerializeStringFieldUtils.DESERIALIZE_METHOD,
                                        List.of(getIdent(FIELD_BIT_SET_NAME), treeMaker.Literal(fieldIndex), getIdent(BYTE_BUFFER_NAME), treeMaker.Literal(fieldByteLength))))));

                                deSerializeLEJcS_ByteBuf.append(treeMaker.Exec(treeMaker.Assign(getThisField(fieldJCVariable), generateApply(SerializeStringFieldUtils.DESERIALIZE_LE_METHOD,
                                        List.of(getIdent(FIELD_BIT_SET_NAME), treeMaker.Literal(fieldIndex), getIdent(BYTE_BUFFER_NAME), treeMaker.Literal(fieldByteLength))))));


                            } else if (fieldType.toString().equals(ProcessUtil.BYTE_ARRAY)) {
                                byteLengthAddJcS.append(generateApply(SerializeByteArrayFieldUtils.GET_BYTE_ARRAY_LENGTH_METHOD,
                                        List.of(getThisField(fieldJCVariable), treeMaker.Literal(fieldByteLength))));

                                serializeJcS_ByteBuffer.append(treeMaker.Exec(generateApply(SerializeByteArrayFieldUtils.SERIALIZE_METHOD,
                                        List.of(getIdent(BYTE_BUFFER_NAME), getThisField(fieldJCVariable), treeMaker.Literal(fieldByteLength)))));
                                serializeJcS_ByteBuf.append(treeMaker.Exec(generateApply(SerializeByteArrayFieldUtils.SERIALIZE_METHOD,
                                        List.of(getIdent(BYTE_BUFFER_NAME), getThisField(fieldJCVariable), treeMaker.Literal(fieldByteLength)))));
                                serializeLEJcS_ByteBuf.append(treeMaker.Exec(generateApply(SerializeByteArrayFieldUtils.SERIALIZE_LE_METHOD,
                                        List.of(getIdent(BYTE_BUFFER_NAME), getThisField(fieldJCVariable), treeMaker.Literal(fieldByteLength)))));

                                deSerializeJcS_ByteBuffer.append(treeMaker.Exec(treeMaker.Assign(getThisField(fieldJCVariable), generateApply(SerializeByteArrayFieldUtils.DESERIALIZE_METHOD,
                                        List.of(getIdent(FIELD_BIT_SET_NAME), treeMaker.Literal(fieldIndex), getIdent(BYTE_BUFFER_NAME), getThisField(fieldJCVariable), treeMaker.Literal(fieldByteLength))))));

                                deSerializeJcS_ByteBuf.append(treeMaker.Exec(treeMaker.Assign(getThisField(fieldJCVariable), generateApply(SerializeByteArrayFieldUtils.DESERIALIZE_METHOD,
                                        List.of(getIdent(FIELD_BIT_SET_NAME), treeMaker.Literal(fieldIndex), getIdent(BYTE_BUFFER_NAME), getThisField(fieldJCVariable), treeMaker.Literal(fieldByteLength))))));

                                deSerializeLEJcS_ByteBuf.append(treeMaker.Exec(treeMaker.Assign(getThisField(fieldJCVariable), generateApply(SerializeByteArrayFieldUtils.DESERIALIZE_LE_METHOD,
                                        List.of(getIdent(FIELD_BIT_SET_NAME), treeMaker.Literal(fieldIndex), getIdent(BYTE_BUFFER_NAME), getThisField(fieldJCVariable), treeMaker.Literal(fieldByteLength))))));

                            } else {
                                fieldByteLength = SerializeFieldUtils.getByteLength(fieldType);
                                if (isBasedVar(fieldType)) {
                                    byteLength += fieldByteLength;
                                } else {
                                    byteLengthAddJcS.append(generateApply(SerializeFieldUtils.GET_FIELD_LENGTH_METHOD,
                                            List.of(getThisField(fieldJCVariable))));
                                }
                                serializeJcS_ByteBuffer.append(treeMaker.Exec(generateApply(SerializeFieldUtils.SERIALIZE_METHOD,
                                        List.of(getIdent(BYTE_BUFFER_NAME), getThisField(fieldJCVariable)))));
                                serializeJcS_ByteBuf.append(treeMaker.Exec(generateApply(SerializeFieldUtils.SERIALIZE_METHOD,
                                        List.of(getIdent(BYTE_BUFFER_NAME), getThisField(fieldJCVariable)))));
                                serializeLEJcS_ByteBuf.append(treeMaker.Exec(generateApply(SerializeFieldUtils.SERIALIZE_LE_METHOD,
                                        List.of(getIdent(BYTE_BUFFER_NAME), getThisField(fieldJCVariable)))));
                                if (isBasedVar(fieldType)){
                                    deSerializeJcS_ByteBuffer.append(treeMaker.Exec(treeMaker.Assign(getThisField(fieldJCVariable), generateApply(SerializeFieldUtils.getDeserializeMethodName(fieldType),
                                            List.of(getIdent(BYTE_BUFFER_NAME))))));

                                    deSerializeJcS_ByteBuf.append(treeMaker.Exec(treeMaker.Assign(getThisField(fieldJCVariable), generateApply(SerializeFieldUtils.getDeserializeMethodName(fieldType),
                                            List.of(getIdent(BYTE_BUFFER_NAME))))));

                                    deSerializeLEJcS_ByteBuf.append(treeMaker.Exec(treeMaker.Assign(getThisField(fieldJCVariable), generateApply(SerializeFieldUtils.getDeserializeLEMethodName(fieldType),
                                            List.of(getIdent(BYTE_BUFFER_NAME))))));
                                }else {
                                    deSerializeJcS_ByteBuffer.append(treeMaker.Exec(treeMaker.Assign(getThisField(fieldJCVariable), generateApply(SerializeFieldUtils.getDeserializeMethodName(fieldType),
                                            List.of(getIdent(FIELD_BIT_SET_NAME), treeMaker.Literal(fieldIndex), getIdent(BYTE_BUFFER_NAME))))));

                                    deSerializeJcS_ByteBuf.append(treeMaker.Exec(treeMaker.Assign(getThisField(fieldJCVariable), generateApply(SerializeFieldUtils.getDeserializeMethodName(fieldType),
                                            List.of(getIdent(FIELD_BIT_SET_NAME), treeMaker.Literal(fieldIndex), getIdent(BYTE_BUFFER_NAME))))));

                                    deSerializeLEJcS_ByteBuf.append(treeMaker.Exec(treeMaker.Assign(getThisField(fieldJCVariable), generateApply(SerializeFieldUtils.getDeserializeLEMethodName(fieldType),
                                            List.of(getIdent(FIELD_BIT_SET_NAME), treeMaker.Literal(fieldIndex), getIdent(BYTE_BUFFER_NAME))))));
                                }

                            }
                        } else if (SerializeListField.class.getName().equals(annType.toString())) {
                            JCTree.JCExpression fClassJcE = ((JCTree.JCTypeApply) fieldJCVariable.vartype).arguments.get(0);
                            String listFieldTypeStr = ((JCTree.JCIdent) fClassJcE).sym.toString();
                            byteLengthAddJcS.append(generateApply(SerializeFieldListUtil.GET_LIST_LENGTH_METHOD,
                                    List.of(getThisField(fieldJCVariable), getGenericClass(listFieldTypeStr))));

                            serializeJcS_ByteBuffer.append(treeMaker.Exec(generateApply(SerializeFieldListUtil.SERIALIZE_METHOD,
                                    List.of(getIdent(BYTE_BUFFER_NAME), getThisField(fieldJCVariable), getGenericClass(listFieldTypeStr)))));
                            serializeJcS_ByteBuf.append(treeMaker.Exec(generateApply(SerializeFieldListUtil.SERIALIZE_METHOD,
                                    List.of(getIdent(BYTE_BUFFER_NAME), getThisField(fieldJCVariable), getGenericClass(listFieldTypeStr)))));
                            serializeLEJcS_ByteBuf.append(treeMaker.Exec(generateApply(SerializeFieldListUtil.SERIALIZE_LE_METHOD,
                                    List.of(getIdent(BYTE_BUFFER_NAME), getThisField(fieldJCVariable), getGenericClass(listFieldTypeStr)))));

                            deSerializeJcS_ByteBuffer.append(treeMaker.Exec(treeMaker.Assign(getThisField(fieldJCVariable), generateApply(SerializeFieldListUtil.DESERIALIZE_METHOD,
                                    List.of(getIdent(FIELD_BIT_SET_NAME), treeMaker.Literal(fieldIndex), getIdent(BYTE_BUFFER_NAME), getGenericClass(listFieldTypeStr))))));

                            deSerializeJcS_ByteBuf.append(treeMaker.Exec(treeMaker.Assign(getThisField(fieldJCVariable), generateApply(SerializeFieldListUtil.DESERIALIZE_METHOD,
                                    List.of(getIdent(FIELD_BIT_SET_NAME), treeMaker.Literal(fieldIndex), getIdent(BYTE_BUFFER_NAME), getGenericClass(listFieldTypeStr))))));

                            deSerializeLEJcS_ByteBuf.append(treeMaker.Exec(treeMaker.Assign(getThisField(fieldJCVariable), generateApply(SerializeFieldListUtil.DESERIALIZE_LE_METHOD,
                                    List.of(getIdent(FIELD_BIT_SET_NAME), treeMaker.Literal(fieldIndex), getIdent(BYTE_BUFFER_NAME), getGenericClass(listFieldTypeStr))))));
                        } else if (SerializeObjField.class.getName().equals(annType.toString())) {
                            byteLengthAddJcS.append(generateApply(SerializeObjFieldUtils.GET_OBJ_FIELD_LENGTH_METHOD,
                                    List.of(getThisField(fieldJCVariable))));
                            serializeJcS_ByteBuffer.append(treeMaker.Exec(generateApply(SerializeObjFieldUtils.SERIALIZE_METHOD,
                                    List.of(getIdent(BYTE_BUFFER_NAME), getThisField(fieldJCVariable)))));
                            serializeJcS_ByteBuf.append(treeMaker.Exec(generateApply(SerializeObjFieldUtils.SERIALIZE_METHOD,
                                    List.of(getIdent(BYTE_BUFFER_NAME), getThisField(fieldJCVariable)))));
                            serializeLEJcS_ByteBuf.append(treeMaker.Exec(generateApply(SerializeObjFieldUtils.SERIALIZE_LE_METHOD,
                                    List.of(getIdent(BYTE_BUFFER_NAME), getThisField(fieldJCVariable)))));

                            deSerializeJcS_ByteBuffer.append(treeMaker.Exec(treeMaker.Assign(getThisField(fieldJCVariable), generateApply(SerializeObjFieldUtils.DESERIALIZE_METHOD,
                                    List.of(getIdent(FIELD_BIT_SET_NAME), treeMaker.Literal(fieldIndex), getIdent(BYTE_BUFFER_NAME), getThisField(fieldJCVariable), getGenericClass(fieldType.toString()))))));

                            deSerializeJcS_ByteBuf.append(treeMaker.Exec(treeMaker.Assign(getThisField(fieldJCVariable), generateApply(SerializeObjFieldUtils.DESERIALIZE_METHOD,
                                    List.of(getIdent(FIELD_BIT_SET_NAME), treeMaker.Literal(fieldIndex), getIdent(BYTE_BUFFER_NAME), getThisField(fieldJCVariable), getGenericClass(fieldType.toString()))))));

                            deSerializeLEJcS_ByteBuf.append(treeMaker.Exec(treeMaker.Assign(getThisField(fieldJCVariable), generateApply(SerializeObjFieldUtils.DESERIALIZE_LE_METHOD,
                                    List.of(getIdent(FIELD_BIT_SET_NAME), treeMaker.Literal(fieldIndex), getIdent(BYTE_BUFFER_NAME), getThisField(fieldJCVariable), getGenericClass(fieldType.toString()))))));

                        }

                        break;
                    }
                }
            }
            fieldIndex++;
        }

        releaseFieldBitSet(serializeJcS_ByteBuffer);
        releaseFieldBitSet(serializeJcS_ByteBuf);
        releaseFieldBitSet(serializeLEJcS_ByteBuf);
        releaseFieldBitSet(deSerializeJcS_ByteBuffer);
        releaseFieldBitSet(deSerializeJcS_ByteBuf);
        releaseFieldBitSet(deSerializeLEJcS_ByteBuf);
        byteLengthAddJcS.append(treeMaker.Literal(byteLength));
        serializeMethodJCMethod_ByteBuffer.body.stats = serializeJcS_ByteBuffer.toList();
        deSerializeMethodJCMethod_ByteBuffer.body.stats = deSerializeJcS_ByteBuffer.toList();
        serializeMethodJCMethod_ByteBuf.body.stats = serializeJcS_ByteBuf.toList();
        deSerializeMethodJCMethod_ByteBuf.body.stats = deSerializeJcS_ByteBuf.toList();
        serializeLEMethodJCMethod_ByteBuf.body.stats = serializeLEJcS_ByteBuf.toList();
        deSerializeLEMethodJCMethod_ByteBuf.body.stats = deSerializeLEJcS_ByteBuf.toList();
        getByteLengthMethod.body.stats = List.of(treeMaker.Return(addAll(byteLengthAddJcS)));
    }

    private JCTree.JCFieldAccess getThisField(JCTree.JCVariableDecl fieldJCVariable) {
        return treeMaker.Select(treeMaker.Ident(names.fromString(ProcessUtil.THIS)), fieldJCVariable.name);
    }

    private JCTree.JCIdent getIdent(String name) {
        return treeMaker.Ident(names.fromString(name));
    }

    private JCTree.JCMethodInvocation generateSuperApply(String methodName, String... params) {
        List<JCTree.JCExpression> paramsList;
        if (params != null) {
            JCTree.JCIdent[] parameterList = new JCTree.JCIdent[params.length];
            for (int i = 0; i < params.length; i++) {
                parameterList[i] = getIdent(BYTE_BUFFER_NAME);
            }
            paramsList = List.from(parameterList);
        } else {
            paramsList = List.nil();
        }
        return generateApply(String.format("%s.%s", ProcessUtil.SUPER, methodName), paramsList);
    }


    private JCTree.JCMethodInvocation generateApply(String methodName, List<JCTree.JCExpression> paramsList) {
        return treeMaker.Apply(
                List.nil(),
                ProcessUtil.memberAccess(treeMaker, names, methodName),
                paramsList //传入的参数集合
        );
    }

    private JCTree.JCStatement generateSerializeFieldBitSet() {
        return treeMaker.VarDef(
                treeMaker.Modifiers(0), //访问标志。极其坑爹！！！
                names.fromString(FIELD_BIT_SET_NAME), //名字
                ProcessUtil.memberAccess(treeMaker, names, FIELD_BIT_SET_CLASS), //类型
                treeMaker.Apply(
                        List.nil(),
                        ProcessUtil.memberAccess(treeMaker, names, FieldBitSetUtil.GET_BIT_SET_METHOD_NAME),
                        List.of(treeMaker.Literal(fieldNum)) //传入的参数集合
                ) //初始化语句
        );
    }

    private void initSerializeFieldBitSet(ListBuffer<JCTree.JCStatement> jcS) {
        jcS.append(generateSerializeFieldBitSet());
        int index = 0;
        for (JCTree.JCVariableDecl fieldJCVariable : fieldJCVariables) {
            for (JCTree.JCAnnotation annotation : fieldJCVariable.mods.annotations) {
                Type type = annotation.annotationType.type;
                if (type != null) {
                    if (isSerializeAnnotation(type)) {
                        if (isBasedVar(fieldJCVariable.vartype.type)) {
                            jcS.append(treeMaker.Exec(treeMaker.Apply(
                                    List.nil(),
                                    ProcessUtil.memberAccess(treeMaker, names, FIELD_BIT_SET_NAME + FIELD_BIT_SET_SET_METHOD),
                                    List.of(treeMaker.Literal(index), treeMaker.Literal(true))
                            )));
                        } else {
                            jcS.append(treeMaker.Exec(treeMaker.Apply(
                                    List.nil(),
                                    ProcessUtil.memberAccess(treeMaker, names, FIELD_BIT_SET_NAME + FIELD_BIT_SET_SET_METHOD),
                                    List.of(treeMaker.Literal(index),
                                            treeMaker.Binary(JCTree.Tag.NE, treeMaker.Select(
                                                    treeMaker.Ident(names.fromString(ProcessUtil.THIS)),
                                                    fieldJCVariable.name
                                            ), treeMaker.Literal(TypeTag.BOT, null)))
                            )));
                        }
                        break;
                    }
                }
            }
            index++;
        }
        jcS.append(treeMaker.Exec(treeMaker.Apply(
                List.nil(),
                ProcessUtil.memberAccess(treeMaker, names, FIELD_BIT_SET_NAME + FIELD_BIT_SET_SERIALIZE_METHOD),
                List.of(getIdent(BYTE_BUFFER_NAME), treeMaker.Literal(fieldNum))
        )));

    }
    private void releaseFieldBitSet(ListBuffer<JCTree.JCStatement> jcS) {
        jcS.append(treeMaker.Exec(treeMaker.Apply(
                List.nil(),
                ProcessUtil.memberAccess(treeMaker, names, FieldBitSetUtil.RELEASE_BIT_SET_METHOD_NAME),
                List.of(getIdent(FIELD_BIT_SET_NAME))
        )));

    }

    private void initDeserializeFieldBitSet(ListBuffer<JCTree.JCStatement> jcS) {
        jcS.append(generateSerializeFieldBitSet());
        jcS.append(treeMaker.Exec(treeMaker.Apply(
                List.nil(),
                ProcessUtil.memberAccess(treeMaker, names, FIELD_BIT_SET_NAME + FIELD_BIT_SET_INIT_METHOD),
                List.of(getIdent(BYTE_BUFFER_NAME), treeMaker.Literal(fieldNum))
        )));
    }

    private JCTree.JCExpression addAll(ListBuffer<JCTree.JCExpression> jcS) {
        JCTree.JCExpression jcStatement = null;
        for (JCTree.JCExpression jc : jcS) {
            if (jcStatement == null) {
                jcStatement = jc;
            } else {
                jcStatement = treeMaker.Binary(JCTree.Tag.PLUS, jcStatement, jc);
            }
        }
        if (jcStatement == null) {
            return treeMaker.Literal(0);
        }
        return jcStatement;
    }

    private boolean isSerializeAnnotation(Type type) {
        return SerializeField.class.getName().equals(type.toString())
                || SerializeListField.class.getName().equals(type.toString())
                || SerializeObjField.class.getName().equals(type.toString());
    }

    private boolean isBasedVar(Type type) {
        switch (type.toString()) {
            case "byte":
            case "boolean":
            case "short":
            case "char":
            case "int":
            case "float":
            case "long":
            case "double":
                return true;
            default:
                return false;
        }
    }

    private void after() {
        this.jcClass = null;
        this.fieldJCVariables = null;
    }
}
