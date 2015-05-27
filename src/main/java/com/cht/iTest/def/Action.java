package com.cht.iTest.def;

/**
 * 
 * 定義元件操作行為
 * 
 * @author wen
 *
 */
public enum Action {

	/**
	 * 輸入
	 */
	input,
	/**
	 * 點擊
	 */
	click,
	/**
	 * 對話盒確認
	 */
	accept,
	/**
	 * 對話盒拒絕
	 */
	dismiss,
	/**
	 * 下拉式選單
	 */
	select,
	/**
	 * 聚焦
	 */
	focus,
	/**
	 * 取值置入行程變數
	 */
	get,
	/**
	 * 取代
	 */
	replace,
	/**
	 * 等待
	 */
	wait,
	/**
	 * 切換瀏覽器視窗
	 */
	switchpop,
	/**
	 * 跳轉至特定URL
	 */
	to_page,
	/**
	 * 取得視窗標題
	 */
	get_title,
	/**
	 * 移動當前操作元件為對話盒
	 */
	dialog,
	/**
	 * 
	 */
	iframe;

}