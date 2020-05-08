package com.bizvpm.dps.processor.pmsetl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;

public class Formatter {

	private static char[] array = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

	public static final String DATE_FORMAT_JS_FULL = "yyyy-MM-dd'T'HH:mm:ss.SSS Z";

	public static final String DATE_FORMAT_DATE = "yyyy-MM-dd";

	public static final String DATE_FORMAT_TIME = "HH:mm";

	public static final String DATE_FORMAT_DATETIME = "yyyy-MM-dd HH:mm:ss";

	public static final String MONEY_NUMBER_FORMAT = "#,##0.0";

	/**
	 * ʮ����ת�����������
	 * 
	 * @param number
	 * @param N
	 * @return
	 */
	public static String dec_n(long number, int N) {
		Long rest = number;
		Stack<Character> stack = new Stack<Character>();
		StringBuilder result = new StringBuilder(0);
		while (rest != 0) {
			stack.add(array[new Long((rest % N)).intValue()]);
			rest = rest / N;
		}
		for (; !stack.isEmpty();) {
			result.append(stack.pop());
		}
		return result.length() == 0 ? "0" : result.toString();
	}

	public static int n_dec(String hexValue, int N) {
		if (null == hexValue || "".equals(hexValue.trim()))
			return 0;

		Map<String, Integer> digthMap = new HashMap<String, Integer>();
		int count = 0;
		for (char item : array) {
			digthMap.put("" + item, count);
			count++;
		}
		String str = new StringBuffer(hexValue.trim()).reverse().toString();
		int sum = 0;
		for (int index = 0; index < str.length(); index++) {
			sum = new Double(Math.pow(N, index)).intValue() * digthMap.get("" + str.charAt(index)) + sum;
		}
		return sum;
	}

