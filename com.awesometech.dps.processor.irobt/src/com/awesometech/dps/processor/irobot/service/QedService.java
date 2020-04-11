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
 * QED处理类
 * 
 * @author Zhangjc
 *
 */
public class QedService {

	private String qedPath;

	private String workPath;

	private final static String PID = "%pid%";

	private final static String FILENAME = "%fileName%";

	// 根据传入的jobId进行解析QED
	// TODO 注意原始的QED报告也要返回回去，原始的报告是存放在我们的rfq里面
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
	// 业务对象解析
	
	// 业务对象一：T_SummaryParameter
	// XSD描述为
	/**
	 	<xsd:complexType name="T_SummaryParameter">
		    <xsd:simpleContent>
		      <xsd:extension base="xsd:string">
		        <xsd:attribute name="name" use="required"/>
		      </xsd:extension>
		    </xsd:simpleContent>
		  </xsd:complexType>
	 */
	// SummaryParameter  下为一个Document List，里面的Document均包含两个数据：name，value
	// 处理办法：将List中的每组数据的 name和value都作为QED中的一组key->value
	public void summaryParameterAdapter(Document qed,List<Document> summaryParameterList) {
		summaryParameterList.stream().forEach(c -> {
			// TODO 需要做名称的adapter，将其适配成我方QED的名称
			qed.append(AdapterTool.mappingName(c.getString("name")),
					AdapterTool.adapterSummary(c.getString("name"), c.getString("value")));
		});
	}
	
	// 业务对象一：T_CopperLayer
	// XSD描述为
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
		// 用以区分是否top层和bottom层
		List<String> layerOrGroupRef = Arrays.asList(new String[] { "top", "bot" }); // 类型为 string,无特殊处理方法
		// 用以区分每个属性的子属性
		List<String> minResultList = Arrays
				.asList(new String[] { "MinTrack", "MinTrackAllCopper", "MinTrackCriticalTrace", "MinTrackAllTrace",
						"MinGap", "MinSelfSpacing", "MinGapPP", "MinGapPT", "MinGapTT", "MinGapTraceTrace", "MinRing",
						"MinDistCopperToPlated", "MinDistCopperToNonPlated", "MinDistCopperToBackdrill",
						"MinDistCopperToOutline", "MinDistCopperToOutlinePads", "MinDistCopperToOutlineTracks",
						"MinDistCopperToOutlineRegions", "MinDistCopperToScoredOutline", "MinDistViaPlug" });  // 类型为T_MinResult
		List<String> minCopperAreaList = Arrays.asList(new String[] { "CopperArea" });  // 类型为T_CopperArea
		List<String> decStringList = Arrays.asList(new String[] { "EtchCompensation" }); // 类型为T_DecString

		AdapterTool atl = new AdapterTool();
		atl.setMinResultList(minResultList).setMinCopperAreaList(minCopperAreaList).setDecStringList(decStringList);
		
