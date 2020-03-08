package cn.edu.fudan.se.pom.handler;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import cn.edu.fudan.se.util.FileUtil;

public class PomFileReader {
	

	public static Model parsePomFileToModel(String completePath) {	
//		System.out.println(completePath);
		MavenXpp3Reader reader = new MavenXpp3Reader();	

		Model model = null;
		try {
			FileReader fr = new FileReader(completePath); 
			model = reader.read(fr);
			fr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			System.out.println("XmlPullParserException");
			FileUtil.appendLine("D:/error.txt", completePath);
//			e.printStackTrace();
			return null;
		}       		
		return model;	
//		if(completePath.equals("F:/GP/high_quality_repos/apache/hbase/hbase-mapreduce/pom.xml")
//				||completePath.equals("F:/GP/high_quality_repos/apache/hbase/hbase-mapreduce/../hbase-build-configuration/pom.xml")
//				||completePath.equals("F:/GP/high_quality_repos/apache/hbase/hbase-mapreduce/../hbase-build-configuration/../pom.xml")) {
//			System.out.println(completePath);
//			readModel(model,pomPath,isPomLib,isParent);	
//		}			
	}

	public static Model parsePomFileToModel(InputStream inputStream) {	
		if(inputStream == null)
			return null;
		MavenXpp3Reader reader = new MavenXpp3Reader();

		Model model = null;
		try {
			model = reader.read(inputStream);
			inputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			System.out.println("XmlPullParserException");
//			e.printStackTrace();
			return null;
		}       		

		return model;
//		if(model == null)
//			return;		
////		System.out.println("++++++++++++++++++++++++++++"+pomPath);
//		readModel(model,"",isPomLib,isParent);		
		
	}
}