	public static String bytes2HexString(byte[] b) {
		String ret = "";
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = "0" + hex;
			}
			ret += hex;
		}
		return ret;
	}

	/**
	 * JS �����ַ���ת������
	 * 
	 * @param str
	 * @return
	 */
	public static Date getDatefromJS(String str) {
		if (str == null)
			return null;
		String _str = str.replace("Z", " UTC");
		SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_JS_FULL);
		try {
			return format.parse(_str);
		} catch (ParseException e) {
			return null;
		}
	}

	/**
	 * �ַ���תint
	 * 
	 * @param text
	 * @param errMsg
	 *            ����ת��ʱ����ʾ
	 * @return
	 */
	public static int getInt(String text, String errMsg) {
		if (Check.isNotAssigned(text)) {
			return 0;
		}
		try {
			return Integer.parseInt(text.trim());
		} catch (Exception e) {
			throw new RuntimeException(errMsg);
		}
	}

	public static int getInt(String text) {
		return getInt(text, "���ǺϷ���ֵ");
	}

	public static int getIntValue(Object value) {
		if (value == null)
			return 0;
		if (value instanceof String) {
			return getInt((String) value);
		}
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		throw new RuntimeException(value + " ���ǺϷ���int����");
	}

	public static double getDoubleValue(Object value) {
		if (value == null)
			return 0;
		if (value instanceof String) {
			return getDouble((String) value);
		}
		if (value instanceof Number) {
			return ((Number) value).doubleValue();
		}
		throw new RuntimeException(value + " ���ǺϷ���double����");
	}

	/**
	 * �ַ���תdouble
	 * 
	 * @param text
	 * @param errMsg
	 *            ����ת��ʱ����ʾ
	 * @return
	 */
	public static double getDouble(String text, String errMsg) {
		if (Check.isNotAssigned(text)) {
			return 0d;
		}
		try {
			return Double.parseDouble(text.trim());
		} catch (Exception e) {
			throw new RuntimeException(errMsg);
		}
	}

	public static double getDouble(String text) {
		return getDouble(text, "���ǺϷ���ֵ");
	}

	public static String getString(Object value) {
		return getString(value, null, null);
	}

	public static String getPercentageFormatString(Object value) {
		return getString(value, "#0.0%", null);
	}

	public static String getMoneyFormatString(Double budget) {
		if (budget == null || budget == 0d) {
			return "";
		}
		return Formatter.getString(budget, MONEY_NUMBER_FORMAT);
	}

	public static String getString(Object value, String format) {
		return getString(value, format, null);
	}

	public static String getString(Object value, String format, Locale locale) {
		return getString(value, format, "", locale);
	}

	public static String getString(Object value, String format, String defaultValue, Locale locale) {
		String text;
		if (value instanceof Date) {
			String sdf = Check.isNotAssigned(format) ? DATE_FORMAT_DATE : format;
			return Optional.ofNullable(locale).map(l -> new SimpleDateFormat(sdf, l)).orElse(new SimpleDateFormat(sdf)).format(value);
		} else if (value instanceof Integer || value instanceof Long || value instanceof Short) {
			text = Optional.ofNullable(format)//
					.map(f -> {
						DecimalFormat df = new DecimalFormat(f);
						df.setRoundingMode(RoundingMode.HALF_UP);
						return df.format(value);
					}).orElse(value.toString());
		} else if (value instanceof Float || value instanceof Double) {
			DecimalFormat df = new DecimalFormat(Optional.ofNullable(format).orElse("0.0"));
			df.setRoundingMode(RoundingMode.HALF_UP);
			return df.format(value);
		} else if (value instanceof Boolean) {
			text = (boolean) value ? "��" : "��";
		} else if (value instanceof String) {
			text = (String) value;
		} else if (value instanceof List<?>) {
			text = "";
			for (int i = 0; i < ((List<?>) value).size(); i++) {
				if (i != 0) {
					text += ", ";
				}
				text += getString(((List<?>) value).get(i), format, locale);
			}
		} else {
			text = defaultValue;
		}

		return text;
	}

	public static <T, R> List<R> getList(List<T> source, Function<T, R> func) {
		ArrayList<R> result = new ArrayList<R>();
		source.forEach(item -> result.add(func.apply(item)));
		return result;
	}

	public static <T, R> List<R> getList(T[] source, Function<T, R> func) {
		return getList(Arrays.asList(source), func);
	}

	/**
	 * 
	 * @param <T>
	 * @param source
	 *            Ҫ�ָ������
	 * @param subSize
	 *            �ָ�Ŀ��С
	 * @return
	 *
	 */
	public static <T> List<List<T>> getSplitedList(List<T> source, int subSize) {
		List<List<T>> subAryList = new ArrayList<List<T>>();
		int count = subSize == 0 ? 0 : (source.size() % subSize == 0 ? source.size() / subSize : source.size() / subSize + 1);
		for (int i = 0; i < count; i++) {
			int index = i * subSize;
			List<T> list = new ArrayList<T>();
			int j = 0;
			while (j < subSize && index < source.size()) {
				list.add(source.get(index++));
				j++;
			}
			subAryList.add(list);
		}

		return subAryList;
	}

	// i, u, v��������ĸ, ����ǰ�����ĸ

	private static char[] alphatable = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',

			'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

	// private static char[] alphatable = { 'a', 'b', 'c', 'd', 'e', 'f', 'g',
	// 'h', 'i',
	//
	// 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
	// 'x', 'y', 'z' };

	// ��ʼ��
	private static int[] alphatable_code = { 45217, 45253, 45761, 46318, 46826, 47010, 47297, 47614, 47614, 48119, 49062, 49324, 49896,
			50371, 50614, 50622, 50906, 51387, 51446, 52218, 52218, 52218, 52698, 52980, 53689, 54481, 55289 };

	/**
	 * ����һ���������ֵ��ַ�������һ������ƴ������ĸ���ַ���
	 * 
	 * @param String
	 *            SourceStr ����һ�����ֵ��ַ���
	 */
	public static String getAlphaString(String src) {
		if (src == null) {
			return "";
		}
		String result = ""; //$NON-NLS-1$
		int i;
		try {
			for (i = 0; i < src.length(); i++) {
				result += char_alpha(src.charAt(i));
			}
		} catch (Exception e) {
			result = ""; //$NON-NLS-1$
		}
		return result;
	}

	/**
	 * ������,�����ַ�,�õ�������ĸ, Ӣ����ĸ���ض�Ӧ����ĸ �����Ǽ��庺�ַ��� '0'
	 * 
	 * @param char
	 *            ch ����ƴ������ĸ���ַ�
	 */
	private static char char_alpha(char ch) {

		if (ch >= 'a' && ch <= 'z')
			// return (char) (ch - 'a' + 'A');
			return ch;
		if (ch >= 'A' && ch <= 'Z')
			return ch;
		if (ch >= '0' && ch <= '9')
			return ch;

		int gb = getCodeValue(ch, "GB2312"); //$NON-NLS-1$
		if (gb < alphatable_code[0])
			return '0';

		int i;
		for (i = 0; i < 26; ++i) {
			if (alphaCodeMatch(i, gb))
				break;
		}

		if (i >= 26)
			return 'X';
		else
			return alphatable[i];
	}

	/**
	 * �ж��ַ��Ƿ���table�����е��ַ���ƥ��
	 * 
	 * @param i
	 *            table�����е�λ��
	 * @param gb
	 *            ���ı���
	 * @return
	 */
	private static boolean alphaCodeMatch(int i, int gb) {

		if (gb < alphatable_code[i])
			return false;

		int j = i + 1;

		// ��ĸZʹ����������ǩ
		while (j < 26 && (alphatable_code[j] == alphatable_code[i]))
			++j;

		if (j == 26)
			return gb <= alphatable_code[j];
		else
			return gb < alphatable_code[j];

	}

	/**
	 * ȡ�����ֵı���
	 * 
	 * @param char
	 *            ch ����ƴ������ĸ���ַ�
	 */
	private static int getCodeValue(char ch, String charsetName) {

		String str = new String();
		str += ch;
		try {
			byte[] bytes = str.getBytes(charsetName);
			if (bytes.length < 2)
				return 0;
			return (bytes[0] << 8 & 0xff00) + (bytes[1] & 0xff);
		} catch (Exception e) {
			return 0;
		}
	}

	public static String getFriendlyTimeDuration(long diff) {
		long day = diff / (24 * 60 * 60 * 1000);
		long hour = (diff / (60 * 60 * 1000) - day * 24);
		long min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
		long sec = (diff / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);

		String result = "";
		if (day != 0)
			result += day + "�� ";
		if (hour != 0)
			result += hour + "Сʱ ";
		if (min != 0)
			result += min + "���� ";
		if (sec != 0)
			result += sec + "��";
		return result;
	}

	public static String toHtml(String text) {
		StringBuffer out = new StringBuffer();
		for (int i = 0; text != null && i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '\'')
				out.append("&#039;");
			else if (c == '\"')
				out.append("&#034;");
			else if (c == '<')
				out.append("&lt;");
			else if (c == '>')
				out.append("&gt;");
			else if (c == '&')
				out.append("&amp;");
			else if (c == ' ')
				out.append("&nbsp;");
			else if (c == '\n')
				out.append("<br/>");
			else
				out.append(c);
		}
		return out.toString();
	}

	public static Date getStartOfDay(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	public static Date getEndOfDay(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		c.add(Calendar.DATE, 1);
		c.add(Calendar.MILLISECOND, -1);
		return c.getTime();
	}

	public static Date getStartOfMonth(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	public static Date getEndOfMonth(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		c.add(Calendar.MONTH, 1);
		c.add(Calendar.MILLISECOND, -1);
		return c.getTime();
	}

	public static Date getStartOfYear(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.MONTH, 0);
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	public static Date getEndOfYear(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.MONTH, 0);
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		c.add(Calendar.YEAR, 1);
		c.add(Calendar.MILLISECOND, -1);
		return c.getTime();
	}

	public static List<Double> toList(double... ds) {
		List<Double> result = new ArrayList<>();
		for (int i = 0; i < ds.length; i++) {
			result.add(ds[i]);
		}
		return result;
	}

	public static double[] toArray(List<Double> list) {
		double[] result = new double[list.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = Optional.ofNullable(list.get(i)).orElse(0d);
		}
		return result;
	}

	/**
	 * ɾ��Html��ǩ
	 * 
	 * @param input
	 * @return
	 */
	public static String removeHtmlTag(String input) {
		if (input == null)
			return "";

		try {

			// ����script��������ʽ{��<script[^>]*?>[\\s\\S]*?<\\/script>
			String reg = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>";
			String result = Pattern.compile(reg, Pattern.CASE_INSENSITIVE).matcher(input).replaceAll(""); // ����script��ǩ

			// ����style��������ʽ{��<style[^>]*?>[\\s\\S]*?<\\/style>
			reg = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>";
			result = Pattern.compile(reg, Pattern.CASE_INSENSITIVE).matcher(result).replaceAll(""); // ����style��ǩ

			// ����HTML��ǩ��������ʽ
			reg = "<[^>]+>";
			result = Pattern.compile(reg, Pattern.CASE_INSENSITIVE).matcher(result).replaceAll(""); // ����html��ǩ

			// ����һЩ�����ַ���������ʽ �磺&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			reg = "\\&[a-zA-Z]{1,10};";
			result = Pattern.compile(reg, Pattern.CASE_INSENSITIVE).matcher(result).replaceAll(""); // ���������ǩ

			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String concat(String... str) {
		StringBuffer sb = new StringBuffer();
		if (str != null)
			Arrays.asList(str).forEach(s -> {
				if (s != null)
					sb.append(s);
			});
		return sb.toString();
	}

	public static String getStatusText(String status, String lang) {
		int idx = Arrays
				.asList("Created", "Ready", "Reserved", "InProgress", "Suspended", "Completed", "Failed", "Error", "Exited", "Obsolete")
				.indexOf(status);
		if (idx != -1) {
			String text = new String[] { "����", "��ǩ��", "��ǩ��", "ִ��", "��ͣ", "���", "ʧ��", "����", "�˳�", "����" }[idx];
			// return NLS.get(lang, text);//TODO NLS
			return text;
		} else {
			return null;
		}
	}

	public static String getFirstString(String... text) {
		for (int i = 0; i < text.length; i++) {
			if (text[i] != null && !text[i].isEmpty())
				return text[i];
		}
		return null;
	}

	public static int getMaxCommonDivisor(int a, int b) {
		// ����һ������վֵ
		int temp = 0;
		while (a % b != 0) {
			temp = a % b;
			a = b;
			b = temp;
		}
		return b;
	}

	// ������������С����������������� ���� �������������Լ������С�������� ����
	public static int getMinCommonMultiple(int a, int b) {
		return a * b / getMaxCommonDivisor(a, b);
	}

	// ����������С������
	public static int getMinMultiCommonMultiple(int... arrays) {
		int val = arrays[0];
		// ʵ��ԭ����ǰ����������С��Լ���ͺ�һ�����Ƚϣ������ǵĹ�Լ���Դ����ơ�����
		for (int i = 1; i < arrays.length; i++) {
			val = getMinCommonMultiple(val, arrays[i]);
		}
		return val;
	}

	public static String getDateWeek(Calendar cal) {
		switch (cal.get(Calendar.DAY_OF_WEEK)) {
		case 1:
			return "����";
		case 2:
			return "��һ";
		case 3:
			return "�ܶ�";
		case 4:
			return "����";
		case 5:
			return "����";
		case 6:
			return "����";
		default:
			return "����";
		}
	}

	public static String getLimitLengthString2(String source, int length) {
		String target;
		if (source.getBytes().length > length) {
			target = source.substring(0, (length - 3) / 2) + "..."; //$NON-NLS-1$
		} else {
			target = source;
		}

		return target;
	}

	public static List<String> listParameter(String js) {
		List<String> result = new ArrayList<>();
		String regEx = "\"<.+?>\"";
		Pattern pattern = Pattern.compile(regEx);
		Matcher m = pattern.matcher(js);
		while (m.find()) {
			String group = m.group();
			result.add(group.substring(2, group.length() - 2));
		}
		regEx = "'<.+?>'";
		pattern = Pattern.compile(regEx);
		m = pattern.matcher(js);
		while (m.find()) {
			String group = m.group();
			result.add(group.substring(2, group.length() - 2));
		}
		return result;
	}

	public static String getString(InputStream in) throws IOException, UnsupportedEncodingException {
		return getString(in, "utf-8");
	}

	public static String getString(InputStream in, String charsetName) throws IOException, UnsupportedEncodingException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		// ��ȡ����
		byte[] buffer = new byte[2048];
		int length = 0;
		while ((length = in.read(buffer)) != -1) {
			bos.write(buffer, 0, length);// д�������
		}
		in.close();// ��ȡ��ϣ��ر�������

		// ��������������ַ�������
		String result = new String(bos.toByteArray(), charsetName);
		return result;
	}

	/**
	 * @return ����һ��Ϊ0��ObjectId, ������PathParam���ݣ����null������˽��ܺ�ɴ���null
	 */
	public static ObjectId ZeroObjectId() {
		return ObjectId.createFromLegacyFormat(0, 0, 0);
	}

	public static String encodeNameForURL(String str) {
		char[] utfBytes = str.toCharArray();
		String encode = "";
		for (int i = 0; i < utfBytes.length; i++) {
			if (i != 0)
				encode += "-";
			encode = encode + Formatter.dec_n(utfBytes[i], 60);
		}
		return encode;
	}

	public static String decodeNameForURL(String str) {
		String[] _decode = str.split("-");
		char[] result = new char[_decode.length];
		for (int i = 0; i < _decode.length; i++) {
			result[i] = (char) Formatter.n_dec(_decode[i], 60);
		}
		return String.valueOf(result);
	}

	public static String html2plainText(String inputString) {
		String htmlStr = inputString; // ��html��ǩ���ַ���
		String textStr = "";
		java.util.regex.Pattern p_script;
		java.util.regex.Matcher m_script;
		java.util.regex.Pattern p_style;
		java.util.regex.Matcher m_style;
		java.util.regex.Pattern p_html;
		java.util.regex.Matcher m_html;
		try {
			String regEx_script = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>"; // ����script��������ʽ{��<script[^>]*?>[\\s\\S]*?<\\/script>
			String regEx_style = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>"; // ����style��������ʽ{��<style[^>]*?>[\\s\\S]*?<\\/style>
			String regEx_html = "<[^>]+>"; // ����HTML��ǩ��������ʽ
			p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
			m_script = p_script.matcher(htmlStr);
			htmlStr = m_script.replaceAll(""); // ����script��ǩ
			p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
			m_style = p_style.matcher(htmlStr);
			htmlStr = m_style.replaceAll(""); // ����style��ǩ
			p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
			m_html = p_html.matcher(htmlStr);
			htmlStr = m_html.replaceAll(""); // ����html��ǩ
			textStr = htmlStr;
		} catch (Exception e) {
			System.err.println("Html2Text: " + e.getMessage());
		}
		// �޳��ո���
		textStr = textStr.replaceAll("[ ]+", " ");
		textStr = textStr.replaceAll("(?m)^\\s*$(\\n|\\r\\n)", "");
		return textStr;// �����ı��ַ���
	}

	/**
	 * ��ʾͨ���ĺ���
	 */
	public static final int CHOICE_YES = 1;

	/**
	 * ��ʾ����ĺ���
	 */
	public static final int CHOICE_NO = 2;

	/**
	 * �޷�ȷ���京���choice
	 */
	public static final int CHOICE_NA = 4;

	/**
	 * ��ʾ���ĵĺ���
	 */
	public static final int CHOICE_REWORK = 3;

	/**
	 * û��choice
	 */
	public static final int CHOICE_NULL = 0;

	public static int getChoiceCode(Object choice) {
		if (choice == null)
			return CHOICE_NULL;

		if (choice instanceof Integer) {
			if ((int) choice < 0)
				return CHOICE_NULL;
			if ((int) choice > 4)
				return CHOICE_NA;
			return (int) choice;
		}

		if (Arrays.asList("ͨ��", "ͬ��", "��׼", "ȷ��", "���", "����", "����", "��", "����").contains(choice))
			return CHOICE_YES;

		if (Arrays.asList("����", "�ò�����", "����", "����").contains(choice))
			return CHOICE_REWORK;

		if (Arrays.asList("��ͨ��", "����ͨ��", "������׼", "��������", "���", "��ͬ��", "ȡ��", "������", "����", "��", "��ֹ").contains(choice))
			return CHOICE_NO;

		return CHOICE_NA;
	}

	public static String toCSVStr(String str) {
		if (str == null)
			return "";
		return str.replaceAll(",", "��").replaceAll("\n", "\t");
	}

	public static String getHtmltext(String text) {
		if (text == null)
			return "";
		text = removeHtmlTag(text);
		Pattern p = Pattern.compile("(\r?\n(\\s*\r?\n)+)");
		Matcher m = p.matcher(text);
		text = m.replaceAll("\r\n");
		text = text.replaceAll("\\n", "<br>");
		return text;
	}

	public static double getDouble(Map<?, ?> data, String key) {
		if (key == null)
			throw new IllegalArgumentException("ȡֵ��������Ϊ��");
		if (data == null)
			return 0d;
		Object value = data.get(key);
		if (value instanceof Number) {
			return ((Number) value).doubleValue();
		}
		return 0d;
	}

	public static int getInt(Map<?, ?> data, String key) {
		if (key == null)
			throw new IllegalArgumentException("ȡֵ��������Ϊ��");
		if (data == null)
			return 0;
		Object value = data.get(key);
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		return 0;
	}

	public static long getLong(Map<?, ?> data, String key) {
		if (key == null)
			throw new IllegalArgumentException("ȡֵ��������Ϊ��");
		if (data == null)
			return 0;
		Object value = data.get(key);
		if (value instanceof Number)
			return ((Number) value).longValue();
		return 0;
	}

	public static List<?> getList(Map<?, ?> data, String key) {
		if (key == null)
			throw new IllegalArgumentException("ȡֵ��������Ϊ��");
		if (data == null)
			return new ArrayList<>();
		Object value = data.get(key);
		if (value instanceof List<?>)
			return (List<?>) value;
		return null;
	}

	@SuppressWarnings("unchecked")
	public static Document getDocument(Map<?, ?> data, String key) {
		if (key == null)
			throw new IllegalArgumentException("ȡֵ��������Ϊ��");
		if (data != null) {
			Object value = data.get(key);
			if (value instanceof Document) {
				return (Document) value;
			} else if (value instanceof Map<?, ?>) {
				Document result = new Document();
				result.putAll((Map<? extends String, ?>) value);
				return result;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static BasicDBObject getBasicDBObject(Map<?, ?> data, String key) {
		if (key == null)
			throw new IllegalArgumentException("ȡֵ��������Ϊ��");
		if (data != null) {
			Object value = data.get(key);
			if (value instanceof BasicDBObject) {
				return (BasicDBObject) value;
			} else if (value instanceof Map<?, ?>) {
				BasicDBObject result = new BasicDBObject();
				result.putAll((Map<? extends String, ?>) value);
				return result;
			}
		}
		return null;
	}

	public static Map<?, ?> getMap(Map<?, ?> data, String key) {
		if (key == null)
			throw new IllegalArgumentException("ȡֵ��������Ϊ��");
		if (data != null) {
			Object value = data.get(key);
			if (value instanceof Map<?, ?>) {
				return (Map<?, ?>) value;
			}
		}
		return null;
	}

	public static Object getDeepValue(Map<?, ?> data, String key, String splitBy) {
		if (key == null)
			throw new IllegalArgumentException("ȡֵ��������Ϊ��");
		String[] keys = key.split(splitBy);
		Map<?, ?> doc = data;
		for (int i = 0; i < keys.length; i++) {
			Object value = doc.get(keys[i]);
			if (i == keys.length - 1) {
				return value;
			} else if (value instanceof Map<?, ?>) {
				Object d = ((Map<?, ?>) value).get(keys[i]);
				if (d instanceof Map<?, ?>) {
					doc = (Map<?, ?>) d;
				} else {
					break;
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * ����������ã�ͨ����Document������json��ʽ��ת��properties��ʽ��key
	 * ����:"com/aaa/bbb"->"com.aaa.bbb"
	 * 
	 * @param setting
	 * @param splitRex
	 * @return
	 */
	public static Map<String, Object> getProperties(Map<String, ?> setting, String splitRex) {
		HashMap<String, Object> result = new HashMap<>();
		setting.entrySet().forEach(e -> {
			Object value = e.getValue();
			if (value != null) {
				String key = e.getKey().replaceAll(splitRex, ".");
				result.put(key, value);
			}
		});
		return result;
	}

	/**
	 * 
	 * @param e1
	 * @param e2
	 * @param emptyEqsNull
	 *            �ն��������� �Ƿ� ���� null
	 * @return
	 */
	public static boolean equals(Document e1, Document e2, boolean emptyEqsNull) {
		if (e1 == null && e2 == null)
			return true;
		if (e1 == null && e2 != null || e1 != null && e2 == null)
			return false;

		return e1.equals(e2);
	}

}
