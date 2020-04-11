package com.awesometech.dps.processor.irobot.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.io.IOException;
import java.math.BigDecimal;

import javax.xml.bind.JAXBException;
import org.bson.Document;

import com.awesometech.dps.processor.irobot.Activator;
import com.awesometech.dps.processor.irobot.preferences.IRobotPreferenceConstants;

/**
 * QED������
 * 
 * @author Zhangjc
 *
 */
public class QedService {

	private String qedPath;

	private String workPath;

	private final static String PID = "%pid%";

	private final static String FILENAME = "%fileName%";

	// ���ݴ����jobId���н���QED
	// TODO ע��ԭʼ��QED����ҲҪ���ػ�ȥ��ԭʼ�ı����Ǵ�������ǵ�rfq����
	public Document hanldeQed(String jobId, String fileName) throws JAXBException, IOException {
		init(jobId, fileName);
		Document irobotQed = XmlBsonTool.xmlFileToBSONObject(qedPath);
		Document qed = qedAdapter(irobotQed, new Document());
		Document doc = new Document();
		doc.append("irobotQed", irobotQed);
		doc.append("qed", qed);
		return doc;
	}

	private void init(String jobId, String fileName) {
		qedPath = Activator.getDefault().getPreferenceStore().getString(IRobotPreferenceConstants.IRobot_QED_PATH);
		workPath = Activator.getDefault().getPreferenceStore().getString(IRobotPreferenceConstants.IRobot_WORK_PATH);
		qedPath = qedPath.replaceAll(PID, jobId).replaceAll(FILENAME, fileName);
		workPath = workPath.replaceAll(PID, jobId).replaceAll(FILENAME, fileName);

	}

