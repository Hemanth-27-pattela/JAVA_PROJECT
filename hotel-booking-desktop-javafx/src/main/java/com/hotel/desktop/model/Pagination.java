package com.hotel.desktop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Client-side model representing pagination metadata returned from Spring Boot.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pagination {
    private long total;
    private int page;
    private int pages;

    public Pagination() {
    }

    public Pagination(long total, int page, int pages) {
        this.total = total;
        this.page = page;
        this.pages = pages;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }
}

