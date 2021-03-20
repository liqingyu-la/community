package com.lqy.community.entity;

/**
 * 分装分页相关的信息
 */
public class Page {
//    当前页码
    private int current = 1;
    //    显示上限
    private int limit = 10;
    //    数据总数
    private int rows;
    //    查询路径（用于服用分页链接），让页面上写的逻辑简洁一点
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if (current >= 1){
            this.current = current;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if (limit >=1 && limit <= 100){
            this.limit = limit;
        }

    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if (rows >= 0){
            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取起始行
     * @return
     */
    public int getOffset(){
        return (current - 1) * limit;
    }

    /**
     * 获取总页数
     * @return
     */
    public int getTotal(){
        if (rows % limit == 0){
            return rows/limit;
        }else {
            return rows/limit + 1;
        }
    }

//    显示页码条所需函数

    /**
     * 获取起始页码
     * @return
     */
    public int getFrom(){
        int from = current - 2;
        return from > 1 ? from : 1;
    }

    /**
     * 获取结束页码
     * @return
     */
    public int getTo(){
        int to = current + 2;
        int total = getTotal();
        return to < total ? to : total;
    }


}
