package cn.cyrus.otm.service.impl;

import java.util.Stack;

public class Test {

	public static void main(String[] args) {


		String str = "select substr(DATA_MONTH, 5, 2) || '月' as xAxisName,";

		String[] placeName = str.split(" ");

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

			}
		}


	}

}
