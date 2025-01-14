//package com.etv.util.eshare;
//
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//
//public class SystemProperties {
//    private static Method sysPropGet;
//    private static Method sysPropSet;
//
//    private SystemProperties() {
//    }
//
//    static {
//        try {
//            Class<?> S = Class.forName("android.os.SystemProperties");
//            Method M[] = S.getMethods();
//            for (Method m : M) {
//                String n = m.getName();
//                if (n.equals("get")) {
//                    sysPropGet = m;
//                } else if (n.equals("set")) {
//                    sysPropSet = m;
//                }
//            }
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static String get(String name, String default_value) {
//        try {
//            return (String) sysPropGet.invoke(null, name, default_value);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return default_value;
//    }
//
//    public static void set(String name, String value) {
//        try {
//            sysPropSet.invoke(null, name, value);
//        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
