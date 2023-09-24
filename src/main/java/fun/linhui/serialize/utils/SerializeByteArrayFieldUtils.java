package fun.linhui.serialize.utils;

import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.function.Function;

/**
 * @author linhui
 * date 2023-08-27
 */
public class SerializeByteArrayFieldUtils {
    public static String SERIALIZE_METHOD = "fun.linhui.serialize.utils.SerializeByteArrayFieldUtils.serializeByteArray";
    public static String SERIALIZE_LE_METHOD = "fun.linhui.serialize.utils.SerializeByteArrayFieldUtils.serializeByteArrayLE";
    public static String DESERIALIZE_METHOD = "fun.linhui.serialize.utils.SerializeByteArrayFieldUtils.deserializeByteArray";
    public static String DESERIALIZE_LE_METHOD = "fun.linhui.serialize.utils.SerializeByteArrayFieldUtils.deserializeByteArrayLE";
    private static final Logger logger = LoggerFactory.getLogger(SerializeByteArrayFieldUtils.class);
    private static Function<Integer, Object> byteArrayCreate;
    private static Function<Integer, ByteBuffer> byteBufferCreate;
    public static String GET_BYTE_ARRAY_LENGTH_METHOD = "fun.linhui.serialize.utils.SerializeByteArrayFieldUtils.getByteArrayLength";

    public static void setByteBufferCreate(Function<Integer, ByteBuffer> byteBufferCreate) {
        SerializeByteArrayFieldUtils.byteBufferCreate = byteBufferCreate;
    }

    public static void setByteArrayCreate(Function<Integer, Object> byteArrayCreate) {
        SerializeByteArrayFieldUtils.byteArrayCreate = byteArrayCreate;
    }

    private static byte[] byteArrayCreate(int len) {
        if (byteArrayCreate != null) return (byte[]) byteArrayCreate.apply(len);
        return new byte[len];
    }


    public static ByteBuffer getByteBuffer(int size) {
        if (byteBufferCreate != null) byteBufferCreate.apply(size);
        return ByteBuffer.allocate(size);
    }

    //若原长度够则直接用，否则new一个
    public static byte[] deserializeByteArray(FieldBitSet fieldBitSet, int index, ByteBuffer buffer, byte[] data, int length) {
        if (!fieldBitSet.get(index)) return null;
        if (length == 0) {
            length = buffer.getInt();
        }
        if (data == null || data.length < length) {
            data = byteArrayCreate(length);
        }
        buffer.get(data, 0, length);
        return data;
    }

    //若原长度够则直接用，否则new一个
    public static byte[] deserializeByteArray(FieldBitSet fieldBitSet, int index, ByteBuf buffer, byte[] data, int length) {
        if (!fieldBitSet.get(index)) return null;
        if (length == 0) {
            length = buffer.readInt();
        }
        if (data == null || data.length < length) {
            data = byteArrayCreate(length);
        }
        buffer.readBytes(data, 0, length);
        return data;
    }

    //若原长度够则直接用，否则new一个
    public static byte[] deserializeByteArrayLE(FieldBitSet fieldBitSet, int index, ByteBuf buffer, byte[] data, int length) {
        if (!fieldBitSet.get(index)) return null;
        if (length == 0) {
            length = buffer.readIntLE();
        }
        if (data == null || data.length < length) {
            data = byteArrayCreate(length);
        }
        buffer.readBytes(data, 0, length);
        return data;
    }

    public static void serializeByteArray(ByteBuffer buffer, byte[] data, int length) {
        if (data == null) return;
        if (length == 0) {
            length = data.length;
            buffer.putInt(length);
        }
        if (data.length >= length) {
            buffer.put(data, 0, length);
        } else {
            buffer.put(data);
            buffer.position(length - data.length + buffer.position());
        }
    }

    public static void serializeByteArray(ByteBuf buffer, byte[] data, int length) {
        if (data == null) return;
        if (length == 0) {
            length = data.length;
            buffer.writeInt(length);
        }
        if (data.length >= length) {
            buffer.writeBytes(data, 0, length);
        } else {
            buffer.writeBytes(data, 0, data.length);
            buffer.writeZero(length - data.length);
        }
    }

    public static void serializeByteArrayLE(ByteBuf buffer, byte[] data, int length) {
        if (data == null) return;
        if (length == 0) {
            length = data.length;
            buffer.writeIntLE(length);
        }
        if (data.length >= length) {
            buffer.writeBytes(data, 0, length);
        } else {
            buffer.writeBytes(data, 0, data.length);
            buffer.writeZero(length - data.length);
        }
    }

    public static int getByteArrayLength(byte[] data, int length) {
        if (data == null) return 0;
        if (length > 0) return length;
        return data.length + 4;
    }

}
