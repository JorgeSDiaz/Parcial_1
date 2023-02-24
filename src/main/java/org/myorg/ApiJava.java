package org.myorg;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ApiJava {
    public String Class(String className) {
        Class<?> aClass = null;
        try {
            aClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        List<Method> methods = List.of(aClass.getMethods());
        List<Field> fields = List.of(aClass.getDeclaredFields());

        return "{" + className + ":" + "Methods: " +
                methods.stream().map((Method method) -> "\"" + method.toString() + "\"").collect(Collectors.toList()) +
                ", Fields: " +
                fields.stream().map((Field field) -> "\"" + field.toString() + "\"").collect(Collectors.toList()) + "}";
    }

    public Object invoke(String className, String methodInvoke) {
        Class<?> aClass = null;
        try {
            aClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        Method method = null;
        try {
            method = aClass.getMethod(methodInvoke);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        Object res = null;

        try {
            res = method.invoke(null);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        return res;
    }

    public Object unaryInvoke(String className, String methodInvoke, String typeParam, String param) {
        Class<?> aClass = null;
        try {
            aClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        Object res = null;
        Method method = null;
        switch (typeParam) {
            case "int":
                try {
                    method = aClass.getMethod(methodInvoke, int.class);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
                try {
                    res = method.invoke(null, Integer.parseInt(param));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "double":
                try {
                    method = aClass.getMethod(methodInvoke, double.class);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
                try {
                    res = method.invoke(null, Double.parseDouble(param));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "String":
                try {
                    method = aClass.getMethod(methodInvoke, String.class);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
                try {
                    res = method.invoke(null, param);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
                break;
            default:
                try {
                    res = method.invoke(null, (Object)param);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
                break;
        }

        return res;
    }

    public Object binaryInvoke(String className, String methodInvoke,
                              String typeFirstParam, String firstParamValue,
                              String typeSecondParam, String secondParamValue)
    {
        Class<?> aClass = null;
        try {
            aClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            return "Class not Found";
        }

        Object res = null;
        Method method = null;

        try {
            method = aClass.getMethod(methodInvoke, Class.forName(typeFirstParam), Class.forName(typeFirstParam));
            res = method.invoke(null, Class.forName(typeFirstParam).cast(firstParamValue),
                    Class.forName(secondParamValue).cast(secondParamValue));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
            return "Invalid Method";
        }

        return res;
    }

}
