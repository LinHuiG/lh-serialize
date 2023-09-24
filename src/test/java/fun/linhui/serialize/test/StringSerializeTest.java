package fun.linhui.serialize.test;

import fun.linhui.serialize.utils.FieldBitSet;
import fun.linhui.serialize.utils.SerializeStringFieldUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * @author linhui
 * date 2023-08-27
 */
public class StringSerializeTest {
    static FieldBitSet fieldBitSet = new FieldBitSet(10);

    public static void main(String[] args) {
        fieldBitSet.set(0, true);
        String str = "我生来就是高山而非溪流 ，我欲于群峰之巅俯视平庸的沟壑。 我生来就是人杰而非草芥，我站在伟人之肩藐视卑微的懦夫。";
        if (str.getBytes(StandardCharsets.UTF_8).length != SerializeStringFieldUtils.getStringLength(str)) {
            System.out.println("error");
        }
        int length = SerializeStringFieldUtils.getStringLength(str);
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer(length);

        ByteBuffer byteBuffer = ByteBuffer.allocate(SerializeStringFieldUtils.getStringLength(str));
        int serializeLength = SerializeStringFieldUtils.getStringLength(str);
        SerializeStringFieldUtils.writeUTF8String2ByteBuffer(byteBuf, str, serializeLength);
        SerializeStringFieldUtils.writeUTF8String2ByteBuffer(byteBuffer, str, serializeLength);

        if (serializeLength != byteBuf.writerIndex()) {
            System.out.println("error");
        }
        byteBuffer.position(0);
        String str1 = SerializeStringFieldUtils.deserializeString(fieldBitSet, 0, byteBuf, serializeLength);
        String str2 = SerializeStringFieldUtils.deserializeString(fieldBitSet, 0, byteBuffer, serializeLength);
        System.out.println(str1);
        System.out.println(str2);


        int n = 10000000;
        int cs = 10;
        for (int i = 0; i < cs; i++) {
            str += "1546";
            byteBuf = ByteBufAllocator.DEFAULT.buffer(length);
            long start = System.nanoTime();
            for (int j = 0; j < n; j++) {
                byteBuf.writerIndex(0);
                length = SerializeStringFieldUtils.getStringLength(str);
                SerializeStringFieldUtils.writeUTF8String2ByteBuffer(byteBuf, str, length);
            }
            System.out.println("new---speedTime:" + (System.nanoTime() - start) * 1.0D / n);


            start = System.nanoTime();
            for (int j = 0; j < n; j++) {
                byteBuf.writerIndex(0);
                length = str.getBytes(StandardCharsets.UTF_8).length;
                byteBuf.writeBytes(str.getBytes(StandardCharsets.UTF_8));
            }
            System.out.println("speedTime:" + (System.nanoTime() - start) * 1.0D / n);
        }
    }
}
