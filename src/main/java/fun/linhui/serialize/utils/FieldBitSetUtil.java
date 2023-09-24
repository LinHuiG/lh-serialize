package fun.linhui.serialize.utils;

import io.netty.util.concurrent.FastThreadLocal;

import java.util.ArrayDeque;

/**
 * @author linhui
 * date 2023-08-27
 */
public class FieldBitSetUtil {

    private static class Stack<T> {
        private final Object[] element;
        private final int maxSize;
        int size = 0;

        public Stack(int maxSize) {
            this.maxSize = maxSize;
            this.element = new Object[maxSize];
        }

        public void push(T t) {
            if (size == maxSize) return;
            element[size++] = t;
        }

        public T pop() {
            if (size == 0) return null;
            return (T) element[--size];
        }

    }

    private static boolean active = false;
    public static int DEFAULT_SIZE = 64;
    public static int CACHE_SIZE = 1024;
    public static String GET_BIT_SET_METHOD_NAME = "fun.linhui.serialize.utils.FieldBitSetUtil.getBitSet";
    public static String RELEASE_BIT_SET_METHOD_NAME = "fun.linhui.serialize.utils.FieldBitSetUtil.release";
    private static FastThreadLocal<Stack<FieldBitSet>> bitsetCache = new FastThreadLocal<Stack<FieldBitSet>>() {
        @Override
        protected Stack<FieldBitSet> initialValue() {
            return new Stack<>(CACHE_SIZE);
        }
    };

    public static boolean isActive() {
        return active;
    }

    public static void setActive(boolean active) {
        FieldBitSetUtil.active = active;
    }

    public static FieldBitSet getBitSet(int size) {
        if (size > DEFAULT_SIZE) {
            return new FieldBitSet(size);
        }
        FieldBitSet fieldBitSet = bitsetCache.get().pop();
        if (fieldBitSet == null) {
            return new FieldBitSet(DEFAULT_SIZE);
        } else {
            return fieldBitSet;
        }
    }

    public static void release(FieldBitSet fieldBitSet) {
        if (fieldBitSet.getSize() == DEFAULT_SIZE) {
            Stack<FieldBitSet> fieldBitSetStack = bitsetCache.get();
            fieldBitSetStack.push(fieldBitSet);
        }
    }

}
