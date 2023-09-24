package fun.linhui.serialize.utils;

import com.sun.tools.javac.code.Type;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * @author linhui
 * date 2023-08-27
 */
public class SerializeFieldUtils {

    public static final String SERIALIZE_METHOD = "fun.linhui.serialize.utils.SerializeFieldUtils.write";
    public static final String SERIALIZE_LE_METHOD = "fun.linhui.serialize.utils.SerializeFieldUtils.writeLE";
    private static final String DESERIALIZE_METHOD = "fun.linhui.serialize.utils.SerializeFieldUtils.%s";
    public static String GET_FIELD_LENGTH_METHOD = "fun.linhui.serialize.utils.SerializeFieldUtils.getByteLength";

    private static final Logger logger = LoggerFactory.getLogger(SerializeFieldUtils.class);


    public static String getDeserializeMethodName(Type type) {
        return String.format(DESERIALIZE_METHOD, getDeserializeSelector(type));
    }

    public static String getDeserializeLEMethodName(Type type) {
        return String.format(DESERIALIZE_METHOD, getDeserializeSelectorLE(type));
    }

    public static int getByteLength(byte val) {
        return 1;
    }

    public static int getByteLength(boolean val) {
        return 1;
    }

    public static int getByteLength(short val) {
        return 2;
    }

    public static int getByteLength(int val) {
        return 4;
    }

    public static int getByteLength(float val) {
        return 4;
    }

    public static int getByteLength(long val) {
        return 8;
    }

    public static int getByteLength(double val) {
        return 8;
    }

    public static int getByteLength(Byte val) {
        return val == null ? 0 : 1;
    }

    public static int getByteLength(Boolean val) {
        return val == null ? 0 : 1;
    }

    public static int getByteLength(Short val) {
        return val == null ? 0 : 2;
    }

    public static int getByteLength(Integer val) {
        return val == null ? 0 : 4;
    }

    public static int getByteLength(Float val) {
        return val == null ? 0 : 4;
    }

    public static int getByteLength(Long val) {
        return val == null ? 0 : 8;
    }

    public static int getByteLength(Double val) {
        return val == null ? 0 : 8;
    }


    public static void write(ByteBuffer byteBuffer, byte val) {
        byteBuffer.put(val);
    }

    public static void write(ByteBuffer byteBuffer, boolean val) {
        byteBuffer.put((byte) (val ? 1 : 0));
    }

    public static void write(ByteBuffer byteBuffer, short val) {
        byteBuffer.putShort(val);
    }

    public static void write(ByteBuffer byteBuffer, int val) {
        byteBuffer.putInt(val);
    }

    public static void write(ByteBuffer byteBuffer, float val) {
        byteBuffer.putFloat(val);
    }

    public static void write(ByteBuffer byteBuffer, long val) {
        byteBuffer.putLong(val);
    }

    public static void write(ByteBuffer byteBuffer, double val) {
        byteBuffer.putDouble(val);
    }

    public static void write(ByteBuffer byteBuffer, Byte val) {
        if (val != null) {
            byteBuffer.put(val);
        }
    }

    public static void write(ByteBuffer byteBuffer, Boolean val) {
        if (val != null) {
            byteBuffer.put((byte) (val ? 1 : 0));
        }
    }

    public static void write(ByteBuffer byteBuffer, Short val) {
        if (val != null) {
            byteBuffer.putShort(val);
        }
    }

    public static void write(ByteBuffer byteBuffer, Integer val) {
        if (val != null) {
            byteBuffer.putInt(val);
        }
    }

    public static void write(ByteBuffer byteBuffer, Float val) {
        if (val != null) {
            byteBuffer.putFloat(val);
        }
    }

    public static void write(ByteBuffer byteBuffer, Long val) {
        if (val != null) {
            byteBuffer.putLong(val);
        }
    }

    public static void write(ByteBuffer byteBuffer, Double val) {
        if (val != null) {
            byteBuffer.putDouble(val);
        }
    }


    public static void write(ByteBuf byteBuffer, byte val) {
        byteBuffer.writeByte(val);
    }

    public static void write(ByteBuf byteBuffer, boolean val) {
        byteBuffer.writeBoolean(val);
    }

