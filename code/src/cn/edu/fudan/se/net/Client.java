package cn.edu.fudan.se.net;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class Client {
	
	public static String post(String urlStr,String repoUrl,String groupId,String artifactId,String version) {
		URL postUrl;
		try {
			postUrl = new URL(urlStr);		
        HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();       
        // 设置是否向connection输出，因为这个是post请求，参数要放在  
        // http正文内，因此需要设为true  
        connection.setDoOutput(true);  
        // Read from the connection. Default is true.  
        connection.setDoInput(true);  
        connection.setRequestMethod("POST");        
        // Post 请求不能使用缓存  
        connection.setUseCaches(false);      
        // 配置本次连接的Content-type，配置为application/x-www-form-urlencoded的  
        // 意思是正文是urlencoded编码过的form参数  
        connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");  
        // 连接，从postUrl.openConnection()至此的配置必须要在connect之前完成，  
        // 要注意的是connection.getOutputStream会隐含的进行connect。  
        connection.connect();  
        DataOutputStream out = new DataOutputStream(connection  
                .getOutputStream());  
        // 正文，正文内容其实跟get的URL中 '? '后的参数字符串一致  
        String content = "repoUrl=" + URLEncoder.encode(repoUrl, "utf-8")+"&"+"groupId=" + URLEncoder.encode(groupId, "utf-8")+"&"+"artifactId=" + URLEncoder.encode(artifactId, "utf-8")+"&"+"version=" + URLEncoder.encode(version, "utf-8");  
        // DataOutputStream.writeBytes将字符串中的16位的unicode字符以8位的字符形式写到流里面  
        out.writeBytes(content);  
        //流用完记得关  
        out.flush();  
        out.close();  
//        int length = connection.getContentLength();
//        is = connection.getInputStream();
//        is = new InputStreamReader(connection.getInputStream());
        connection.disconnect();  
        
        BufferedReader bf = new BufferedReader(new InputStreamReader(connection.getInputStream()));  
        StringBuffer buffer=new StringBuffer();  
        String line="";  
        while((line=bf.readLine())!=null){  
            buffer.append(line);  
        }  
        bf.close(); 
        return buffer.toString();
        
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;  
	}
	
	public static boolean readFromStream(InputStream inputStream) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));  
        String line;  
        try {
			while ((line = reader.readLine()) != null){  
			    System.out.println(line);
			}		
            reader.close();  
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
        return false; 
	}

	public static void main(String[] args) {
//		System.out.println(readFromStream(Client.post("http://127.0.0.1:8080","org.glassfish.hk2","examples","3.0.0-alpha.2-SNAPSHOT")));
//		System.out.println(readFromStream(Client.post("http://10.222.147.212:8080","ai.h2o","h2o-classic-parent","2.5")));
	}
}
