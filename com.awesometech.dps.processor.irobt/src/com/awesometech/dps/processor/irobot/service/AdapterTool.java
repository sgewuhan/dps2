package com.awesometech.dps.processor.irobot.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;

/**
 * QED 基础对象取值适配器
 * @author ThinkPad
 *
 */
public class AdapterTool {
	
	private List<String> minResultList = new ArrayList<String>();
	private List<String> minCopperAreaList = new ArrayList<String>();
	private List<String> decStringList = new ArrayList<String>();
	private List<String> basicStrList = new ArrayList<String>();
	private List<String> spanList = new ArrayList<String>();
	private List<String> drillSequenceTypeList = new ArrayList<String>();
	
	private static Document nameMapping;
	
	// 取出基础对象的值
	@SuppressWarnings("unchecked")
	public String getValue(String key, Object value) {
		String data = "";
		if(value instanceof List) {
			List<Document> valueList = (List<Document>)value;
			if(minResultList.contains(key)) {
				data = getMinValue(valueList,AdapterTool::minResultAdapter);
			}else if(minCopperAreaList.contains(key)) {
				data = getMinValue(valueList,AdapterTool::copperAreaAdapter);
			}else if(decStringList.contains(key)) {
				
			}else if(basicStrList.contains(key)) {
				
			}else if(spanList.contains(key)) {
				
			}else if(drillSequenceTypeList.contains(key)) {
				
			}
		}else {
			data = (String)value;
		}
		
		return data;
	}
	
