package fun.linhui.serialize.utils;

import fun.linhui.serialize.interfaces.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linhui
 * date 2023-08-27
 */
public class SerializeUtils {
    private static final Logger logger = LoggerFactory.getLogger(SerializeUtils.class);
    public  static String GET_GENERIC_CLASS_NAME_BY_INDEX="fun.linhui.serialize.utils.SerializeUtils.getGenericClassNameByIndex";

    public static int addAll(int... nums) {
        int sum = 0;
        for (int num : nums) {
            sum += num;
        }
        return sum;
    }

    private static final Map<Class, Map<Class, Class>> genericClassNameByIndexMap = new ConcurrentHashMap<>();

    public static <T extends Serializable> Class<T> getGenericClassNameByIndex(Object obj, Class currentLevelClz, int index) {
        return getGenericClassNameByIndex(obj.getClass(), currentLevelClz, index);
    }

    public static <T extends Serializable> Class<T> getGenericClassNameByIndex(Class clz, Class currentLevelClz, int index) {
        Map<Class, Class> tMap = genericClassNameByIndexMap.computeIfAbsent(clz, c -> new ConcurrentHashMap<>());
        if (tMap.containsKey(currentLevelClz)) {
            return tMap.get(currentLevelClz);
        }

        Class<T> resultClz = null;
        if (!clz.getSuperclass().equals(currentLevelClz) && !clz.equals(Object.class)) {
            resultClz = getGenericClassNameByIndex(clz.getSuperclass(), currentLevelClz, index);
        } else {
            Type genericSuperclass = clz.getGenericSuperclass();
            if (genericSuperclass instanceof ParameterizedType) {
                resultClz = (Class<T>) ((ParameterizedType) genericSuperclass).getActualTypeArguments()[index];
            }
        }
        tMap.putIfAbsent(currentLevelClz, resultClz);
        return resultClz;
    }

    public static int getCountByteLength(int paramsCount) {
        return 4 + (paramsCount + 7) / 8;
    }
}
