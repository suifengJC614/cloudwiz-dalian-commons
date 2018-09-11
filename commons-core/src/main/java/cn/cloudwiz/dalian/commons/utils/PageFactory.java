package cn.cloudwiz.dalian.commons.utils;

import org.springframework.data.domain.*;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PageFactory {

    public static final int MAX_PAGE_ZISE = 200;

    public static <T> Page<T> emptyPage() {
        return Page.empty();
    }

    public static <T> Page<T> createPage(List<T> content) {
        return new PageImpl<T>(content);
    }

    public static <T> Page<T> createPage(List<T> content, Pageable page, long total) {
        return new PageImpl<T>(content, page, total);
    }

    public static <T> Page<T> createPage(List<T> content, Pageable page, Supplier<Long> supplier) {
        if(page == null){
            return createPage(content);
        }else {
            return createPage(content, page, supplier.get());
        }
    }

    public static boolean needPrevious(Page<?> page, Pageable pageinfo) {
        return pageinfo != null && page != null && page.getContent().isEmpty() && pageinfo.hasPrevious();
    }

    public static Pageable createDefaultPageable() {
        return createPageable(0, 20);
    }

    public static Pageable createPageable(int page, int size) {
        return PageRequest.of(page, size);
    }

    public static Pageable cleanSort(Pageable page) {
        return createPageable(page.getPageNumber(), page.getPageSize());
    }

    public static Pageable addSort(Pageable page, Direction direction, String... columns) {
        return addSort(page, direction, true, columns);
    }

    public static Pageable addSort(Pageable page, Direction direction, boolean ignoreCase, String... columns) {
        List<Order> orderlist = new ArrayList<Order>(columns.length);
        for (String col : columns) {
            Order order = new Order(direction, col);
            if (ignoreCase) {
                order = order.ignoreCase();
            }
            orderlist.add(order);
        }
        Sort sort = Sort.by(orderlist);
        if (page.getSort() != null) {
            sort = page.getSort().and(sort);
        }
        return PageRequest.of(page.getPageNumber(), page.getPageSize(), sort);
    }

    public static Pageable addSort(Pageable page, Sort addsort) {
        Sort sort = page.getSort();
        if (sort == null) {
            sort = addsort;
        } else {
            sort.and(addsort);
        }
        return PageRequest.of(page.getPageNumber(), page.getPageSize(), sort);
    }

}
