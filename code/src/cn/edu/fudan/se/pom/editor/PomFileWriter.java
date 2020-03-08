package cn.edu.fudan.se.pom.editor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.shared.utils.io.IOUtil;

public class PomFileWriter {

	public static void writeModelToPomFile(File pomFile, Model model) {
		Writer writer = null;
	    try {
	        writer = new FileWriter(pomFile);
	        MavenXpp3Writer pomWriter = new MavenXpp3Writer();
	        pomWriter.write(writer, model);
	    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    finally {
	        IOUtil.close(writer);
	    }
	}
}
