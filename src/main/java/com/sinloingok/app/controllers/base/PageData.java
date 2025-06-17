package com.sinloingok.app.controllers.base;

import lombok.Data;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Objects;

/**
 * 自定義分頁查詢組件，包含分頁信息和請求體
 *
 * @param <T> 請求體類型
 */
@Data
public class PageData<T>{
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;
    private int pageNo;
    private int pageSize;
    private T body;

    public PageData(){
        this(DEFAULT_PAGE, DEFAULT_SIZE);
    }

    /**
     * 帶分頁和排序參數的構造函數
     *
     * @param pageNo 頁碼（從0開始）
     * @param pageSize 每頁大小
     */
    public PageData(int pageNo, int pageSize) {
        this(pageNo, pageSize, null);
    }

    /**
     * 完整構造函數
     *
     * @param pageNo 頁碼（從0開始）
     * @param pageSize 每頁大小
     * @param body 請求體
     */
    public PageData(int pageNo, int pageSize, T body) {
        if (pageNo < 0) {
            throw new IllegalArgumentException("Page index must not be less than zero");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("Page pageSize must not be less than one");
        }

        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.body = body;
    }

    public int getPageSize() {
        return pageSize;
    }

    /**
     * 獲取頁碼（從1開始）
     */
    public int getPageNo() {
        return pageNo;
    }

    /**
     * 設置頁碼（從1開始）
     */
    public void setPageNo(int pageNo) {
        if (pageNo < 1) {
            throw new IllegalArgumentException("Page index must not be less than zero");
        }
        this.pageNo = pageNo;
    }

    /**
     * 設置每頁大小
     */
    public void setPageSize(int pageSize) {
        if (pageSize < 1) {
            throw new IllegalArgumentException("Page pageSize must not be less than one");
        }
        this.pageSize = pageSize;
    }

    /**
     * 獲取請求體
     */
    public T getBody() {
        return body;
    }

    /**
     * 設置請求體
     */
    public void setBody(T body) {
        this.body = body;
    }

    public long getOffset() {
        return (long) (pageNo - 1) * (long) pageSize;
    }

    public boolean hasPrevious() {
        return pageNo > 0;
    }

    @NonNull
    public PageData previousOrFirst() {
        return hasPrevious() ? previous() : first();
    }

    @NonNull
    public PageData next() {
        return new PageData<>(pageNo + 1, pageSize, body);
    }

    /**
     * 獲取上一頁
     */
    @NonNull
    public PageData<T> previous() {
        return pageNo == 0 ? this : new PageData<>(pageNo - 1, pageSize, body);
    }

    @NonNull
    public PageData<T> first() {
        return new PageData<>(0, pageSize, body);
    }

    @NonNull
    public PageData<T> withPage(int pageNumber) {
        if (pageNumber < 0) {
            throw new IllegalArgumentException("Page index must not be less than zero");
        }
        return new PageData<>(pageNumber, pageSize, body);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PageData)) {
            return false;
        }
        PageData<?> that = (PageData<?>) obj;
        return this.pageNo == that.pageNo &&
                this.pageSize == that.pageSize &&
                Objects.equals(this.body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageNo, pageSize, body);
    }

    @Override
    public String toString() {
        return String.format("PageData{pageNo=%d, pageSize=%d, sort=%s, body=%s}",
                pageNo, pageSize, body);
    }
}