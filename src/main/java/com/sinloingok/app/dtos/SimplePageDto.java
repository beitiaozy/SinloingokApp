package com.sinloingok.app.dtos;

import com.alibaba.fastjson.JSONObject;
import com.sinloingok.app.controllers.base.PageData;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 分页模型
 * 
 */
public class SimplePageDto<E> implements Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -1017013339612139770L;
	/** 当前第几页 */
	private long pageNumber = 1;
	/** 每页显示大小 */
	private long pageSize = 10;
	/** 页面显示数据 */
	private List<E> datas;
	/** 总页数 */
	private long totalPage;
	/** 总记录数 */
	private long totalRecord;

	/**
	 * 构造器
	 * 
	 * @param pageNumber
	 *            当前第几页
	 * @param pageSize
	 *            每页显示大小
	 * @param totalPage
	 *            总页数
	 * @param totalRecord
	 *            总记录数
	 * @param datas
	 *            当前页数据
	 */
	public SimplePageDto(long pageNumber, long pageSize, long totalRecord,
			List<E> datas) {
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
		this.totalRecord = totalRecord;
		this.datas = datas;
		this.totalPage = (totalRecord % pageSize == 0) ? (totalRecord / pageSize)
				: (totalRecord / pageSize + 1);
	}


	public SimplePageDto(PageData page, long totalRecord,
						 List<E> datas) {
		this.pageNumber = page.getPageNo() + 1;
		this.pageSize = page.getPageSize();
		this.totalRecord = totalRecord;
		this.datas = datas;
		this.totalPage = (totalRecord % pageSize == 0) ? (totalRecord / pageSize)
				: (totalRecord / pageSize + 1);
	}

	public long getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(long pageNumber) {
		this.pageNumber = pageNumber;
	}

	public long getPageSize() {
		return pageSize;
	}

	public void setPageSize(long pageSize) {
		this.pageSize = pageSize;
	}

        public List getDatas() {
                if(CollectionUtils.isNotEmpty(datas) && datas.get(0) instanceof Map){
                        List<JSONObject> jsonList = new ArrayList<>();
                        datas.forEach(data -> {
                                Map map = (Map) data;
                                JSONObject json = new JSONObject(map);
                                jsonList.add(json);
                        });
                        return jsonList;
                }
                return datas;
	}

	public List<E> listDatas(){
		return datas;
	}

	public void setDatas(List<E> datas) {
		this.datas = datas;
	}

	public long getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(long totalPage) {
		this.totalPage = totalPage;
	}

	public long getTotalRecord() {
		return totalRecord;
	}

	public void setTotalRecord(long totalRecord) {
		this.totalRecord = totalRecord;
	}

}