	public static void main(String[] args) {
		try {
			new QedService().hanldeQed("90", "1585634669317");
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////
	// ҵ��������
	
	// ҵ�����һ��T_SummaryParameter
	// XSD����Ϊ
	/**
	 	<xsd:complexType name="T_SummaryParameter">
		    <xsd:simpleContent>
		      <xsd:extension base="xsd:string">
		        <xsd:attribute name="name" use="required"/>
		      </xsd:extension>
		    </xsd:simpleContent>
		  </xsd:complexType>
	 */
	// SummaryParameter  ��Ϊһ��Document List�������Document�������������ݣ�name��value
	// ����취����List�е�ÿ�����ݵ� name��value����ΪQED�е�һ��key->value
	public void summaryParameterAdapter(Document qed,List<Document> summaryParameterList) {
		summaryParameterList.stream().forEach(c -> {
			// TODO ��Ҫ�����Ƶ�adapter������������ҷ�QED������
			qed.append(AdapterTool.mappingName(c.getString("name")),
					AdapterTool.adapterSummary(c.getString("name"), c.getString("value")));
		});
	}
	
	// ҵ�����һ��T_CopperLayer
	// XSD����Ϊ
	/**
		<xsd:complexType name="T_CopperLayer">
		    <xsd:sequence>
		      <xsd:element name="MinTrack" type="T_MinResult"/>
		      <xsd:element name="MinTrackAllCopper" type="T_MinResult"/>
		      <xsd:element name="MinTrackCriticalTrace" type="T_MinResult"/>
		      <xsd:element name="MinTrackAllTrace" type="T_MinResult"/>
		      <xsd:element name="MinGap" type="T_MinResult"/>
		      <xsd:element name="MinSelfSpacing" type="T_MinResult"/>
		      <xsd:element name="MinGapPP" type="T_MinResult"/>
		      <xsd:element name="MinGapPT" type="T_MinResult"/>
		      <xsd:element name="MinGapTT" type="T_MinResult"/>
		      <xsd:element name="MinGapTraceTrace" type="T_MinResult" minOccurs="0"/>
		      <xsd:element name="MinRing" type="T_MinResult"/>
		      <xsd:element name="MinDistCopperToPlated" type="T_MinResult"/>
		      <xsd:element name="MinDistCopperToNonPlated" type="T_MinResult"/>
		      <xsd:element name="MinDistCopperToBackdrill" type="T_MinResult"/>
		      <xsd:element name="MinDistCopperToOutline" type="T_MinResult"/>
		      <xsd:element name="MinDistCopperToOutlinePads" type="T_MinResult" minOccurs="0"/>
		      <xsd:element name="MinDistCopperToOutlineTracks" type="T_MinResult" minOccurs="0"/>
		      <xsd:element name="MinDistCopperToOutlineRegions" type="T_MinResult" minOccurs="0"/>
		      <xsd:element name="MinDistCopperToScoredOutline" type="T_MinResult" maxOccurs="4"/>
		      <xsd:element name="MinDistViaPlug" type="T_MinResult"/>
		      <xsd:element name="CopperArea" type="T_CopperArea"/>
		      <xsd:element name="EtchCompensation" type="T_DecString" minOccurs="0"/>
		    </xsd:sequence>
		    <xsd:attribute name="layerOrGroupRef" type="xsd:string" use="required"/>
		  </xsd:complexType>
	 */
	public void copperLayerAdapter(Document qed, List<Document> copperLayerList) {
		// ���������Ƿ�top���bottom��
		List<String> layerOrGroupRef = Arrays.asList(new String[] { "top", "bot" }); // ����Ϊ string,�����⴦����
		// ��������ÿ�����Ե�������
		List<String> minResultList = Arrays
				.asList(new String[] { "MinTrack", "MinTrackAllCopper", "MinTrackCriticalTrace", "MinTrackAllTrace",
						"MinGap", "MinSelfSpacing", "MinGapPP", "MinGapPT", "MinGapTT", "MinGapTraceTrace", "MinRing",
						"MinDistCopperToPlated", "MinDistCopperToNonPlated", "MinDistCopperToBackdrill",
						"MinDistCopperToOutline", "MinDistCopperToOutlinePads", "MinDistCopperToOutlineTracks",
						"MinDistCopperToOutlineRegions", "MinDistCopperToScoredOutline", "MinDistViaPlug" });  // ����ΪT_MinResult
		List<String> minCopperAreaList = Arrays.asList(new String[] { "CopperArea" });  // ����ΪT_CopperArea
		List<String> decStringList = Arrays.asList(new String[] { "EtchCompensation" }); // ����ΪT_DecString

		AdapterTool atl = new AdapterTool();
		atl.setMinResultList(minResultList).setMinCopperAreaList(minCopperAreaList).setDecStringList(decStringList);
		
		Document minIn = new Document();
		Document minOut = new Document();
		// �����List��һ��ÿ�����ݵ��б�
		copperLayerList.stream().forEach(c -> {
			Document cp = (Document) c;
			boolean isTopBot = layerOrGroupRef.contains(cp.getString("layerOrGroupRef"));
			cp.forEach((k, v) -> {
				// ���ݴ���ץȡÿ��������ֵ
				String value = atl.getValue(k, v);
				// ���ݴ��������ݽ��з��࣬�ж���out����in��Ȼ��ȡ�������ֵ����minIn��minOut�����ֵ���бȶ�,��ȡ���е���Сֵ
				if (isTopBot) {
					minOut.append(k, getMin(minOut.getString(k),value));
				} else {
					minIn.append(k, getMin(minIn.getString(k),value));
				}
			});
		});
		// TODO ����������Ҫ������
		// ������ת���� ����չʾ��Ҫȥ���б�
		List copperList = minResultList.stream().map(s -> {
				Document doc = new Document();
				doc.append("type", s);
				doc.append("outer", minOut.getString(s));
				doc.append("inner", minIn.getString(s));
				return doc;
			}).collect(Collectors.toList());
		qed.append("ED_CopperLayers", copperList);
	}
	
	// ҵ��������T_DrillSequences
	// XSD����Ϊ
	/**
	   <xsd:complexType name="T_DrillSequences">
		    <xsd:sequence>
		      <xsd:element name="DrillSequence" type="T_DrillSequence" minOccurs="0" maxOccurs="unbounded"/>
		    </xsd:sequence>
		    <xsd:attribute name="id" type="xsd:string" use="required"/>
	   </xsd:complexType>
	   
	   <xsd:complexType name="T_DrillSequence">
	    <xsd:sequence minOccurs="0" maxOccurs="unbounded">
	      <xsd:element name="Span" type="T_Span" minOccurs="0"/>
	      <xsd:element name="Tools" type="xsd:integer"/>
	      <xsd:element name="MinEndDia" type="T_DecString"/>
	      <xsd:element name="MaxEndDia" type="T_DecString"/>
	      <xsd:element name="MinToolDia" type="T_DecString" minOccurs="0"/>
	      <xsd:element name="MaxToolDia" type="T_DecString" minOccurs="0"/>
	      <xsd:element name="Holes" type="xsd:integer"/>
	      <xsd:element name="MinRingOnOuter" type="T_MinResult"/>
	      <xsd:element name="MinRingOnInner" type="T_MinResult"/>
	      <xsd:element name="MinClrCopper" type="T_MinResult"/>
	      <xsd:element name="MinClr" type="T_MinResult" minOccurs="0">
	        <xsd:annotation>
	          <xsd:documentation>Optional by license</xsd:documentation>
	        </xsd:annotation>
	      </xsd:element>
	      <xsd:element name="OverlappingHoles" type="xsd:string" minOccurs="0">
	        <xsd:annotation>
	          <xsd:documentation>Optional by license</xsd:documentation>
	        </xsd:annotation>
	      </xsd:element>
	      <xsd:element name="MinClrInnerSpan" type="T_MinResult" minOccurs="0">
	        <xsd:annotation>
	          <xsd:documentation>Optional by license</xsd:documentation>
	        </xsd:annotation>
	      </xsd:element>
	      <xsd:element name="MinClrOutline" type="T_MinResult"/>
	      <xsd:element name="MinClrOutlineTracks" type="T_MinResult" minOccurs="0"/>
	    </xsd:sequence>
	    <xsd:attribute name="type" type="T_DrillSequenceType" use="required"/>
	    
	    <xsd:simpleType name="T_DrillSequenceType">
		    <xsd:restriction base="xsd:string">
		      <xsd:enumeration value="backdrill"/>
		      <xsd:enumeration value="buried"/>
		      <xsd:enumeration value="blind"/>
		      <xsd:enumeration value="NPTH"/>
		      <xsd:enumeration value="PTH"/>
		    </xsd:restriction>
		  </xsd:simpleType>
	  </xsd:complexType>
	  
	  <xsd:complexType name="T_Span">
	    <xsd:attribute name="fromLayer" type="xsd:string"/>
	    <xsd:attribute name="toLayer" type="xsd:string"/>
	  </xsd:complexType>
	 */
	// T_DrillSequences  ��Ϊһ��Document List�������Document��id��Ϊ����,�����ŵ���T_DrillSequenceType
	// ����취��
	// TODO:
	@SuppressWarnings("unchecked")
	public void drillSequencesAdapter(Document qed, List<Document> sequencesList) {
		List<String> minResultList = Arrays.asList(new String[] { "MinRingOnOuter", "MinRingOnInner", "MinClrCopper",
				"MinClr", "MinClrInnerSpan", "MinClrOutline", "MinClrOutlineTracks" }); // ����ΪT_MinResult
		List<String> decStringList = Arrays.asList(new String[] { "MinEndDia", "MaxEndDia", "MinToolDia", "MaxToolDia", }); // ����ΪT_DecString
		List<String> basicStrList = Arrays.asList(new String[] { "Tools", "Holes", "OverlappingHoles" }); // ����Ϊinteger,string�����������
		List<String> spanList = Arrays.asList(new String[] { "Span" }); // ����ΪT_Span
		List<String> drillSequenceTypeList = Arrays.asList(new String[] { "T_DrillSequenceType" }); // ����ΪT_DrillSequenceType

//		String[] types = new String[] { "PTH", "NPTH", "blind", "buried", "backdrill" };

		AdapterTool atl = new AdapterTool();
		atl.setMinResultList(minResultList).setDecStringList(decStringList).setBasicStrList(basicStrList)
				.setSpanList(spanList).setDrillSequenceTypeList(drillSequenceTypeList);

		Map<String, Document> dataMap = new LinkedHashMap<String, Document>();   

		List<Document> drillDetails = new ArrayList<Document>();
		
		sequencesList.stream().map(d -> {
			// TODO �����������һ��Ľڵ��б�
			d.get("id"); // original Ŀǰ������һ�����һ��ֵ��������ʱ�������
			return d.get("DrillSequence"); // �������������Ҫ�� ������Ҳ��һ��documentList
		}).filter(ds -> ds instanceof List).forEach(l -> { // ֻ���� List<Document>,ͬʱ��null���ݹ��˵�,ҵ����ΪList<T_DrillSequence>
			((List<Document>) l).stream().forEach(m -> {
				// TODO Span �Ĵ�����qed 29.xml���� spanò��Ҳ����ͷ��Ҳ����˵��type��ͬ�����
				Document doc = new Document(m);
				Document drillLayer = new Document();
				String type = doc.getString("type");
				drillLayer.append("type", type);
				doc.remove("type"); // ��������ΪҪ��type��ȡ������Ϊ����,����������key��Ϊ����,ƴװ��Ϊһ��������Խ���remove��
				doc.forEach((k, v) -> {
					// ��ȡʵ��ֵ
					String data = atl.getValue(k, v);
					// TODO ��Ҫ������ӳ���ת��
					drillLayer.append(k, data);
					if (null == dataMap.get(k)) {
						dataMap.put(k, new Document());
					}
					// ת����Ӧ��ϵ����ֵת������kΪ��ͷ�������ݣ�type ��ֵΪkey
					dataMap.get(k).put(type, data);
				});
				drillDetails.add(drillLayer);
			});
		});
		// ��key->Dcoument ת����typeΪ��ͷ�������ݣ�����ÿ�����ݽ���total����
		List<Document> list = new ArrayList<Document>();
		dataMap.forEach((k, v) -> {
			// ����totalֵ���Բ�ͬ���͵��ֶ����Լ��㷽ʽ���в����
			// TODO ������������Ҫucamco������ȷ�ϣ����ڰ��������ݶԱȳ��Ľ�����м��㣬���ܲ�̫׼ȷ
			String total = atl.calcDrilllTotal(k,(Document)v);
			Document doc = new Document();
			doc.append("type", k);
			doc.append("total", total);
			doc.putAll(v);
			list.add(doc);
		});
		qed.append("ED_DrillSummary", list);
	}
	
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////
	// ��ΪQED����ÿ����������List��ʽ���ڣ������NULL�жϷǳ����ô�������������÷�ʽΪ��
	// ��QED�������е�Ԫ�ؽ��б�����ץ��������Ҫ�Ľڵ㣬Ȼ�����QED.XSD���н���
	// ͬʱ����QED�е�XML���ֽڵ�������Ҫ���õ�ֵ����attribute��ʽ���ڣ���������element����ʽ���ڣ����в�����ͬʱ���ڣ������޷������ȫ��ͳһ��������������н����ÿ��������е����Ĵ���
	
	// ��I8QED�����������Ҫ��QED
	@SuppressWarnings("unchecked")
	public Document qedAdapter(Document irobotQed,Document qed) {
		irobotQed.forEach((key, value) -> {
			// QED �� Summary ���ִ���
			if (key.equals("SummaryParameter")) {
				summaryParameterAdapter(qed,((List<Document>) value));
				return;
			}
			
			if (key.equals("CopperLayer")) {
				// CopperLayer ��Ϊһ��Document List,�����Document������layerOrGroupRef�����ڲ㣩���Լ�
				copperLayerAdapter(qed,((List<Document>) value));
				return;
			}
			
			if (key.equals("DrillSequences")) {
				drillSequencesAdapter(qed,((List<Document>) value));
				return;
			}
			
			// ��ʣ���Ԫ��Ԫ�ؽ��б���
//			Document doc = null;
			List<Document> docList = new ArrayList<Document>();
			if (value instanceof Document) {
				qedAdapter((Document) value, qed);
			} else if (value instanceof List) {
				List<Document> docs = (List<Document>) value;
				docs.forEach((d) -> {
					Document re = qedAdapter(d,qed);
					docList.add(re);
				});
			}

		});
		return qed;
	}

	// ���ַ���ת����double��Ȼ����д�С�ȶԣ�������Сֵ���������һ����Ϊ���֣����Ǹ�Ϊ��ֵ�ķ���
	 private String getMin (String str1, String str2) {
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
	
	
	// �ж�ֵ�ܷ�ת����Ϊ����
	public static boolean isNumeric(String str) {
		// ��������ʽ����ƥ�����е����� ��������
		Pattern pattern = Pattern.compile("-?[0-9]+\\.?[0-9]*");
		String bigStr;
		try {
			bigStr = new BigDecimal(str).toString();
		} catch (Exception e) {
			return false;// �쳣 ˵�����������֡�
		}

		Matcher isNum = pattern.matcher(bigStr); // matcher��ȫƥ��
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}

}
