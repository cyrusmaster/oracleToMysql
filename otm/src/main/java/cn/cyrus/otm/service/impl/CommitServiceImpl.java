package cn.cyrus.otm.service.impl;
import cn.cyrus.otm.service.CommitService;
import com.alibaba.druid.sql.SQLUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


@Service
@Slf4j
public class CommitServiceImpl implements CommitService {

	//
	@Override
	public  String changeSql(String str ,Integer type) {

		//数据清洗
		str = str.replace("&gt;",">");
		str = str.replace("&lt;","<");
		String[] placeName = str.split(" ");
		//inital
		String result = "";
		ArrayList<String> pageItem  = new ArrayList<>();
		int substrStart = 0;
		int substrEnd = 0 ;

		//去除 substr函数所有空格
		//1 遍历数组
		for (int i = 0; i <placeName.length ; i++) {
			//取索引
			if (placeName[i].contains("substr") || placeName[i].contains("SUBSTR")) {
				substrStart = i;
				//1 获取匹配字段
				String trueText = placeName[i].contains("substr")?"substr":"SUBSTR";
				//2 initial
				Stack<String> substrStack = new Stack<>();
				substrStack.push(trueText);
				int decodeBeginIndex = placeName[i].indexOf(trueText) + trueText.length();
				//int decodeEndIndex = decodeBeginIndex+1;
				//3 逻辑 (入 )出  获取i值
				// 判断左括号，取索引入栈，遍历该字串匹配右括号 匹配不到+i
				while (!substrStack.empty()){
					// substring取左    取不到会导致索引越界
					if (decodeBeginIndex == placeName[i].length()){
						i++;
						decodeBeginIndex = 0;
					}
					String substring = placeName[i].substring(decodeBeginIndex, decodeBeginIndex + 1);

					if ("(".equals(substring)){
						substrStack.push(substring);
						decodeBeginIndex++;
					} else if (")".equals(substring)) {
						substrStack.pop();
						if (trueText.equals(substrStack.peek())){
							substrStack.pop();
							for (int j = substrStart; j <= substrEnd; j++) {
								if (j>substrStart) {
									placeName[substrStart] += placeName[j];
									placeName[j] = "";
								}
							}
						}else {
							decodeBeginIndex++;
						}
					} else {
						decodeBeginIndex++;
					}
					substrEnd = i;
				}

				//数组删除空元素
				// 遍历删除函数后空元素 否则后续拼接空格出错
				for (int j = substrStart+1; j <= substrEnd; j++) {
					//System.out.println();
					placeName = removeElement(placeName,substrStart+1);
				}

			}else if (placeName[i].contains("decode") || placeName[i].contains("DECODE")) {
				substrStart = i;
				//1 获取匹配字段
				String trueText = placeName[i].contains("decode")?"decode":"DECODE";
				//2 initial
				Stack<String> substrStack = new Stack<>();
				substrStack.push(trueText);
				int decodeBeginIndex = placeName[i].indexOf(trueText) + trueText.length();
				//int decodeEndIndex = decodeBeginIndex+1;
				//3 逻辑 (入 )出  获取i值
				// 判断左括号，取索引入栈，遍历该字串匹配右括号 匹配不到+i
				while (!substrStack.empty()){
					// substring取左    取不到会导致索引越界
					if (decodeBeginIndex == placeName[i].length()){
						i++;
						decodeBeginIndex = 0;
					}
					String substring = placeName[i].substring(decodeBeginIndex, decodeBeginIndex + 1);

					if ("(".equals(substring)){
						substrStack.push(substring);
						decodeBeginIndex++;
					} else if (")".equals(substring)) {
						substrStack.pop();
						if (trueText.equals(substrStack.peek())){
							substrStack.pop();
							for (int j = substrStart; j <= substrEnd; j++) {
								if (j>substrStart) {
									placeName[substrStart] += placeName[j];
									placeName[j] = "";
								}
							}
						}else {
							decodeBeginIndex++;
						}
					} else {
						decodeBeginIndex++;
					}
					substrEnd = i;
				}

				//数组删除空元素
				// 遍历删除函数后空元素 否则后续拼接空格出错
				for (int j = substrStart+1; j <= substrEnd; j++) {
					//System.out.println();
					placeName = removeElement(placeName, substrStart+1);
				}

			}



		}



		// 遍历数组替换分页
		for (int i = 0; i <placeName.length ; i++) {
		if (placeName[i].contains("OFFSET") || placeName[i].contains("offset")) {
			//1 遍历拿ONLY索引
			int onlyIndex = 0;
			for (int j = i; j < placeName.length; j++) {
				if ("ONLY".equals(placeName[j]) || "only".equals(placeName[j])) {
					onlyIndex = j;
				}
			}
			for (int j = i; j <= onlyIndex; j++) {
				pageItem.add(placeName[j]);
			}
			//2 遍历写
			// 误区 需要遍历 placeName
			for (int x = i; x <= onlyIndex; x++) {

				if (placeName[x].equals(pageItem.get(0))) {
					placeName[x] = "LIMIT";
				} else if (placeName[x].equals(pageItem.get(1)) && StringUtils.isNumeric(pageItem.get(1))) {
					placeName[x] = pageItem.get(1);
				} else if (placeName[x].equals(pageItem.get(2)) && placeName[x+1].equals(pageItem.get(3))) {
					placeName[x] = ",";
				} else if (placeName[x].equals(pageItem.get(5)) && StringUtils.isNumeric(pageItem.get(5))) {
					if (StringUtils.isNumeric(pageItem.get(5))) {
						placeName[x] = pageItem.get(5);
					}
				} else {
					placeName[x] = "";
				}
			}
		}
	}
	//单块替换
	//todo  多部份连接
	for (int i = 0; i <placeName.length ; i++) {
		if (!"".equals(placeName[i]) ) {
			if ("||".equals(placeName[i])) {
				placeName[i] = "CONCAT(" + placeName[i-1] + "," + placeName[i+1] + ")";
				placeName = removeElement(placeName, i - 1);
				placeName = removeElement(placeName, i);
			}
			// '第'||to_char(to_date(DATA_MONTH,'yyyymm'),'q')||'季度'
			else if (placeName[i].contains("||")) {
				placeName[i] = replaceVerticalBar(placeName[i]);

			}


			// 日期替换
			if (placeName[i].contains("yyyy-mm-dd") || placeName[i].contains("YYYY-MM-DD")) {
				placeName[i] = placeName[i].replace(placeName[i].contains("yyyy-mm-dd") ? "yyyy-mm-dd" : "YYYY-MM-DD", "%Y-%m-%d");
			}

			if (placeName[i].contains("yyyymm")){
				placeName[i] = placeName[i].replace("yyyymm","%y%m");
			}
			else if (placeName[i].contains("yyyy") || placeName[i].contains("YYYY")) {
				placeName[i] = placeName[i].replace(placeName[i].contains("yyyy") ? "yyyy" : "YYYY", "%Y");
			}


			if ((placeName[i].contains("to_date") && !placeName[i].contains("str_to_date")) || (placeName[i].contains("TO_DATE") && !placeName[i].contains("STR_TO_DATE")) ) {
				String trueText =   placeName[i].contains("to_date") ? "to_date" : "TO_DATE" ;
				// 转小写
				//placeName[i] = placeName[i].toLowerCase().replace(trueText, "STR_TO_DATE");
				placeName[i] = placeName[i].replace(trueText, "STR_TO_DATE");

			}
			if (placeName[i].contains("to_char")|| placeName[i].contains("TO_CHAR") ) {
				String trueText =   placeName[i].contains("to_char") ? "to_char" : "TO_CHAR" ;
				if (placeName[i].contains("'q')")){
					placeName[i] = placeName[i].replace(trueText, "QUARTER");
					placeName[i] = placeName[i].replace(",'q'", "");
				}else {
					placeName[i] = placeName[i].replace(trueText, "DATE_FORMAT");
				}
			}
			if (placeName[i].contains("nvl") || placeName[i].contains("NVL") ) {
				String trueText =   placeName[i].contains("nvl") ? "nvl" : "NVL" ;
				placeName[i] = placeName[i].replace(trueText, "IFNULL");
			}
			if (placeName[i].contains("decode") || placeName[i].contains("DECODE") ) {
				String trueText =   placeName[i].contains("decode") ? "decode" : "DECODE" ;
				String decodeStr = getDecodeStr(placeName[i], trueText);
				String caseWhenStr = decodeToCaseWhen(decodeStr);
				//System.out.println(decodeToCaseWhen);
				placeName[i] = placeName[i].replace(decodeStr, caseWhenStr);

			}
		}
	}

	for (int i = 0; i <placeName.length ; i++) {
		//if (!"".equals(placeName[i])) {
			result += placeName[i] + " ";
		//}
	}


	if ( null == type || 0 == type ){
		System.out.println(result);
		return result;
	} else if (1 == type) {
		String re = SQLUtils.formatMySql(result);
		System.out.println(re);
		return re;
	}else {
		return "类型错误";
	}



	}


