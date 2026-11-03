package com.hotel.booking.dtos;

public class PaginationDto {
    private long total;
    private int page;
    private int pages;

    public PaginationDto() {
    }

    public PaginationDto(long total, int page, int pages) {
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

