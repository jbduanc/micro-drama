package com.series.common.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 表格分页数据对象
 *
 * @author shouhan todo 放入common
 */
@Data
public class TablePageInfo<T> implements Serializable {

    private static final long serialVersionUID = 880505125821601378L;

    /**
     * 总记录数
     */
    private int total;

    /**
     * 列表数据
     */
    private List<T> data;

    /**
     * 消息状态码
     */
    private int code;

    /**
     * 消息内容
     */
    private String msg;

    /**
     * 表格数据对象
     */
    public TablePageInfo() {
    }

    /**
     * 分页
     *
     * @param list  列表数据
     * @param total 总记录数
     */
    public TablePageInfo(List<T> list, int total) {
        this.data = list;
        this.total = total;
    }
}
