package cn.edu.fudan.se.util;

import java.io.FileWriter;
import java.io.IOException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class FastJsonUtil {

    public static void writeJson(String savePath, JSONArray array, boolean isFormatted) {
        try {
            FileWriter f = new FileWriter(savePath);
            if(isFormatted)
            	f.write(JSON.toJSONString(array,SerializerFeature.PrettyFormat,SerializerFeature.WriteMapNullValue));// 写入
            else
            	f.write(array.toString());// 写入
            f.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void writeJson(String savePath, JSONObject obj, boolean isFormatted) {
        try {
            FileWriter f = new FileWriter(savePath);
            if(isFormatted)
            	f.write(JSON.toJSONString(obj,SerializerFeature.PrettyFormat,SerializerFeature.WriteMapNullValue));// 写入
            else
            	f.write(obj.toString());// 写入
            f.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void writeJsonString(String savePath, String obj) {
        try {
            FileWriter f = new FileWriter(savePath);
            f.write(obj);// 写入
            f.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