	/**
	 * REMARK  竖线替换
	 * @methodName   replaceVerticalBar
	 * @return java.lang.String
	 * @date 2023/2/7 16:09
	 * @author cyf
	 */
	public static String replaceVerticalBar(String str){

		//	1 匹配索引
		int i = str.indexOf("||");

		String result = "";
		ArrayList<String> strItem = new ArrayList<>();
		if (-1 != i){
			strItem.add(str.substring(0,i));
		}

		while (-1 != i){
			int j = i+2;
			i = str.indexOf("||",i+1);
			if (-1 != i) {
				strItem.add(str.substring(j, i));
			}else {
				strItem.add(StringUtils.substringAfterLast(str,"||"));
			}
		}
		for (String item : strItem){
			result += item + ",";
		}

		return "CONCAT(" +StringUtils.substringBeforeLast(result,",")+")";

	}


	/*
	 * REMARK   移除元素
	 * @methodName   removeElement
	 * @return java.lang.String[]
	 * @date 2023/2/1 17:24
	 * @author cyf
	 */
	public static String[] removeElement(String[] strings , int index){
		int length = strings.length;
		int i = 0;
		//String[] resultString = new String[0];
		while (i < length){
			if (i == index){
				for (int j = i; j < length -1 ; j++) {
					strings[j] = strings[j+1];
				}
				//i--;
				length = length - 1;
				String[] resultString = new String[length];
				for (int j = 0; j < length  ;j++) {
					resultString[j] = strings[j];

				}
				return resultString;

			}
			i++;

		}

		return new String[0];
	}


