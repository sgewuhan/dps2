package com.sg.sqldb.utility;

import java.util.HashMap;

import org.eclipse.core.runtime.Assert;

public class SQLRow {

	private Object[] data;
	private String[] columns;
	private Object code;

//	private SQLResult result;

//	private int index;

	public SQLRow(String[] columns){//, int index) {
//		this.result = result;
		this.columns = columns;
		data = new Object[columns.length];
//		this.index = index;
	}

	public Object getValue(int colNo) {
		Assert.isTrue(data != null && colNo >= 0 && colNo < data.length,
				"Given Column Number Exceed Data Coumn Count");
		return data[colNo];
	}

	public String getText(int colNo) {
		Object value = getValue(colNo);
		return Util.getString(value);
	}
	
	public String getText(String columnName){
		Object value = getValue(columnName);
		return Util.getString(value);
	}

	public void setValue(int colNo, Object value) {
		data[colNo] = value;
	}
	
	public void setValue(String name, Object value) {
		int index = Util.indexOfIngnoreCase(columns, name);
		if (index != -1)
			setValue(index,value);
	}

	public int find(String text, int style) {
		for (int i = 0; i < data.length; i++) {
			if (Util.isMatch(text, getText(i), style)) {
				return i;
			}
		}
		return -1;
	}

	public Object getValue(String name) {
		int index = Util.indexOfIngnoreCase(columns, name);
		if (index != -1)
			return getValue(index);
		return null;
	}

	public Object[] getData() {
		return data;
	}
	
	public String[] getColumns(){
		return columns;
	}

	public void update(Object[] data, String[] columns) {
		this.data = data;
		if(columns!=null){
			this.columns = columns;
		}
	}
	

	public void update(SQLRow newData) {
		update(newData.data,newData.columns);
	}
	
	
	public void setResult(SQLResult result){
		result.add(this);
	}

	public void setCode(Object code) {
		this.code = code;
	}

	public Object getCode() {
		return code;
	}

    public HashMap<String, Object> toMap() {
        HashMap<String, Object> item = new HashMap<String,Object> ();
        for (int i = 0; i < columns.length; i++) {
            item.put(columns[i], data[i]);
        }
        
        return item;
    }


//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + index;
//		return result;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		SQLRow other = (SQLRow) obj;
//		if (index != other.index)
//			return false;
//		return true;
//	}

}
