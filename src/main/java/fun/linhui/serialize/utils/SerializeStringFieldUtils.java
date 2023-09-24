package fun.linhui.serialize.utils;

import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author linhui
 * date 2023-08-27
 */
public class SerializeStringFieldUtils {
    private static final Logger logger = LoggerFactory.getLogger(SerializeStringFieldUtils.class);

    private static Charset charset = StandardCharsets.UTF_8;
    public static String GET_STRING_LENGTH_METHOD = "fun.linhui.serialize.utils.SerializeStringFieldUtils.getStringFiledLength";
    public static String SERIALIZE_METHOD = "fun.linhui.serialize.utils.SerializeStringFieldUtils.serializeString";
    public static String SERIALIZE_LE_METHOD = "fun.linhui.serialize.utils.SerializeStringFieldUtils.serializeStringLE";
    public static String DESERIALIZE_METHOD = "fun.linhui.serialize.utils.SerializeStringFieldUtils.deserializeString";
    public static String DESERIALIZE_LE_METHOD = "fun.linhui.serialize.utils.SerializeStringFieldUtils.deserializeStringLE";

    public static void setCharSet(Charset charset) {
        SerializeStringFieldUtils.charset = charset;
    }

    public static String deserializeString(FieldBitSet fieldBitSet, int index, ByteBuffer buffer, int len) {
        if (!fieldBitSet.get(index)) return null;
        if (len == 0) {
            len = buffer.getInt();
        }
        byte[] bytes = buffer.array();
        int position = buffer.position();
        int length = getLengthNot0(bytes, position, len);
        buffer.position(position + len);
        return new String(bytes, position, length, charset);
    }

    public static String deserializeString(FieldBitSet fieldBitSet, int index, ByteBuf buffer, int len) {
        if (!fieldBitSet.get(index)) return null;
        if (len == 0) {
            len = buffer.readInt();
        }
        int position = buffer.readerIndex();
        len = Math.min(len, buffer.readableBytes());
        buffer.skipBytes(len);
        int length = getLengthNot0(buffer, position, len);
        return buffer.toString(position, length, charset);
    }

    public static String deserializeStringLE(FieldBitSet fieldBitSet, int index, ByteBuf buffer, int len) {
        if (!fieldBitSet.get(index)) return null;
        if (len == 0) {
            len = buffer.readIntLE();
        }
        int position = buffer.readerIndex();
        len = Math.min(len, buffer.readableBytes());
        buffer.skipBytes(len);
        int length = getLengthNot0(buffer, position, len);
        return buffer.toString(position, length, charset);
    }

    private static int getLengthNot0(byte[] buffer, int position, int len) {
        int left = 0;
        int right = len - 1;
        while (left <= right) {
            int mid = (left + right) / 2;
            if (buffer[position + mid] == 0x00) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }
        return left;
    }

    private static int getLengthNot0(ByteBuf buffer, int position, int len) {
        int left = 0;
        int right = len - 1;
        while (left <= right) {
            int mid = (left + right) / 2;
            if (buffer.getByte(position + mid) == 0x00) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }
        return left;
    }

    public static void serializeString(ByteBuffer buffer, String str, int length) {
        if (str == null) return;
        if (length == 0) {
            length = getStringLength(str);
            buffer.putInt(length);
            writeUTF8String2ByteBuffer(buffer, str, length);
            return;
        }
        writeUTF8String2ByteBuffer(buffer, str, length);
    }

    public static void serializeString(ByteBuf buffer, String str, int length) {
        if (str == null) return;
        if (length == 1) {
            byte c = 0;
            if (!str.isEmpty()) {
                c = (byte) str.charAt(0);
            }
            buffer.writeByte(c);
            return;
        }
        if (length == 0) {
            length = getStringLength(str);
            buffer.writeInt(length);
            writeUTF8String2ByteBuffer(buffer, str, length);
            return;
        }
        writeUTF8String2ByteBuffer(buffer, str, length);
    }

    public static void serializeStringLE(ByteBuf buffer, String str, int length) {
        if (str == null) return;
        if (length == 1) {
            byte c = 0;
            if (!str.isEmpty()) {
                c = (byte) str.charAt(0);
            }
            buffer.writeByte(c);
            return;
        }
        if (length == 0) {
            length = getStringFiledLength(str, 0);
            buffer.writeIntLE(length);
            writeUTF8String2ByteBuffer(buffer, str, length);
            return;
        }
        writeUTF8String2ByteBuffer(buffer, str, length);
    }

    public static int getStringFiledLength(String str, int length) {
        if (str == null) return 0;
        if (length > 0) return length;
        return getStringLength(str) + 4;
    }