	/**
	 * REMARK  截取完整decode
	 * @methodName   getDecodeStr
	 * @return java.lang.String
	 * @date 2023/1/16 16:51
	 * @author cyf
	 */
	public static String getDecodeStr(String context,String decodeValue){
		//1 建栈
		val decodeStack = new Stack<String>();
		decodeStack.push(decodeValue);
		//2 确定索引
		int decodeBeginIndex = context.indexOf(decodeValue) + decodeValue.length();
		int decodeEndIndex = decodeBeginIndex+1;
		//3  遇（入栈，遇）出栈 遇栈底返回截取字符串
		while (!decodeStack.empty()){
			String substring = context.substring(decodeEndIndex - 1, decodeEndIndex);
			if ("(".contains(substring)){
				decodeStack.push(substring);
				decodeEndIndex++;
			} else if (")".contains(substring)) {
				// ? 为什么循环
				//while (!"(".equals(decodeStack.pop())){
				//}
				//
				decodeStack.pop();
				if (decodeValue.equals(decodeStack.peek())){
					decodeStack.pop();
				} else {
					decodeEndIndex++;
				}

			} else {
				decodeEndIndex++;
			}
		}
		return context.substring(decodeBeginIndex-decodeValue.length(),decodeEndIndex);
	}

	/*
	 * REMARK   对完整decode替换
	 * @methodName   decodeToCaseWhen
	 * @return java.lang.String
	 * @date 2023/1/17 10:48
	 * @author cyf
	 */
	public static String decodeToCaseWhen(String decodeText){
		//1  注索引从0开始   截取左含右不含
		int left = decodeText.indexOf("(");
		int right = decodeText.lastIndexOf(")");
		String substring = decodeText.substring(left+1, right);
		//2 拿到切割部分
		// 传入decode，切割部分(遇，且栈空写入Arraylist)
		ArrayList<String> itemList = new ArrayList<>();
		// decode切割完整性判断（）对称出入栈
		Stack<String> columnStack = new Stack<>();
		// 定义字符串变量、
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < substring.length(); i++) {
			String charStr = substring.substring(i, i + 1);
			if ("(".equals(charStr)){
				columnStack.push(charStr);
				stringBuffer.append(charStr);
			} else if (")".equals(charStr)) {
				columnStack.pop();
				stringBuffer.append(charStr);
			} else if (",".equals(charStr) && columnStack.empty()) {
				itemList.add(stringBuffer.toString());
				// 清空字符串变量 , 否则动态数组会变成递增写入
				stringBuffer = new StringBuffer();
			} else {
				stringBuffer.append(charStr);
			}
		}
		// 3 写入结尾部分
		if (StringUtils.isNotEmpty(stringBuffer.toString())) {
			itemList.add(stringBuffer.toString());
		}
		// 4健壮性 切分部分最少四个
		if (itemList.size()< 4){
			System.out.println("切分后动态数组小于3,不合符decode,语句:" + decodeText);
		}
		// 5 替换逻辑
		StringBuffer caseSB = new StringBuffer("CASE ");
		for (int i = 0; i < itemList.size() ; i++) {
			String item = itemList.get(i);
			if (i == 0){
				caseSB.append(item);
			} else if (i % 2 == 1) {
				if (i == itemList.size() - 1){
					caseSB.append(" ELSE ").append(item);
				}else {
					caseSB.append(" WHEN ").append(item);
				}
			} else if (i % 2 == 0) {
				caseSB.append(" THEN ").append(item);
			}

		}
		caseSB.append(" END ");


