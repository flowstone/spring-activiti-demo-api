package me.xueyao.base;

import com.github.pagehelper.Page;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    public PageResult(int pageNum, int pageSize, List<T> data) {
        this.result = (T) data;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.count = Long.valueOf(Integer.valueOf(data.size()));
    }
}
