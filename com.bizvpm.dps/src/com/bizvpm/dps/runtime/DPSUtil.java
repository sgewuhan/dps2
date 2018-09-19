package com.bizvpm.dps.runtime;

import java.io.File;

import org.eclipse.osgi.internal.loader.ModuleClassLoader;
import org.osgi.framework.Bundle;

@SuppressWarnings("restriction")
public class DPSUtil {
	
	public static String getTempDirector(Class<?> claz,boolean makeDir){
		
		String path = System.getProperty("java.io.tmpdir");
		ClassLoader cl = claz.getClassLoader();
		if(cl instanceof ModuleClassLoader){
			ModuleClassLoader loader = (ModuleClassLoader) cl;
			Bundle bundle = loader.getBundle();
			path += bundle.getSymbolicName()+File.separator;
		}else{
			path += claz.getName()+File.separator;
		}
		File file = new File(path);
		if(!file.isDirectory()){
			file.mkdirs();
		}

		return path;
	}

}