		return caseSB.toString();
	}




	//todo 行读取尝试 放弃格式化

	public static void main(String[] args) {


	
		
		// 1 单一文件
		File sourceFile = new File("src/main/resources/in/SubjDevMapper.xml");
		File sinkFile = new File("src/main/resources/out/SubjDevMapper.xml");
		//modifySingleFile(sourceFile,sinkFile);

		// 2 目录遍历 content root / absolute path
		File dir = new File("src/main/resources/mapper");
		modifyDirFile(dir);



	}

	
	/*
	 * REMARK  遍历目录  
	 * @methodName   modifyDirFile
	 * @return void
	 * @date 2023/3/6 11:22
	 * @author cyf
	 */
	private static void modifyDirFile(File dir){
		File[] files = dir.listFiles();
		try {
			for (File file : files){
				if (file.length() != 0){
					System.out.println("file.getName() = " + file.getName());
					modifySingleFile(file,file);
				}
			}

		}catch (Exception e) {
			e.printStackTrace();
			log.error("文件夹路径有误");
		}


	}


	/**
	 * REMARK  单一文件替换
	 * @methodName   modifySingleFile
	 * @return void
	 * @date 2023/3/6 10:39
	 * @author cyf
	 */
	public static void modifySingleFile(File source,File sink){
		try {
			BufferedReader br = new BufferedReader(new FileReader(source));
			ArrayList<String> strings = new ArrayList<String>();
			String s;//读取的每一行数据
			//br.re
			while ((s=br.readLine()) != null){
				CommitService commitService = new CommitServiceImpl();
				s = commitService.changeSql(s,0);
				strings.add(s);//将数据存入集合
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(sink));
			for (String string : strings) {
				bw.write(string);//一行一行写入数据
				bw.newLine();//换行
			}
			bw.close();
        }catch (Exception e){
			e.printStackTrace();
        }
	}









}
