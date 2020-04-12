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
 * �� org.dom4j.Document ת���� org.bson.Document
 *  ע�⣺�����XML����������Ϊͨ���ֻ࣬�����Խ���I8��XML�ļ�����Ҫԭ�����ڷ���elementToBSONObject�������I8��QED����������
 *  	����I8��QED�е�XML�д��ڴ����Ľ���ֵ���͡����ԡ�������һ��ʹ�����������ʾ�������������Ժ�ֵ����Ҫʹ�ã���ͻᵼ����bson�����ڱ���ʱ���޷����ֵ
      <MinDistCopperToScoredOutline threshold="1.6" orientation="Bottom">0.203</MinDistCopperToScoredOutline>
      <MinDistCopperToScoredOutline threshold="1.6" orientation="Left">0.246</MinDistCopperToScoredOutline>
 *   �������������ֵ����value�洢�ŵ���Ԫ���£�ʾ���Ľ����������ɣ�
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
	 * org.dom4j.Document ת BasicBSONObject
	 * 
	 * @param xml
	 * @return
	 * @throws DocumentException
	 */
	public static org.bson.Document documentToBSONObject(String xml) {
		return elementToBSONObject(strToDocument(xml).getRootElement());
	}

	/**
	 * String ת org.dom4j.Document
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
	 * org.dom4j.Element ת BasicBSONObject
	 * 
	 * @param node
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static org.bson.Document elementToBSONObject(Element node) {
		org.bson.Document bsonDoc = new org.bson.Document();
		// ��ǰ�ڵ�����ơ��ı����ݺ�����
		List<Attribute> listAttr = node.attributes();// ��ǰ�ڵ���������Ե�list
		for (Attribute attr : listAttr) {// ������ǰ�ڵ����������
			bsonDoc.put(attr.getName(), attr.getValue());
		}
		// �ݹ������ǰ�ڵ����е��ӽڵ�
		List<Element> listElement = node.elements();// ����һ���ӽڵ��list
		if (!listElement.isEmpty()) {
			for (Element e : listElement) {// ��������һ���ӽڵ�
				if (e.attributes().isEmpty() && e.elements().isEmpty()) // �ж�һ���ڵ��Ƿ������Ժ��ӽڵ�
					bsonDoc.put(e.getName(), e.getTextTrim());// �]���򽫵�ǰ�ڵ���Ϊ�ϼ��ڵ�����ԶԴ�
				else {
					if (!bsonDoc.containsKey(e.getName())) // �жϸ��ڵ��Ƿ���ڸ�һ���ڵ����Ƶ�����
						bsonDoc.put(e.getName(), new ArrayList<org.bson.Document>());// û���򴴽�
					 org.bson.Document doc = elementToBSONObject(e);
					 // FIXME ���I8 QED�����⴦���߼�
					 if(!"".equals(e.getTextTrim())) {
						 doc.append("value", e.getTextTrim());
					 }
					((ArrayList<org.bson.Document>) bsonDoc.get(e.getName())).add(doc);// ����һ���ڵ����ýڵ����Ƶ����Զ�Ӧ��ֵ��
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
