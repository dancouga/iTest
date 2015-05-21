package com.cht.iTest.def;

/**
 * 
 * 測試節點屬性定義
 * 
 * @author wen
 *
 */
public enum StepDefine {

	/**
	 * 名稱
	 */
	name, 
	/**
	 * 是否忽略
	 */
	skip, 
	/**
	 * 操作行為
	 */
	action, 
	/**
	 * 是否同步
	 */
	sync, 
	/**
	 * 元件名稱
	 */
	element, 
	/**
	 * 找尋元件所用的方法
	 */
	findMethod, 
	/**
	 * 輸入值
	 */
	inputValue, 
	/**
	 * 是否截圖
	 */
	snapshot, 
	/**
	 * 對此一節點的描述
	 */
	description;

}