    public static void write(ByteBuf byteBuffer, short val) {
        byteBuffer.writeShort(val);
    }

    public static void write(ByteBuf byteBuffer, int val) {
        byteBuffer.writeInt(val);
    }

    public static void write(ByteBuf byteBuffer, float val) {
        byteBuffer.writeFloat(val);
    }

    public static void write(ByteBuf byteBuffer, long val) {
        byteBuffer.writeLong(val);
    }

    public static void write(ByteBuf byteBuffer, double val) {
        byteBuffer.writeDouble(val);
    }

    public static void write(ByteBuf byteBuffer, Byte val) {
        if (val != null) {
            byteBuffer.writeByte(val);
        }
    }

    public static void write(ByteBuf byteBuffer, Boolean val) {
        if (val != null) {
            byteBuffer.writeBoolean(val);
        }
    }

    public static void write(ByteBuf byteBuffer, Short val) {
        if (val != null) {
            byteBuffer.writeShort(val);
        }
    }

    public static void write(ByteBuf byteBuffer, Integer val) {
        if (val != null) {
            byteBuffer.writeInt(val);
        }
    }

    public static void write(ByteBuf byteBuffer, Float val) {
        if (val != null) {
            byteBuffer.writeFloat(val);
        }
    }

    public static void write(ByteBuf byteBuffer, Long val) {
        if (val != null) {
            byteBuffer.writeLong(val);
        }
    }

    public static void write(ByteBuf byteBuffer, Double val) {
        if (val != null) {
            byteBuffer.writeDouble(val);
        }
    }

    public static void writeLE(ByteBuf byteBuffer, byte val) {
        byteBuffer.writeByte(val);
    }

    public static void writeLE(ByteBuf byteBuffer, boolean val) {
        byteBuffer.writeBoolean(val);
    }

    public static void writeLE(ByteBuf byteBuffer, short val) {
        byteBuffer.writeShortLE(val);
    }

    public static void writeLE(ByteBuf byteBuffer, int val) {
        byteBuffer.writeIntLE(val);
    }

    public static void writeLE(ByteBuf byteBuffer, float val) {
        byteBuffer.writeFloatLE(val);
    }

    public static void writeLE(ByteBuf byteBuffer, long val) {
        byteBuffer.writeLongLE(val);
    }

    public static void writeLE(ByteBuf byteBuffer, double val) {
        byteBuffer.writeDoubleLE(val);
    }

    public static void writeLE(ByteBuf byteBuffer, Byte val) {
        if (val != null) {
            byteBuffer.writeByte(val);
        }
    }

    public static void writeLE(ByteBuf byteBuffer, Boolean val) {
        if (val != null) {
            byteBuffer.writeBoolean(val);
        }
    }

    public static void writeLE(ByteBuf byteBuffer, Short val) {
        if (val != null) {
            byteBuffer.writeShortLE(val);
        }
    }

    public static void writeLE(ByteBuf byteBuffer, Integer val) {
        if (val != null) {
            byteBuffer.writeIntLE(val);
        }
    }

    public static void writeLE(ByteBuf byteBuffer, Float val) {
        if (val != null) {
            byteBuffer.writeFloatLE(val);
        }
    }

    public static void writeLE(ByteBuf byteBuffer, Long val) {
        if (val != null) {
            byteBuffer.writeLongLE(val);
        }
    }

    public static void writeLE(ByteBuf byteBuffer, Double val) {
        if (val != null) {
            byteBuffer.writeDoubleLE(val);
        }
    }


    public static byte readByte(ByteBuf byteBuffer) {
        return byteBuffer.readByte();
    }

    public static boolean readBoolean(ByteBuf byteBuffer) {
        return byteBuffer.readBoolean();
    }

    public static int readShort(ByteBuf byteBuffer) {
        return byteBuffer.readInt();
    }

    public static int readInt(ByteBuf byteBuffer) {
        return byteBuffer.readInt();
    }

    public static float readFloat(ByteBuf byteBuffer) {
        return byteBuffer.readFloat();
    }

    public static long readLong(ByteBuf byteBuffer) {
        return byteBuffer.readLong();
    }

    public static double readDouble(ByteBuf byteBuffer) {
        return byteBuffer.readDouble();
    }

