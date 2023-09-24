package fun.linhui.serialize.utils;

import fun.linhui.serialize.interfaces.Serializable;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author linhui
 * date 2023-08-27
 */
public class SerializeFieldListUtil {
    public static final String GET_LIST_LENGTH_METHOD = "fun.linhui.serialize.utils.SerializeFieldListUtil.getListFiledLength";
    public static String SERIALIZE_METHOD = "fun.linhui.serialize.utils.SerializeFieldListUtil.serializeFieldList";
    public static String SERIALIZE_LE_METHOD = "fun.linhui.serialize.utils.SerializeFieldListUtil.serializeFieldListLE";
    public static String DESERIALIZE_METHOD = "fun.linhui.serialize.utils.SerializeFieldListUtil.deSerializeFieldList";
    public static String DESERIALIZE_LE_METHOD = "fun.linhui.serialize.utils.SerializeFieldListUtil.deSerializeFieldListLE";

    public static <T> void serializeFieldList(ByteBuffer byteBuffer, List<T> dataList, Class<T> clz) {
        if (dataList == null) {
            return;
        }
        byteBuffer.putInt(dataList.size());
        if (Serializable.class.isAssignableFrom(clz)) {
            for (T t : dataList) {
                SerializeObjFieldUtils.serializeFieldObj(byteBuffer, (Serializable) t);
            }
            return;
        }
        if (Byte.class.equals(clz)) {
            for (T t : dataList) {
                byteBuffer.put((Byte) t);
            }
        } else if (Integer.class.equals(clz)) {
            for (T t : dataList) {
                byteBuffer.putInt((Integer) t);
            }
        } else if (Long.class.equals(clz)) {
            for (T t : dataList) {
                byteBuffer.putLong((Long) t);
            }
        } else if (Short.class.equals(clz)) {
            for (T t : dataList) {
                byteBuffer.putShort((Short) t);
            }
        } else if (Character.class.equals(clz)) {
            for (T t : dataList) {
                byteBuffer.putChar((Character) t);
            }
        } else if (Double.class.equals(clz)) {
            for (T t : dataList) {
                byteBuffer.putDouble((Double) t);
            }
        } else if (Float.class.equals(clz)) {
            for (T t : dataList) {
                byteBuffer.putFloat((Float) t);
            }
        }
    }

    public static <T> List<T> deSerializeFieldList(FieldBitSet bitSet, int index, ByteBuffer byteBuffer, Class<T> clz) {
        if (!bitSet.get(index)) return null;
        int size = byteBuffer.getInt();
        List<T> dataList = new ArrayList<>(size);
        if (Serializable.class.isAssignableFrom(clz)) {
            for (int i = 0; i < size; i++) {
                T data = (T) SerializeObjFieldUtils.deserializeFieldObj(bitSet, index,byteBuffer, null, (Class<Serializable>) clz);
                dataList.add(data);
            }
            return dataList;
        }
        if (Byte.class.equals(clz)) {
            for (int i = 0; i < size; i++) {
                Byte t = byteBuffer.get();
                dataList.add((T) t);
            }
            return dataList;
        } else if (Integer.class.equals(clz)) {
            for (int i = 0; i < size; i++) {
                Integer t = byteBuffer.getInt();
                dataList.add((T) t);
            }
            return dataList;
        } else if (Long.class.equals(clz)) {
            for (int i = 0; i < size; i++) {
                Long t = byteBuffer.getLong();
                dataList.add((T) t);
            }
            return dataList;
        } else if (Short.class.equals(clz)) {
            for (int i = 0; i < size; i++) {
                Short t = byteBuffer.getShort();
                dataList.add((T) t);
            }
            return dataList;
        } else if (Character.class.equals(clz)) {
            for (int i = 0; i < size; i++) {
                Character t = byteBuffer.getChar();
                dataList.add((T) t);
            }
            return dataList;
        } else if (Double.class.equals(clz)) {
            for (int i = 0; i < size; i++) {
                Double t = byteBuffer.getDouble();
                dataList.add((T) t);
            }
            return dataList;
        } else if (Float.class.equals(clz)) {
            for (int i = 0; i < size; i++) {
                Float t = byteBuffer.getFloat();
                dataList.add((T) t);
            }
            return dataList;
        }
        return dataList;
    }

