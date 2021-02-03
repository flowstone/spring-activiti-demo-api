package me.xueyao.base;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

//    public PageResult(T data, Page page) {
//        this.result = data;
//        this.pageNum = page.getPageable().getPageNumber() + 1;
//        this.pageSize = page.getPageable().getPageSize();
//        this.count = page.getTotalElements();
//    }
}