	// 根据参数类型，计算total值
	public String calcDrilllTotal(String key, Document doc) {
		String[] value = new String[] {""};
		doc.forEach((k,v)->{
			// 如果T_MinResult 类型选择最小值
			if(minResultList.contains(key)) {
				value[0] = getMin(value[0],(String)v);
			}
			if(decStringList.contains(key)) {
				if(key.startsWith("Min")) {
					value[0] = getMin(value[0],(String)v);
				}else if(key.startsWith("Max")) {
					value[0] = getMax(value[0],(String)v);
				}
			}
			if(basicStrList.contains(key)) {
				if(isNumeric((String)v)) {
					if(isNumeric(value[0])) {
						value[0] = String.valueOf(Double.valueOf(value[0]) + Double.valueOf((String)v));
					}else {
						value[0] = String.valueOf(Double.valueOf((String)v));
					}
				}
			}		});
		return value[0];
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////// 
	
	// I8 有几个基础对象，以下是通用对象的XSD描述和解析方法
	
	// 基础对象一：T_DecString
	// XSD描述为
	/**
	 	 <xsd:simpleType name="T_DecString">
		    <xsd:restriction base="xsd:string">
		      <xsd:pattern value="-?\d+(\.\d*)?|NA|NE|NR|disabled|unknown|-"/>
		    </xsd:restriction>
		  </xsd:simpleType>
	 */
	// T_DecString 适配方法
	// TODO: 适配方法逻辑
	public void decStringAdapter() {
		
	}
	
	// 基础对象二：T_MinResult
	// XSD描述为
	/**
	 	 <xsd:complexType name="T_MinResult">
		    <xsd:simpleContent>
		      <xsd:extension base="T_DecString">
		        <xsd:attribute name="threshold" type="xsd:string"/>
		        <xsd:attribute name="thresholdExceeded" type="xsd:boolean"/>
		        <xsd:attribute name="orientation" type="xsd:string"/>
		      </xsd:extension>
		    </xsd:simpleContent>
		  </xsd:complexType>
	 */
	// T_MinResult 适配方法
	//  适配方法逻辑:
	public static String minResultAdapter(Document doc) {
		if("NA".equals(doc.getString("threshold")) || "unknown".equals(doc.getString("value"))) {
			return "unknown";
		}
		return doc.getString("value");
	}
	
	// 基础对象三：T_CopperArea
	// XSD描述为
	/**
		 <xsd:complexType name="T_CopperArea">
		    <xsd:simpleContent>
		      <xsd:extension base="T_DecString">
		        <xsd:attribute name="unitMode" type="T_UnitMode" use="required"/>
		      </xsd:extension>
		    </xsd:simpleContent>
		  </xsd:complexType>
	 */
	// T_CopperArea 适配方法
	//  适配方法逻辑:系统产生的数据的单位都是mm，这里在QED上显示的是dm2,所以需要做个单位转换
	public static String copperAreaAdapter(Document doc) {
		if(isNumeric(doc.getString("value"))) {
			return String.valueOf(Double.valueOf(doc.getString("value"))/10000);
		}
		return doc.getString("value");
	}
	
	// 基础对象三：T_DrillSequenceType
	// XSD描述为
	/**
		  <xsd:simpleType name="T_DrillSequenceType">
		    <xsd:restriction base="xsd:string">
		      <xsd:enumeration value="backdrill"/>
		      <xsd:enumeration value="buried"/>
		      <xsd:enumeration value="blind"/>
		      <xsd:enumeration value="NPTH"/>
		      <xsd:enumeration value="PTH"/>
		    </xsd:restriction>
		  </xsd:simpleType>
	 */
	// T_DrillSequenceType 适配方法
	//  
	public static String drillSequenceTypeAdapter(Document doc) {
		return "";
	}
	
	////////////////////////////////////////////////////////////////////////////////
	
	
	
	// 根据传递进去列表，以及取数方法，获取列表中的最小值
	public String getMinValue(List<Document> v,Function<Document,  String> mapper) {
		Optional<String> optionalStr = ((List<Document>) v).stream().map(mapper)
				.filter(n -> isNumeric(n)).min(new Comparator<String>() {
					public int compare(String o1, String o2) {
						if (Double.valueOf((String) o1) > Double.valueOf((String) o2)) {
							return 1;
						}
						return -1;
					}

				});
		if (null != optionalStr && optionalStr.isPresent()) {
			return(optionalStr.get());   // 0.203 
		} else {
			return "";  // [Document{{threshold=NA, value=unknown}}]
		}
	}

	// 将字符串转换成double，然后进行大小比对，返回最小值，如果其中一个不为数字，则将那个为数值的返回
	public String getMin (String str1, String str2) {
		if(isNumeric(str1) && isNumeric(str2)) {
			if(Double.valueOf(str1) < Double.valueOf(str2)) {
				return str1;
			}else {
				return str2;
			}
		}else if(isNumeric(str1) && !isNumeric(str2)) {
			return str1;
		}else if(!isNumeric(str1) && isNumeric(str2)) {
			return str2;
		}
		return "";
	}
	
	// 将字符串转换成double，然后进行大小比对，返回最小值，如果其中一个不为数字，则将那个为数值的返回
	public String getMax (String str1, String str2) {
		if(isNumeric(str1) && isNumeric(str2)) {
			if(Double.valueOf(str1) > Double.valueOf(str2)) {
				return str1;
			}else {
				return str2;
			}
		}else if(isNumeric(str1) && !isNumeric(str2)) {
			return str1;
		}else if(!isNumeric(str1) && isNumeric(str2)) {
			return str2;
		}
		return "";
	}
	
	
	// 判断值能否转换成为数字
	public static boolean isNumeric(String str) {
		// 该正则表达式可以匹配所有的数字 包括负数
		Pattern pattern = Pattern.compile("-?[0-9]+\\.?[0-9]*");
		String bigStr;
		try {
			bigStr = new BigDecimal(str).toString();
		} catch (Exception e) {
			return false;// 异常 说明包含非数字。
		}

		Matcher isNum = pattern.matcher(bigStr); // matcher是全匹配
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}
	
	 
	public static String adapterSummary(String name,String value) {
		String re = value;
		List<String> list = Arrays.asList(new String[] {"soldermask","legend","peeloffmask","carbonmask"});
		if(list.contains(name)) {
			switch(value) {
				case "0":
					re = "None";
					break;
				case "1":
					re = "Top";
					break;
				case "2":
					re = "Bottom";
					break;
				default:
					re = "Both";
					break;
			}
		}
		return re;
	}
	
	
	public static Document getNameMapping() {
		if(null == nameMapping) {
			nameMapping = readJsonFile("QEDMapping.json");
		}
		return nameMapping;
	}

	// 对QED文件处理进行转换，
	public static String mappingName(String pre) {
		if(null != getNameMapping().getString(pre)) {
			return nameMapping.getString(pre);
		}
		return pre;
	}
	
	public static Document readJsonFile(String fileName) {
		InputStream is = null;
		BufferedReader reader = null;
		try {
			is = AdapterTool.class.getResourceAsStream(fileName);
			reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
			String line = null;
			String content = "";
			while ((line = reader.readLine()) != null) {
				content += line + "\n";
			}
			reader.close();
			Document data = Document.parse(content);
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (is != null)
					is.close();
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public AdapterTool setMinResultList(List<String> minResultList) {
		this.minResultList = minResultList;
		return this;
	}
	public AdapterTool setDecStringList(List<String> decStrList) {
		this.decStringList = decStrList;
		return this;
	}
	public AdapterTool setBasicStrList(List<String> basicStrList) {
		this.basicStrList = basicStrList;
		return this;
	}
	public AdapterTool setSpanList(List<String> spanList) {
		this.spanList = spanList;
		return this;
	}
	public AdapterTool setMinCopperAreaList(List<String> minCopperAreaList) {
		this.minCopperAreaList = minCopperAreaList;
		return this;
	}
	public AdapterTool setDrillSequenceTypeList(List<String> drillSequenceTypeList) {
		this.drillSequenceTypeList = drillSequenceTypeList;
		return this;
	}

}
