package com.bizvpm.dps.ui.view;

import java.util.ArrayList;
import java.util.List;

import com.bizvpm.dps.service.ProcessorConfig;

public class ProcessorCatalog {

	private String name;

	private List<Object> children;

	public ProcessorCatalog() {
		children = new ArrayList<Object>();
	}

	public List<Object> getChildren() {
		return children;
	}

	public void setChildren(List<Object> children) {
		this.children = children;
	}

	public void addProcessor(ProcessorConfig processor, String catalog) {
		//直接添加到本级别
		if (catalog == null || catalog.isEmpty()) {
			children.add(processor);
			return;
		}
		
		int index = catalog.indexOf("/");
		if(index ==-1){
			//添加到下级
			ProcessorCatalog child = getChildCatalog(catalog);
			child.addProcessor(processor, null);
			return;
		}
		
		String childName = catalog.substring(0, catalog.indexOf("/"));
		ProcessorCatalog child = getChildCatalog(childName);
		
		String childPath = catalog.substring(catalog.indexOf("/")+1, catalog.length());
		child.addProcessor(processor, childPath);
	}
	
	private ProcessorCatalog getChildCatalog(String name) {
		for (Object object : children) {
			if(object instanceof ProcessorCatalog){
				if(((ProcessorCatalog) object).getName().equals(name)){
					return (ProcessorCatalog) object;
				}
			}
		}
		ProcessorCatalog cata = new ProcessorCatalog();
		cata.setName(name);
		children.add(cata);
		return cata;
	}

//	public static void main(String[] args) {
//		String catalog = "Windchill DS/零部件/aaa";
//		System.out.println(catalog.substring(0, catalog.indexOf("/")));
//		System.out.println(catalog.substring(catalog.indexOf("/")+1, catalog.length()));
//	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
}