    public static Byte readByte(FieldBitSet fieldBitSet, int index, ByteBuf byteBuffer) {
        if (fieldBitSet.get(index)) {
            return byteBuffer.readByte();
        } else {
            return null;
        }
    }

    public static Boolean readBoolean(FieldBitSet fieldBitSet, int index, ByteBuf byteBuffer) {
        if (fieldBitSet.get(index)) {
            return byteBuffer.readBoolean();
        } else {
            return null;
        }
    }

    public static Short readShort(FieldBitSet fieldBitSet, int index, ByteBuf byteBuffer) {
        if (fieldBitSet.get(index)) {
            return byteBuffer.readShort();
        } else {
            return null;
        }
    }

    public static Integer readInt(FieldBitSet fieldBitSet, int index, ByteBuf byteBuffer) {
        if (fieldBitSet.get(index)) {
            return byteBuffer.readInt();
        } else {
            return null;
        }
    }

    public static Float readFloat(FieldBitSet fieldBitSet, int index, ByteBuf byteBuffer) {
        if (fieldBitSet.get(index)) {
            return byteBuffer.readFloat();
        } else {
            return null;
        }
    }

    public static Long readLong(FieldBitSet fieldBitSet, int index, ByteBuf byteBuffer) {
        if (fieldBitSet.get(index)) {
            return byteBuffer.readLong();
        } else {
            return null;
        }
    }

    public static Double readDouble(FieldBitSet fieldBitSet, int index, ByteBuf byteBuffer) {
        if (fieldBitSet.get(index)) {
            return byteBuffer.readDouble();
        } else {
            return null;
        }
    }


    public static byte readByte(ByteBuffer byteBuffer) {
        return byteBuffer.get();
    }

    public static boolean readBoolean(ByteBuffer byteBuffer) {
        return byteBuffer.get() != 0;
    }

    public static short readShort(ByteBuffer byteBuffer) {
        return byteBuffer.getShort();
    }

    public static int readInt(ByteBuffer byteBuffer) {
        return byteBuffer.getInt();
    }

    public static long readLong(ByteBuffer byteBuffer) {
        return byteBuffer.getLong();
    }

    public static float readFloat(ByteBuffer byteBuffer) {
        return byteBuffer.getFloat();
    }

    public static double readDouble(ByteBuffer byteBuffer) {
        return byteBuffer.getDouble();
    }

    public static Byte readByte(FieldBitSet fieldBitSet, int index, ByteBuffer byteBuffer) {
        if (fieldBitSet.get(index)) {
            return byteBuffer.get();
        } else {
            return null;
        }
    }

    public static Boolean readBoolean(FieldBitSet fieldBitSet, int index, ByteBuffer byteBuffer) {
        if (fieldBitSet.get(index)) {
            return byteBuffer.get() != 0;
        } else {
            return null;
        }
    }

    public static Short readShort(FieldBitSet fieldBitSet, int index, ByteBuffer byteBuffer) {
        if (fieldBitSet.get(index)) {
            return byteBuffer.getShort();
        } else {
            return null;
        }
    }

    public static Integer readInt(FieldBitSet fieldBitSet, int index, ByteBuffer byteBuffer) {
        if (fieldBitSet.get(index)) {
            return byteBuffer.getInt();
        } else {
            return null;
        }
    }

    public static Long readLong(FieldBitSet fieldBitSet, int index, ByteBuffer byteBuffer) {
        if (fieldBitSet.get(index)) {
            return byteBuffer.getLong();
        } else {
            return null;
        }
    }

    public static Float readFloat(FieldBitSet fieldBitSet, int index, ByteBuffer byteBuffer) {
        if (fieldBitSet.get(index)) {
            return byteBuffer.getFloat();
        } else {
            return null;
        }
    }

    public static Double readDouble(FieldBitSet fieldBitSet, int index, ByteBuffer byteBuffer) {
        if (fieldBitSet.get(index)) {
            return byteBuffer.getDouble();
        } else {
            return null;
        }
    }

    public static byte readByteLE(ByteBuf byteBuffer) {
        return byteBuffer.readByte();
    }

    public static boolean readBooleanLE(ByteBuf byteBuffer) {
        return byteBuffer.readBoolean();
    }

    public static short readShortLE(ByteBuf byteBuffer) {
        return byteBuffer.readShortLE();
    }

