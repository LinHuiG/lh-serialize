package fun.linhui.serialize.interfaces;

import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

/**
 * @author linhui
 * date 2023-08-27
 */
public interface Serializable {
    void deserialize(ByteBuffer buffer);

    void serialize(ByteBuffer buffer);

    void deserialize(ByteBuf buffer);

    void serialize(ByteBuf buffer);

    void deserializeLE(ByteBuf buffer);

    void serializeLE(ByteBuf buffer);

    int getByteLength();
}
