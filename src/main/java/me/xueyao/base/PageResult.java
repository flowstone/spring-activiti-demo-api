package me.xueyao.base;

import com.github.pagehelper.Page;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

/**
 * @author Simon.Xue
 * @date 2020-03-03 20:26
 **/
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PageResult<T> {
    private Integer pageNum;
    private Integer pageSize;
    private Long count;
    private T result;

    public PageResult(Page page) {
        this.result = (T) page.getResult();
        this.pageNum = page.getPageNum();
        this.pageSize = page.getPageSize();
        this.count = page.getTotal();
    }

    public PageResult(Page page, List<T> data) {
        this.result = (T)data;
        this.pageNum = page.getPageNum();
        this.pageSize = page.getPageSize();
        this.count = page.getTotal();
    }

    public PageResult(int pageNum, int pageSize, List<T> data) {
        this.result = (T) data;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.count = Long.valueOf(Integer.valueOf(data.size()));
    }

    public PageResult(int pageNum, int pageSize, long count, List<T> data) {
        this.result = (T) data;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.count = count;
    }

    /**
     * 对list进行手动分页
     * @param pageNum
     * @param pageSize
     * @param list
     * @return
     */
    public PageResult customizePage(int pageNum, int pageSize, List<T> list) {
        int total = list.isEmpty() ? 0 : list.size();
        int startIndex = (pageNum - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, total);
        List<T> ts;


        if (startIndex > total || startIndex < 0) {
            ts = Collections.EMPTY_LIST;
        } else {
            ts = list.subList(startIndex, endIndex);
        }
        return new PageResult<>(pageNum, pageSize, total, ts);
    }
}
