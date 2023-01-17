package cn.cyrus.otm.service.impl;
import cn.cyrus.otm.service.CommitService;
import com.alibaba.druid.sql.SQLUtils;
import lombok.val;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


@Service
public class CommitServiceImpl implements CommitService {

	//
	@Override
	public String changeSql(String str) {
		str = str.replace("&gt;",">");
		str = str.replace("&lt;","<");
		String[] placeName = str.split(" ");
		String result = "";
		ArrayList<String> pageItem  = new ArrayList<>();
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
	for (int i = 0; i <placeName.length ; i++) {
		if (!"".equals(placeName[i]) ) {

			//todo 待修改
			if (placeName[i].contains("||")) {
				if (placeName[i].contains(",")){
					String head = StringUtils.substringBefore(placeName[i], ",");
					String tail = StringUtils.substringAfter(placeName[i], ",");
					head = "CONCAT(" + head + ")";
					placeName[i] = head +","+tail;
					placeName[i] = placeName[i].replace("||", ",");
				}else {
					placeName[i] = "CONCAT(" + placeName[i] + ")";
					placeName[i] = placeName[i].replace("||", ",");
				}
			}
			if (placeName[i].contains("yyyy-mm-dd")) {
				placeName[i] = placeName[i].replace("yyyy-mm-dd", "%Y-%m-%d");
			}
			if ((placeName[i].contains("to_date") && !placeName[i].contains("str_to_date")) || (placeName[i].contains("TO_DATE") && !placeName[i].contains("STR_TO_DATE")) ) {
				String trueText =   placeName[i].contains("to_date") ? "to_date" : "TO_DATE" ;
				placeName[i] = placeName[i].toLowerCase().replace(trueText, "STR_TO_DATE");
			}
			if (placeName[i].contains("to_char")|| placeName[i].contains("TO_CHAR") ) {
				String trueText =   placeName[i].contains("to_char") ? "to_char" : "TO_CHAR" ;
				if (placeName[i].contains("'q'")){
					placeName[i] = placeName[i].replace(trueText, "QUARTER");
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

			if (!"".equals(placeName[i])) {
				result += placeName[i] + " ";
			}

		}

	}




		//String index ;
		//String offset ;
		//for (int i = 0; i < placeName.length; i++) {
		//
		//		//todo 改进 判断两个数
		//		// 遇到 OFFSET 循环写入 遇到 ONLY 退出
		//		// 1 String数组 判断取两个数字 替换 （后面替换空串）  // 不考虑
		//		// 2 List ？
		//
		//	//ArrayList<String> stringArrayList = new ArrayList<>();
		//
		//
		//	if (placeName[i].contains("OFFSET") ||placeName[i].contains("offset")  ) {
		//		//stringArrayList.add(placeName[i+1]);
		//		if (StringUtils.isNumeric(placeName[i+1])){
		//			//stringArrayList.add(placeName[i+1]);
		//			index = placeName[i+1];
		//		}
		//			//String trueText =   placeName[i].contains("ROWS") ? "ROWS" : "rows" ;
		//			//if("ROWS".trueText){
		//			//
		//			//}
		//			//placeName[i] = placeName[i].replace(trueText, "LIMIT 1");
		//	}
		//	if (placeName[i].contains("NEXT") ||placeName[i].contains("next")  ) {
		//		//stringArrayList.add(placeName[i+1]);
		//		if (StringUtils.isNumeric(placeName[i+1])){
		//			//stringArrayList.add(placeName[i+1]);
		//			offset = placeName[i+1];
		//		}
		//			//String trueText =   placeName[i].contains("ROWS") ? "ROWS" : "rows" ;
		//			//if("ROWS".trueText){
		//			//
		//			//}
		//			//placeName[i] = placeName[i].replace(trueText, "LIMIT 1");
		//	}



		//}






		String re = SQLUtils.formatMySql(result);
		System.out.println(re);

		//for (String s : placeName) {
		//	if (s.contains("||")){
		//		s = "CONCAT("+s+")";
		//	}
		//	s=s.replace("||",",");
		//
		//	//strings.add(s);
		//	result += s;
		//
		//	System.out.println(s);
		//
		//
		//}
		return re;


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

try {
            File file = new File("src/main/resources/in/SubjDevMapper.xml");
            BufferedReader br = new BufferedReader(new FileReader(file));
            ArrayList<String> strings = new ArrayList<String>();
            String s;//读取的每一行数据
            //br.re
            while ((s=br.readLine()) != null){
                if (s.contains("a.item_num AS itemNum,")) {
                    s = s.replace("a.item_num AS itemNum,", "test");
                }
                strings.add(s);//将数据存入集合
            }
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            for (String string : strings) {
                bw.write(string);//一行一行写入数据
                bw.newLine();//换行
            }
            bw.close();
        }catch (Exception e){

        }



	}






}
