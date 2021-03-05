package com.github.aq0706.support.json;

public class JSON {

    public static <T> T parse(String source, Class<T> clazz) {
        return parse(source.getBytes(), clazz);
    }

    public static <T> T parse(byte[] source, Class<T> returnClass) {
        return new JSONParser<T>(source).parse(returnClass);
    }

    public static String toJSONString(Object source) {
        return JSONWriter.toJSONString(source);
    }
}
