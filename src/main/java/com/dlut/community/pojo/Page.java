package com.dlut.community.pojo;

public class Page {
    //当前页数
    private int current = 1;
    //每页显示数量
    private int limit = 10;
    //数据总数（用于查询有多少页）
    private int rows;
    //查询路径（用于复用分页链接）
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if(current >= 1) {
            this.current = current;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if(limit >= 1 && limit <= 100) {
            this.limit = limit;
        }

    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if(rows >= 0) {
            this.rows = rows;
        }

    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
    /*
    * 获取当前页起始行
    * @return
    * */
    public int getOffset() {
        //current * limit - limit
        return (current - 1) * limit;
    }
    /*
    * 获取总页数
    * */
    public int getTotal() {
        if(rows % limit == 0) {
            return rows / limit;
        }else {
            return rows / limit + 1;
        }
    }
    /*
    * 获取起始页码
    * */
    public int getFrom() {
        int from = current - 2;
        return from >= 1 ? from : 1;
    }
    /*
    *  获取终止页码
    * */
    public int getTo() {
        int to = current + 2;
        int total = getTotal();
        return to <= total ? to : total;
    }

    @Override
    public String toString() {
        return "Page{" +
                "current=" + current +
                ", limit=" + limit +
                ", rows=" + rows +
                ", path='" + path + '\'' +
                '}';
    }
}
