package fun.linhui.serialize.utils;

import fun.linhui.serialize.interfaces.Serializable;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * @author linhui
 * date 2023-08-27
 */
public class SerializeObjFieldUtils {
    public static String SERIALIZE_METHOD = "fun.linhui.serialize.utils.SerializeObjFieldUtils.serializeFieldObj";
    public static String SERIALIZE_LE_METHOD = "fun.linhui.serialize.utils.SerializeObjFieldUtils.serializeFieldObjLE";
    public static String DESERIALIZE_METHOD = "fun.linhui.serialize.utils.SerializeObjFieldUtils.deserializeFieldObj";
    public static String DESERIALIZE_LE_METHOD = "fun.linhui.serialize.utils.SerializeObjFieldUtils.deserializeFieldObjLE";
    public static String GET_OBJ_FIELD_LENGTH_METHOD = "fun.linhui.serialize.utils.SerializeObjFieldUtils.getObjFiledLength";
    private static final Logger logger = LoggerFactory.getLogger(SerializeObjFieldUtils.class);

    public static int getObjFiledLength(Serializable data) {
        return data == null ? 0 : data.getByteLength();
    }


    public static void serializeFieldObj(ByteBuffer byteBuffer, Serializable data) {
        if (data != null) {
            data.serialize(byteBuffer);
        }
    }

    public static void serializeFieldObj(ByteBuf byteBuffer, Serializable data) {
        if (data != null) {
            data.serialize(byteBuffer);
        }
    }

    public static void serializeFieldObjLE(ByteBuf byteBuffer, Serializable data) {
        if (data != null) {
            data.serializeLE(byteBuffer);
        }
    }

    public static <T extends Serializable> T deserializeFieldObj(FieldBitSet fieldBitSet, int index, ByteBuffer byteBuffer, T data, Class<T> clz) {
        if (!fieldBitSet.get(index)) return null;
        if (data == null || !data.getClass().equals(clz)) {
            try {
                data = clz.newInstance();
                data.deserialize(byteBuffer);
            } catch (InstantiationException | IllegalAccessException | BufferUnderflowException e) {
                logger.error("序列化异常", e);
            }
        } else {
            data.deserialize(byteBuffer);
        }
        return data;
    }

    public static <T extends Serializable> T deserializeFieldObj(FieldBitSet fieldBitSet, int index, ByteBuf byteBuffer, T data, Class<T> clz) {
        if (!fieldBitSet.get(index)) return null;
        if (data == null || !data.getClass().equals(clz)) {
            try {
                data = clz.newInstance();
                data.deserialize(byteBuffer);
            } catch (InstantiationException | IllegalAccessException | BufferUnderflowException e) {
                logger.error("反序列化异常", e);
            }
        } else {
            data.deserialize(byteBuffer);
        }
        return data;
    }


    public static <T extends Serializable> T deserializeFieldObjLE(FieldBitSet fieldBitSet, int index, ByteBuf byteBuffer, T data, Class<T> clz) {
        if (!fieldBitSet.get(index)) return null;
        if (data == null || !data.getClass().equals(clz)) {
            try {
                data = clz.newInstance();
                data.deserializeLE(byteBuffer);
            } catch (InstantiationException | IllegalAccessException | BufferUnderflowException e) {
                logger.error("反序列化异常", e);
            }
        } else {
            data.deserializeLE(byteBuffer);
        }
        return data;
    }
}
