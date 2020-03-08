package cn.edu.fudan.se.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;

import cn.edu.fudan.se.util.MyException;

public class AstParserUtil {
	
	public static int errorFile = 0;
	public static int totalFile = 0;

	public static void perserFile(String path, String charset) {			
		try {
			File jarFile = new File(path);
			JarFile jar = new JarFile(jarFile);
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                if (!entry.isDirectory() && entry.getName().endsWith(".java")) {
                	String importClass = entry.getName().substring(0, entry.getName().length()-5).replace("/", ".");
//                	System.out.println("+++++++++++++++++++++++++++ "+importClass);
//                	getApi(getCompilationUnit(jar.getInputStream(entry)),importClass);
//                	count ++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public static CompilationUnit getCompilationUnit(InputStream is){
		CompilationUnit compilationUnit = null;
		try {
			compilationUnit = JavaParser.parse(is);
		} catch (ParseProblemException e) {
			System.out.println("ParseProblemException : ");
		}
		return compilationUnit;
	}
	
	public static CompilationUnit getCompilationUnit(String filePath){
		CompilationUnit compilationUnit = null;
		try {
			totalFile ++;
			compilationUnit = JavaParser.parse(new File(filePath));
			if(compilationUnit == null)
				throw new MyException("compilationUnit is null: " + filePath);
		} catch (ParseProblemException e) {
			errorFile ++;
			System.out.println("ParseProblemException : ");
//			e.printStackTrace();
		}
		catch (FileNotFoundException e) {
			errorFile ++;
//			e.printStackTrace();
		} catch (MyException e) {
			errorFile ++;
			// TODO Auto-generated catch block
//			System.exit(0);
		}
		return compilationUnit;
	}

}