    public static <T> void serializeFieldList(ByteBuf byteBuffer, List<T> dataList, Class<T> clz) {
        if (dataList == null) {
            return;
        }
        byteBuffer.writeInt(dataList.size());
        if (Serializable.class.isAssignableFrom(clz)) {
            for (T t : dataList) {
                SerializeObjFieldUtils.serializeFieldObj(byteBuffer, (Serializable) t);
            }
            return;
        }
        if (Byte.class.equals(clz)) {
            for (T t : dataList) {
                byteBuffer.writeByte((Byte) t);
            }
        } else if (Integer.class.equals(clz)) {
            for (T t : dataList) {
                byteBuffer.writeInt((Integer) t);
            }
        } else if (Long.class.equals(clz)) {
            for (T t : dataList) {
                byteBuffer.writeLong((Long) t);
            }
        } else if (Short.class.equals(clz)) {
            for (T t : dataList) {
                byteBuffer.writeShort((Short) t);
            }
        } else if (Character.class.equals(clz)) {
            for (T t : dataList) {
                byteBuffer.writeChar((Character) t);
            }
        } else if (Double.class.equals(clz)) {
            for (T t : dataList) {
                byteBuffer.writeDouble((Double) t);
            }
        } else if (Float.class.equals(clz)) {
            for (T t : dataList) {
                byteBuffer.writeFloat((Float) t);
            }
        }
    }

    public static <T> List<T> deSerializeFieldList(FieldBitSet bitSet, int index, ByteBuf byteBuffer, Class<T> clz) {
        if (!bitSet.get(index)) return null;
        int size = byteBuffer.readInt();
        List<T> dataList = new ArrayList<>(size);
        if (Serializable.class.isAssignableFrom(clz)) {
            for (int i = 0; i < size; i++) {
                T data = (T) SerializeObjFieldUtils.deserializeFieldObj(bitSet, index,byteBuffer, null, (Class<Serializable>) clz);
                dataList.add(data);
            }
            return dataList;
        }
        if (Byte.class.equals(clz)) {
            for (int i = 0; i < size; i++) {
                Byte t = byteBuffer.readByte();
                dataList.add((T) t);
            }
            return dataList;
        } else if (Integer.class.equals(clz)) {
            for (int i = 0; i < size; i++) {
                Integer t = byteBuffer.readInt();
                dataList.add((T) t);
            }
            return dataList;
        } else if (Long.class.equals(clz)) {
            for (int i = 0; i < size; i++) {
                Long t = byteBuffer.readLong();
                dataList.add((T) t);
            }
            return dataList;
        } else if (Short.class.equals(clz)) {
            for (int i = 0; i < size; i++) {
                Short t = byteBuffer.readShort();
                dataList.add((T) t);
            }
            return dataList;
        } else if (Character.class.equals(clz)) {
            for (int i = 0; i < size; i++) {
                Character t = byteBuffer.readChar();
                dataList.add((T) t);
            }
            return dataList;
        } else if (Double.class.equals(clz)) {
            for (int i = 0; i < size; i++) {
                Double t = byteBuffer.readDouble();
                dataList.add((T) t);
            }
            return dataList;
        } else if (Float.class.equals(clz)) {
            for (int i = 0; i < size; i++) {
                Float t = byteBuffer.readFloat();
                dataList.add((T) t);
            }
            return dataList;
        }
        return dataList;
    }

