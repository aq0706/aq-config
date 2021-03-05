package com.github.aq0706.support.json;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class JSONParser<T> {

    private int status = 0;

    private final byte[] buffer;
    private int currentPos;

    private static final int STATUS_LOOKING = 0;
    private static final int STATUS_PAIR_KEY = 1;
    private static final int STATUS_PAIR_VALUE = 2;
    private static final int STATUS_ARRAY = 3;
    private static final int STATUS_END = 4;

    public JSONParser(byte[] buffer) {
        this.buffer = buffer;
    }

    @SuppressWarnings("unchecked")
    public T parse(Class<T> returnClass) {
        currentPos = 0;
        Stack<Object> valueStack = new Stack<>();
        Stack<Integer> statusStack = new Stack<>();

        for (JSONToken token = findNextToken(); ; token = findNextToken()) {
            switch (status) {
                case STATUS_LOOKING: {
                    if (token.type == JSONToken.TYPE_LEFT_BRACE) {
                        statusStack.push(status);
                        status = STATUS_PAIR_KEY;

                        valueStack.push(new HashMap<String, Object>());
                    } else if (token.type == JSONToken.TYPE_LEFT_SQUARE) {
                        statusStack.push(status);
                        status = STATUS_ARRAY;

                        valueStack.push(new ArrayList<Object>());
                    } else if (token.type == JSONToken.TYPE_EOF) {
                        status = STATUS_END;
                    } else {
                        throwError(token);
                    }
                    break;
                }
                case STATUS_PAIR_KEY: {
                    if (token.type == JSONToken.TYPE_STRING) {
                        valueStack.push(token.value);
                    } else if (token.type == JSONToken.TYPE_COLON) {
                        status = STATUS_PAIR_VALUE;
                    } else {
                        throwError(token);
                    }
                    break;
                }
                case STATUS_PAIR_VALUE: {
                    if (token.type == JSONToken.TYPE_NUMBER || token.type == JSONToken.TYPE_STRING || token.type == JSONToken.TYPE_BOOL) {
                        String key = (String)valueStack.pop();
                        Map<String, Object> map = (Map<String, Object>)valueStack.peek();
                        map.put(key, token);
                    } else if (token.type == JSONToken.TYPE_LEFT_BRACE) {
                        statusStack.push(status);
                        status = STATUS_PAIR_KEY;

                        String key = (String)valueStack.pop();
                        Map<String, Object> map = (Map<String, Object>)valueStack.peek();
                        Map<String, Object> value = new HashMap<>();
                        map.put(key, value);
                        valueStack.push(value);
                    } else if (token.type == JSONToken.TYPE_LEFT_SQUARE) {
                        statusStack.push(status);
                        status = STATUS_ARRAY;

                        String key = (String)valueStack.pop();
                        Map<String, Object> map = (Map<String, Object>)valueStack.peek();
                        List<Object> newList = new ArrayList<>();
                        map.put(key, newList);
                        valueStack.push(newList);
                    } else if (token.type == JSONToken.TYPE_COMMA) {
                        status = STATUS_PAIR_KEY;
                    } else if (token.type == JSONToken.TYPE_RIGHT_BRACE) {
                        if (valueStack.size() > 1) {
                            valueStack.pop();
                        }
                        status = statusStack.pop();
                    }
                    break;
                }
                case STATUS_ARRAY: {
                    if (token.type == JSONToken.TYPE_NUMBER || token.type == JSONToken.TYPE_STRING || token.type == JSONToken.TYPE_BOOL) {
                        List<Object> list = (List<Object>)valueStack.peek();
                        list.add(token);
                    } else if (token.type == JSONToken.TYPE_LEFT_BRACE) {
                        statusStack.push(status);
                        status = STATUS_PAIR_KEY;

                        List<Object> list = (List<Object>)valueStack.peek();
                        Map<String, Object> newMap = new HashMap<>();
                        list.add(newMap);
                        valueStack.push(newMap);
                    } else if (token.type == JSONToken.TYPE_LEFT_SQUARE) {
                        statusStack.push(status);
                        status = STATUS_ARRAY;

                        List<Object> list = (List<Object>)valueStack.peek();
                        List<Object> newList = new ArrayList<>();
                        list.add(newList);
                        valueStack.push(newList);
                    } else if (token.type == JSONToken.TYPE_RIGHT_SQUARE) {
                        if (valueStack.size() > 1) {
                            valueStack.pop();
                        }
                        status = statusStack.pop();
                    } else if (token.type == JSONToken.TYPE_COMMA) {
                        continue;
                    } else {
                        throwError(token);
                    }
                    break;
                }
                case STATUS_END: {
                    try {
                        T result = returnClass.newInstance();
                        Object value = valueStack.pop();
                        setObjectValue(result, value);
                        return result;
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new IllegalStateException("Build result failed.");
                    }
                }

                default: {
                    break;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void setObjectValue(Object result, Object value) {
        try {
            if (value instanceof Map) {
                for (Object entry : ((Map) value).entrySet()) {
                    Object k = ((Map.Entry)entry).getKey();
                    Object v = ((Map.Entry)entry).getValue();

                    Field field = result.getClass().getDeclaredField((String) k);
                    if (v instanceof JSONToken) {
                        if (((JSONToken) v).type == JSONToken.TYPE_STRING) {
                            field.set(result, ((JSONToken) v).value);
                        } else if (((JSONToken) v).type == JSONToken.TYPE_BOOL) {
                            field.setBoolean(result, (Boolean) ((JSONToken) v).value);
                        } else if (((JSONToken) v).type == JSONToken.TYPE_NUMBER) {
                            String fieldTypeName = field.getGenericType().getTypeName();
                            if (fieldTypeName.equals("byte") || fieldTypeName.equals(Byte.class.getName())) {
                                field.setByte(result, Byte.parseByte(((JSONToken) v).value.toString()));
                            } else if (fieldTypeName.equals("short") || fieldTypeName.equals(Short.class.getName())) {
                                field.setShort(result, Short.parseShort(((JSONToken) v).value.toString()));
                            } else if (fieldTypeName.equals("int") || fieldTypeName.equals(Integer.class.getName())) {
                                field.setInt(result, Integer.parseInt(((JSONToken) v).value.toString()));
                            } else if (fieldTypeName.equals("long") || fieldTypeName.equals(Long.class.getName())) {
                                field.setLong(result, Long.parseLong(((JSONToken) v).value.toString()));
                            } else if (fieldTypeName.equals("boolean") || fieldTypeName.equals(Boolean.class.getName())) {
                                field.setBoolean(result, Boolean.parseBoolean(((JSONToken) v).value.toString()));
                            }
                        }
                    } else if (v instanceof Map) {
                        String fieldClassName = field.getGenericType().getTypeName();
                        Object filedInstance = Class.forName(fieldClassName).newInstance();
                        field.set(result, filedInstance);
                        setObjectValue(filedInstance, v);
                    } else if (v instanceof List) {
                        Class<?> fieldType = field.getType();
                        Collection<Object> newCollection;
                        if (fieldType == List.class) {
                            newCollection = new ArrayList<>();
                        } else if (fieldType == Set.class) {
                            newCollection = new LinkedHashSet<>();
                        } else {
                            throw new IllegalStateException("UnSupport field:" + field.getName() + " type:" + field.getType().getSimpleName());
                        }

                        Class elementTypeClass = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                        for (int i = 0; i < ((List) v).size(); i++) {
                            Object obj = ((List) v).get(i);
                            Object newElement = elementTypeClass.newInstance();
                            setObjectValue(newElement, obj);
                            newCollection.add(newElement);
                        }

                        field.set(result, newCollection);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new UnsupportedOperationException();
        }
    }

    private void throwError(JSONToken token) {
        String errorMsg = String.format("Unknown str:%s pos:%d", new String(buffer).substring(0, token.pos + 1), token.pos);
        throw new IllegalStateException(errorMsg);
    }

    private JSONToken findNextToken() {
        StringBuilder str = null;
        StringBuilder number = null;
        StringBuilder bool = null;

        for (;;) {
            if (currentPos == buffer.length) {
                return new JSONToken(currentPos - 1, JSONToken.TYPE_EOF, null);
            }

            byte value = buffer[currentPos++];
            switch (value) {
                case ' ':
                case '\\': {
                    continue;
                }
                case '{': {
                    return new JSONToken(currentPos - 1, JSONToken.TYPE_LEFT_BRACE, null);
                }
                case '}': {
                    return new JSONToken(currentPos - 1, JSONToken.TYPE_RIGHT_BRACE, null);
                }
                case '[': {
                    return new JSONToken(currentPos - 1, JSONToken.TYPE_LEFT_SQUARE, null);
                }
                case ']': {
                    return new JSONToken(currentPos - 1, JSONToken.TYPE_RIGHT_SQUARE, null);
                }
                case ':': {
                    return new JSONToken(currentPos - 1, JSONToken.TYPE_COLON, null);
                }
                case ',': {
                    return new JSONToken(currentPos - 1, JSONToken.TYPE_COMMA, null);
                }
                case '"': {
                    if (str == null) {
                        str = new StringBuilder();
                        continue;
                    }

                    if (str.length() > 0) {
                        return new JSONToken(currentPos - str.length(), JSONToken.TYPE_STRING, str.toString());
                    }
                }
                default: {
                    if (str != null) {
                        str.append((char)value);
                    } else {
                        boolean isNumber = false;
                        try {
                            Integer ignored = Integer.parseInt((char) value + "");
                            isNumber = true;
                        } catch (Exception ignored) {

                        }

                        byte nextValue = buffer[currentPos];
                        boolean isFinished = nextValue == ']' || nextValue == '}' || nextValue == ',';

                        if (isNumber) {
                            if (number == null) {
                                number = new StringBuilder();
                            }

                            number.append((char) value);

                            if (isFinished) {
                                return new JSONToken(currentPos - number.length(), JSONToken.TYPE_NUMBER, number.toString());

                            }
                        } else {
                            if (bool == null) {
                                bool = new StringBuilder();
                            }

                            bool.append((char) value);

                            if (isFinished) {
                                return new JSONToken(currentPos - bool.length(), JSONToken.TYPE_BOOL, Boolean.parseBoolean(bool.toString()));
                            }
                        }
                    }
                    break;
                }
            }
        }
    }
}