    public static int getStringLength(String str) {
        if (charset == StandardCharsets.UTF_8) {
            int length = 0;
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                if (c < 0x0080) length++;
                else if (c < 0x0800) length += 2;
                else if (Character.isHighSurrogate(c) && ++i < str.length() && Character.isLowSurrogate(str.charAt(i)))
                    length += 4;
                else length += 3;
            }
            return length;
        }
        return str.getBytes(charset).length;
    }

    public static void writeUTF8String2ByteBuffer(ByteBuf buffer, String str, int byteLength) {
        int i = 0;
        int bufferPosLimit = buffer.writerIndex() + byteLength;
        for (char c = 0; ; c = 0) {
            while (i != str.length() && buffer.writerIndex() != bufferPosLimit && (c = str.charAt(i++)) < 0x0080) {
                buffer.writeByte(c);
            }
            if (i == str.length() && c < 0x0080) {
                break;
            }
            if (buffer.writerIndex() == bufferPosLimit) return;
            if (c < 0x0800) {
                buffer.writeByte((byte) (0xC0 | ((c >> 6) & 0x1F)));
                if (buffer.writerIndex() == bufferPosLimit) return;
                buffer.writeByte((byte) (0x80 | ((c) & 0x3F)));
                if (buffer.writerIndex() == bufferPosLimit) return;
            } else if (Character.isHighSurrogate(c) && i < str.length() && Character.isLowSurrogate(str.charAt(i))) {
                // We have a surrogate pair, so use the 4-byte encoding.
                int codePoint = Character.toCodePoint(c, str.charAt(i));
                buffer.writeByte((byte) (0xF0 | ((codePoint >> 18) & 0x07)));
                if (buffer.writerIndex() == bufferPosLimit) return;
                buffer.writeByte((byte) (0x80 | ((codePoint >> 12) & 0x3F)));
                if (buffer.writerIndex() == bufferPosLimit) return;
                buffer.writeByte((byte) (0x80 | ((codePoint >> 6) & 0x3F)));
                if (buffer.writerIndex() == bufferPosLimit) return;
                buffer.writeByte((byte) (0x80 | ((codePoint) & 0x3F)));
                if (buffer.writerIndex() == bufferPosLimit) return;
                i++;
            } else {
                buffer.writeByte((byte) (0xE0 | ((c >> 12) & 0x0F)));
                if (buffer.writerIndex() == bufferPosLimit) return;
                buffer.writeByte((byte) (0x80 | ((c >> 6) & 0x3F)));
                if (buffer.writerIndex() == bufferPosLimit) return;
                buffer.writeByte((byte) (0x80 | ((c) & 0x3F)));
                if (buffer.writerIndex() == bufferPosLimit) return;
            }
        }
        if (buffer.writerIndex() < bufferPosLimit) {
            buffer.writeZero(bufferPosLimit - buffer.writerIndex());
        }
    }

    public static void writeUTF8String2ByteBuffer(ByteBuffer buffer, String str, int byteLength) {
        int i = 0;
        int bufferPosLimit = buffer.position() + byteLength;
        for (char c = 0; ; c = 0) {
            while (i != str.length() && buffer.position() != bufferPosLimit && (c = str.charAt(i++)) < 0x0080) {
                buffer.put((byte) c);
            }
            if (i == str.length() && c < 0x0080) {
                break;
            }
            if (buffer.position() == bufferPosLimit) return;
            if (c < 0x0800) {
                buffer.put((byte) (0xC0 | ((c >> 6) & 0x1F)));
                if (buffer.position() == bufferPosLimit) return;
                buffer.put((byte) (0x80 | ((c) & 0x3F)));
                if (buffer.position() == bufferPosLimit) return;
            } else if (Character.isHighSurrogate(c) && i < str.length() && Character.isLowSurrogate(str.charAt(i))) {
                // We have a surrogate pair, so use the 4-byte encoding.
                int codePoint = Character.toCodePoint(c, str.charAt(i));
                buffer.put((byte) (0xF0 | ((codePoint >> 18) & 0x07)));
                if (buffer.position() == bufferPosLimit) return;
                buffer.put((byte) (0x80 | ((codePoint >> 12) & 0x3F)));
                if (buffer.position() == bufferPosLimit) return;
                buffer.put((byte) (0x80 | ((codePoint >> 6) & 0x3F)));
                if (buffer.position() == bufferPosLimit) return;
                buffer.put((byte) (0x80 | ((codePoint) & 0x3F)));
                if (buffer.position() == bufferPosLimit) return;
                i++;
            } else {
                buffer.put((byte) (0xE0 | ((c >> 12) & 0x0F)));
                if (buffer.position() == bufferPosLimit) return;
                buffer.put((byte) (0x80 | ((c >> 6) & 0x3F)));
                if (buffer.position() == bufferPosLimit) return;
                buffer.put((byte) (0x80 | ((c) & 0x3F)));
                if (buffer.position() == bufferPosLimit) return;
            }
        }
        if (buffer.position() < bufferPosLimit) {
            buffer.position(bufferPosLimit);
        }
    }
}