    public static int readIntLE(ByteBuf byteBuffer) {
        return byteBuffer.readIntLE();
    }

    public static long readLongLE(ByteBuf byteBuffer) {
        return byteBuffer.readLongLE();
    }

    public static float readFloatLE(ByteBuf byteBuffer) {
        return byteBuffer.readFloatLE();
    }

    public static double readDoubleLE(ByteBuf byteBuffer) {
        return byteBuffer.readDoubleLE();
    }

    public static Byte readByteLE(FieldBitSet fieldBitSet, int index, ByteBuf byteBuffer) {
        if (fieldBitSet.get(index)) {
            return byteBuffer.readByte();
        } else {
            return null;
        }
    }

    public static Boolean readBooleanLE(FieldBitSet fieldBitSet, int index, ByteBuf byteBuffer) {
        if (fieldBitSet.get(index)) {
            return byteBuffer.readBoolean();
        } else {
            return null;
        }
    }

    public static Short readShortLE(FieldBitSet fieldBitSet, int index, ByteBuf byteBuffer) {
        if (fieldBitSet.get(index)) {
            return byteBuffer.readShortLE();
        } else {
            return null;
        }
    }

    public static Integer readIntLE(FieldBitSet fieldBitSet, int index, ByteBuf byteBuffer) {
        if (fieldBitSet.get(index)) {
            return byteBuffer.readIntLE();
        } else {
            return null;
        }
    }

    public static Long readLongLE(FieldBitSet fieldBitSet, int index, ByteBuf byteBuffer) {
        if (fieldBitSet.get(index)) {
            return byteBuffer.readLongLE();
        } else {
            return null;
        }
    }

    public static Float readFloatLE(FieldBitSet fieldBitSet, int index, ByteBuf byteBuffer) {
        if (fieldBitSet.get(index)) {
            return byteBuffer.readFloatLE();
        } else {
            return null;
        }
    }

    public static Double readDoubleLE(FieldBitSet fieldBitSet, int index, ByteBuf byteBuffer) {
        if (fieldBitSet.get(index)) {
            return byteBuffer.readDoubleLE();
        } else {
            return null;
        }
    }

    private static String getDeserializeSelector(Type type) {
        switch (type.toString()) {
            case "java.lang.Boolean":
            case "boolean":
                return "readBoolean";
            case "java.lang.Byte":
            case "byte":
                return "readByte";
            case "java.lang.Integer":
            case "int":
                return "readInt";
            case "java.lang.Long":
            case "long":
                return "readLong";
            case "java.lang.Short":
            case "short":
                return "readShort";
            case "java.lang.Character":
            case "char":
                return "readChar";
            case "java.lang.Double":
            case "double":
                return "readDouble";
            case "java.lang.Float":
            case "float":
                return "readFloat";
            default:
                return "readByte";
        }
    }

    private static String getDeserializeSelectorLE(Type type) {
        switch (type.toString()) {
            case "java.lang.Boolean":
            case "boolean":
                return "readBoolean";
            case "java.lang.Byte":
            case "byte":
                return "readByte";
            case "java.lang.Integer":
            case "int":
                return "readIntLE";
            case "java.lang.Long":
            case "long":
                return "readLongLE";
            case "java.lang.Short":
            case "short":
                return "readShortLE";
//            case "java.lang.Character":
//            case "char":
//                return "readCharLE";
            case "java.lang.Double":
            case "double":
                return "readDoubleLE";
            case "java.lang.Float":
            case "float":
                return "readFloatLE";
            default:
                return "readByte";
        }
    }

    public static int getByteLength(Type type) {
        switch (type.toString()) {
            case "java.lang.Boolean":
            case "boolean":
                return 1;
            case "java.lang.Byte":
            case "byte":
                return 1;
            case "java.lang.Integer":
            case "int":
                return 4;
            case "java.lang.Long":
            case "long":
                return 8;
            case "java.lang.Short":
            case "short":
                return 2;
//            case "java.lang.Character":
//            case "char":
//                return "readCharLE";
            case "java.lang.Double":
            case "double":
                return 8;
            case "java.lang.Float":
            case "float":
                return 4;
            default:
                return 0;
        }
    }
}