		Document minIn = new Document();
		Document minOut = new Document();
		// 这里的List是一个每层数据的列表
		copperLayerList.stream().forEach(c -> {
			Document cp = (Document) c;
			boolean isTopBot = layerOrGroupRef.contains(cp.getString("layerOrGroupRef"));
			cp.forEach((k, v) -> {
				// 数据处理，抓取每个参数的值
				String value = atl.getValue(k, v);
				// 数据处理，对数据进行分类，判断是out还是in，然后取出里面的值，和minIn与minOut里面的值进行比对,获取其中的最小值
				if (isTopBot) {
					minOut.append(k, getMin(minOut.getString(k),value));
				} else {
					minIn.append(k, getMin(minIn.getString(k),value));
				}
			});
		});
		// TODO 参数名称需要做适配
		// 将数据转换成 我们展示需要去的列表
		List copperList = minResultList.stream().map(s -> {
				Document doc = new Document();
				doc.append("type", s);
				doc.append("outer", minOut.getString(s));
				doc.append("inner", minIn.getString(s));
				return doc;
			}).collect(Collectors.toList());
		qed.append("ED_CopperLayers", copperList);
	}
	
	// 业务对象二：T_DrillSequences
	// XSD描述为
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
	// T_DrillSequences  下为一个Document List，里面的Document由id作为区分,下面存放的是T_DrillSequenceType
	// 处理办法：
	// TODO:
	@SuppressWarnings("unchecked")
	public void drillSequencesAdapter(Document qed, List<Document> sequencesList) {
		List<String> minResultList = Arrays.asList(new String[] { "MinRingOnOuter", "MinRingOnInner", "MinClrCopper",
				"MinClr", "MinClrInnerSpan", "MinClrOutline", "MinClrOutlineTracks" }); // 类型为T_MinResult
		List<String> decStringList = Arrays.asList(new String[] { "MinEndDia", "MaxEndDia", "MinToolDia", "MaxToolDia", }); // 类型为T_DecString
		List<String> basicStrList = Arrays.asList(new String[] { "Tools", "Holes", "OverlappingHoles" }); // 类型为integer,string这类基本对象
		List<String> spanList = Arrays.asList(new String[] { "Span" }); // 类型为T_Span
		List<String> drillSequenceTypeList = Arrays.asList(new String[] { "T_DrillSequenceType" }); // 类型为T_DrillSequenceType

//		String[] types = new String[] { "PTH", "NPTH", "blind", "buried", "backdrill" };

		AdapterTool atl = new AdapterTool();
		atl.setMinResultList(minResultList).setDecStringList(decStringList).setBasicStrList(basicStrList)
				.setSpanList(spanList).setDrillSequenceTypeList(drillSequenceTypeList);

		Map<String, Document> dataMap = new LinkedHashMap<String, Document>();   

		List<Document> drillDetails = new ArrayList<Document>();
		
		sequencesList.stream().map(d -> {
			// TODO 这里忽略了这一层的节点列表
			d.get("id"); // original 目前看的这一层就这一组值，我们暂时不管这层
			return d.get("DrillSequence"); // 这个才是我们需要的 他本身也是一个documentList
		}).filter(ds -> ds instanceof List).forEach(l -> { // 只处理 List<Document>,同时将null数据过滤掉,业务含义为List<T_DrillSequence>
			((List<Document>) l).stream().forEach(m -> {
				// TODO Span 的处理，看qed 29.xml内容 span貌似也在列头，也就是说和type是同级别的
				Document doc = new Document(m);
				Document drillLayer = new Document();
				String type = doc.getString("type");
				drillLayer.append("type", type);
				doc.remove("type"); // 这里是因为要将type提取出来作为列名,将其他属性key作为行名,拼装成为一个表格，所以将它remove掉
				doc.forEach((k, v) -> {
					// 获取实际值
					String data = atl.getValue(k, v);
					// TODO 需要做名称映射的转换
					drillLayer.append(k, data);
					if (null == dataMap.get(k)) {
						dataMap.put(k, new Document());
					}
					// 转换对应关系，将值转换成以k为行头的行数据，type 的值为key
					dataMap.get(k).put(type, data);
				});
				drillDetails.add(drillLayer);
			});
		});
		// 将key->Dcoument 转换成type为行头的行数据，并对每行数据进行total计算
		List<Document> list = new ArrayList<Document>();
		dataMap.forEach((k, v) -> {
			// 计算total值，对不同类型的字段属性计算方式是有差异的
			// TODO 具体计算规则需要ucamco的资料确认，现在按解析数据对比出的结果进行计算，可能不太准确
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
	// 因为QED报告每级对象都是以List形式存在，里面的NULL判断非常不好处理，所以这里采用方式为：
	// 对QED报告所有的元素进行遍历，抓出我们需要的节点，然后根据QED.XSD进行解析
	// 同时由于QED中的XML部分节点我们需要利用的值是以attribute形式存在，部分是以element的形式存在，还有部分是同时存在，这里无法达成完全的统一，所以下面解析中将针对每个对象进行单独的处理
	
	// 将I8QED适配成我们需要的QED
	@SuppressWarnings("unchecked")
	public Document qedAdapter(Document irobotQed,Document qed) {
		irobotQed.forEach((key, value) -> {
			// QED 的 Summary 部分处理
			if (key.equals("SummaryParameter")) {
				summaryParameterAdapter(qed,((List<Document>) value));
				return;
			}
			
			if (key.equals("CopperLayer")) {
				// CopperLayer 下为一个Document List,里面的Document均包含layerOrGroupRef（所在层），以及
				copperLayerAdapter(qed,((List<Document>) value));
				return;
			}
			
			if (key.equals("DrillSequences")) {
				drillSequencesAdapter(qed,((List<Document>) value));
				return;
			}
			
			// 对剩余的元素元素进行遍历
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

	// 将字符串转换成double，然后进行大小比对，返回最小值，如果其中一个不为数字，则将那个为数值的返回
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

}
