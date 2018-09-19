package com.sg.sqldb.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class SQLResult {

	private String[] columns;
	private List<SQLRow> data = new ArrayList<SQLRow>();
	private String dataSource;

	public void setColumns(String[] columns) {
		this.columns = columns;
	}
	
	public String[] getColumns(){
		return columns;
	}

	public void add(SQLRow row) {
		data .add(row);
	}
	
	public void add(int index,SQLRow row) {
		data. add(index,row);
	}

	public boolean isEmpty() {
		return data.size()<1;
	}

	public List<SQLRow> getData() {
		return data;
	}

	public int findNext(int from,String text,int style){
		for(int i=from;i<data.size();i++){
			SQLRow row = data.get(i);
			int index = row.find(text, style);
			if(index!=-1){
				return i;
			}
		}
		return -1;
	}

	public int findPrev(int from,String text,int style){
		for(int i=from;i>=0;i--){
			SQLRow row = data.get(i);
			int index = row.find(text, style);
			if(index!=-1){
				return i;
			}
		}
		return -1;
	}
	
	
	public int size() {
		return data==null?0:data.size();
	}

	public Iterator<SQLRow> iterator() {
		return data.iterator();
	}

	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	public String getDataSource() {
		return dataSource;
	}

	public void remove(Object[] rows) {
		List<Object> tobeRemove = Arrays.asList(rows);
		data.removeAll(tobeRemove);
	}

	public String getJson() {
		if(isEmpty()){
			return null;
		}
		
		
		
		String jsonString = "[";
		for(int i =0 ; i <size();i++){
			jsonString+="{";
			
			SQLRow row = data.get(i);
			
			for(int j=0;j<columns.length;j++){
				jsonString += "\""+columns[j].toLowerCase()+"\":" + getJsonValue(row.getValue(columns[j]));
				if(j<columns.length-1){
					jsonString += ",";
				}
			}
			
			jsonString+="}";
			
			if(i<size()-1){
				jsonString+=",";
			}
		}
		
		jsonString = jsonString+"]";
		return jsonString;
	}

	private String getJsonValue(Object value) {
		if(value == null){
			return "null";
		}
		if(value instanceof String){
			return "\""+value+"\"";
		}
		if(value instanceof Boolean){
			return ((Boolean)value)?"true":"false";
		}
		if(value instanceof Date){
			return Util.SDF_YYYY__MM__DD.format((Date)value);
		}
		return ""+value;
	}

}
