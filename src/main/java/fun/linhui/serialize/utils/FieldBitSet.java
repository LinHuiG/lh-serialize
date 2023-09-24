package fun.linhui.serialize.utils;

import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

/**
 * @author linhui
 * date 2023-08-27
 */
public class FieldBitSet {
    private final byte[] values;

    public FieldBitSet(int size) {
        int length = (size + 7) >>> 3;
        this.values = new byte[length];
    }

    public void init(ByteBuffer byteBuffer, int size) {
        int length = (size + 7) >>> 3;
        byteBuffer.get(values, 0, length);
    }

    public void init(ByteBuf byteBuf, int size) {
        int length = (size + 7) >>> 3;
        byteBuf.readBytes(values, 0, length);
    }

    public void set(int index, boolean val) {
        int index0 = index >>> 3;
        if (val) {
            values[index0] |= 1 << (index - index0);
        } else {
            values[index0] &= ~(1 << (index - index0));
        }
    }

    public boolean get(int index) {
        int index0 = index >>> 3;
        return (values[index0] & (1 << (index - index0))) > 0;
    }

    public int getSize() {
        return values.length << 3;
    }

    public void serialize(ByteBuffer byteBuffer, int size) {
        int length = (size + 7) >>> 3;
        byteBuffer.put(values, 0, length);
    }

    public void serialize(ByteBuf byteBuf, int size) {
        int length = (size + 7) >>> 3;
        byteBuf.writeBytes(values, 0, length);
    }
}
