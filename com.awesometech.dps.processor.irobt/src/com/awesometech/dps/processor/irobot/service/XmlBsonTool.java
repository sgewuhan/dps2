package com.awesometech.dps.processor.irobot.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * 将 org.dom4j.Document 转换成 org.bson.Document
 *  注意：此类的XML解析不能作为通用类，只能用以解析I8的XML文件，主要原因在于方法elementToBSONObject做了针对I8的QED的特殊设置
 *  	起因：I8的QED中的XML中存在大量的将“值”和“属性”混杂在一起使用情况（下有示例），而且属性和值都需要使用，这就会导致在bson对象在表达的时候无法表达值
      <MinDistCopperToScoredOutline threshold="1.6" orientation="Bottom">0.203</MinDistCopperToScoredOutline>
      <MinDistCopperToScoredOutline threshold="1.6" orientation="Left">0.246</MinDistCopperToScoredOutline>
 *   解决方法：将“值”用value存储放到该元素下，示例的解析结果将变成：
 		MinDistCopperToScoredOutline:[{threshold:"1.6",orientation:"Bottom",value:"0.203"},{threshold:"1.6",orientation:"Left",value:"0.246"}]
 * @author Zhangjc
 *
 */
public class XmlBsonTool {

	public static org.bson.Document xmlFileToBSONObject(String filePath) {
		String xml = readXmlFile(filePath);
		return documentToBSONObject(xml);
	}
	
	/**
	 * org.dom4j.Document 转 BasicBSONObject
	 * 
	 * @param xml
	 * @return
	 * @throws DocumentException
	 */
	public static org.bson.Document documentToBSONObject(String xml) {
		return elementToBSONObject(strToDocument(xml).getRootElement());
	}

	/**
	 * String 转 org.dom4j.Document
	 * 
	 * @param xml
	 * @return
	 * @throws DocumentException
	 */
	public static Document strToDocument(String xml) {
		try {
			return DocumentHelper.parseText(xml);
		} catch (DocumentException e) {
			return null;
		}
	}
	
	/**
	 * org.dom4j.Element 转 BasicBSONObject
	 * 
	 * @param node
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static org.bson.Document elementToBSONObject(Element node) {
		org.bson.Document bsonDoc = new org.bson.Document();
		// 当前节点的名称、文本内容和属性
		List<Attribute> listAttr = node.attributes();// 当前节点的所有属性的list
		for (Attribute attr : listAttr) {// 遍历当前节点的所有属性
			bsonDoc.put(attr.getName(), attr.getValue());
		}
		// 递归遍历当前节点所有的子节点
		List<Element> listElement = node.elements();// 所有一级子节点的list
		if (!listElement.isEmpty()) {
			for (Element e : listElement) {// 遍历所有一级子节点
				if (e.attributes().isEmpty() && e.elements().isEmpty()) // 判断一级节点是否有属性和子节点
					bsonDoc.put(e.getName(), e.getTextTrim());// 沒有则将当前节点作为上级节点的属性对待
				else {
					if (!bsonDoc.containsKey(e.getName())) // 判断父节点是否存在该一级节点名称的属性
						bsonDoc.put(e.getName(), new ArrayList<org.bson.Document>());// 没有则创建
					 org.bson.Document doc = elementToBSONObject(e);
					 // FIXME 针对I8 QED的特殊处理逻辑
					 if(!"".equals(e.getTextTrim())) {
						 doc.append("value", e.getTextTrim());
					 }
					((ArrayList<org.bson.Document>) bsonDoc.get(e.getName())).add(doc);// 将该一级节点放入该节点名称的属性对应的值中
				}
			}
		}
		return bsonDoc;
	}
	
	public static String readXmlFile(String fileName) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(fileName);
			int b;
			String content = "";
			while ((b = fis.read()) != -1) {
				content += (char)b;
			}
			return content;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (fis != null)
					fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