    public static <T> void serializeFieldListLE(ByteBuf byteBuffer, List<T> dataList, Class<T> clz) {
        if (dataList == null) {
            return;
        }
        byteBuffer.writeIntLE(dataList.size());
        if (Serializable.class.isAssignableFrom(clz)) {
            for (T t : dataList) {
                SerializeObjFieldUtils.serializeFieldObjLE(byteBuffer, (Serializable) t);
            }
            return;
        }
        if (Byte.class.equals(clz)) {
            for (T t : dataList) {
                byteBuffer.writeByte((Byte) t);
            }
        } else if (Integer.class.equals(clz)) {
            for (T t : dataList) {
                byteBuffer.writeIntLE((Integer) t);
            }
        } else if (Long.class.equals(clz)) {
            for (T t : dataList) {
                byteBuffer.writeLongLE((Long) t);
            }
        } else if (Short.class.equals(clz)) {
            for (T t : dataList) {
                byteBuffer.writeShortLE((Short) t);
            }
        } else if (Character.class.equals(clz)) {
            for (T t : dataList) {
                byteBuffer.writeShortLE((Character) t);
            }
        } else if (Double.class.equals(clz)) {
            for (T t : dataList) {
                byteBuffer.writeDoubleLE((Double) t);
            }
        } else if (Float.class.equals(clz)) {
            for (T t : dataList) {
                byteBuffer.writeFloatLE((Float) t);
            }
        }
    }

    public static <T> List<T> deSerializeFieldListLE(FieldBitSet bitSet, int index, ByteBuf byteBuffer, Class<T> clz) {
        if (!bitSet.get(index)) return null;
        int size = byteBuffer.readIntLE();
        List<T> dataList = new ArrayList<>(size);
        if (Serializable.class.isAssignableFrom(clz)) {
            for (int i = 0; i < size; i++) {
                T data = (T) SerializeObjFieldUtils.deserializeFieldObjLE(bitSet, index, byteBuffer, null, (Class<Serializable>) clz);
                dataList.add(data);
            }
            return dataList;
        }
        if (Byte.class.equals(clz)) {
            for (int i = 0; i < size; i++) {
                Byte t = byteBuffer.readByte();
                dataList.add((T) t);
            }
            return dataList;
        } else if (Integer.class.equals(clz)) {
            for (int i = 0; i < size; i++) {
                Integer t = byteBuffer.readIntLE();
                dataList.add((T) t);
            }
            return dataList;
        } else if (Long.class.equals(clz)) {
            for (int i = 0; i < size; i++) {
                Long t = byteBuffer.readLongLE();
                dataList.add((T) t);
            }
            return dataList;
        } else if (Short.class.equals(clz)) {
            for (int i = 0; i < size; i++) {
                Short t = byteBuffer.readShortLE();
                dataList.add((T) t);
            }
            return dataList;
        } else if (Character.class.equals(clz)) {
            for (int i = 0; i < size; i++) {
                Character t = (char) byteBuffer.readShortLE();
                dataList.add((T) t);
            }
            return dataList;
        } else if (Double.class.equals(clz)) {
            for (int i = 0; i < size; i++) {
                Double t = byteBuffer.readDoubleLE();
                dataList.add((T) t);
            }
            return dataList;
        } else if (Float.class.equals(clz)) {
            for (int i = 0; i < size; i++) {
                Float t = byteBuffer.readFloatLE();
                dataList.add((T) t);
            }
            return dataList;
        }
        return dataList;
    }

    public static <T> int getListFiledLength(List<T> list, Class<T> clz) {
        if (list == null) return 0;
        if (list.isEmpty()) return 4;
        if (Serializable.class.isAssignableFrom(clz)) {
            int length = 4;
            for (T t : list) {
                length += ((Serializable) t).getByteLength();
            }
            return length;
        }
        if (Byte.class.equals(clz)) {
            return 4 + list.size();
        } else if (Integer.class.equals(clz)) {
            return 4 + 4 * list.size();
        } else if (Long.class.equals(clz)) {
            return 4 + 8 * list.size();
        } else if (Short.class.equals(clz)) {
            return 4 + 2 * list.size();
        } else if (Character.class.equals(clz)) {
            return 4 + 2 * list.size();
        } else if (Double.class.equals(clz)) {
            return 4 + 8 * list.size();
        } else if (Float.class.equals(clz)) {
            return 4 + 4 * list.size();
        }
        return 4;
    }
}
