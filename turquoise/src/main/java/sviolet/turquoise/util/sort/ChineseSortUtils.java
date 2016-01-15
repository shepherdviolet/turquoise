/*
 * Copyright (C) 2015 S.Violet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Project GitHub: https://github.com/shepherdviolet/turquoise
 * Email: shepherdviolet@163.com
 */

package sviolet.turquoise.util.sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


/**
 * 
 * 中文排序工具
 * 
 * @author S.Violet (ZhuQinChao)
 *
 */
public class ChineseSortUtils {
	
	/**
	 * array排序
	 * 
	 * @param list
	 * @return
	 */
	public static String[] sort(String[] list){
		Comparator<Object> comparator = java.text.Collator.getInstance(java.util.Locale.CHINA);
		Arrays.sort(list,comparator);
		return list;
	}
	
	/**
	 * List<String>排序
	 * 
	 * @param list
	 * @return
	 */
	public static List<String> sort(List<String> list){
		//List<String>转为String[]
		String[] array = new String[list.size()];
		list.toArray(array);
		
		//排序
		array = sort(array);
		
		//String[]转为List<String>
		List<String> result = new ArrayList<String>();
		for(int i = 0 ; i < array.length ; i++){
			result.add(array[i]);
		}
		return result;
	}
	
	/**
	 * List<?> 根据对象的一个String变量(关键字)给对象排序
	 * 
	 		List<Item> list = ...
	  		list = ChineseSortUtils.keySort(list, new KeyGetter<Item>(){
				@Override
				//根据Item对象的getName()方法得到关键字
				public String getKey(Item obj) {
					return obj.getName();
				}
			});
	 * 
	 * @param list
	 * @param keyGetter
	 * @return
	 */
	public static <T> List<T> keySort(List<T> list, KeyGetter<T> keyGetter){
		//取得对象们的关键字
		String[] keyArray = new String[list.size()];
		for(int i = 0 ; i < list.size() ; i++){
			keyArray[i] = keyGetter.getKey(list.get(i));
		}
		
		//关键字排序
		keyArray = sort(keyArray);
		
		//根据关键字顺序对对象进行排序
		List<T> result = new ArrayList<T>();
		for(int i = 0 ; i < keyArray.length ; i++){
			for(int j = 0 ; j < list.size() ; j++){
				if(keyGetter.getKey(list.get(j)).equals(keyArray[i])){
					result.add(list.get(j));
					list.remove(j);
					break;
				}
			}
		}
		return result;
	}
}
