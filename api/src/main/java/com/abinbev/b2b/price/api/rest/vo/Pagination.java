package com.abinbev.b2b.price.api.rest.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "Pagination")
public class Pagination {

	@JsonProperty
	@Schema(example = "4", description = "Current page number. The first page is zero")
	private Integer page;

	@JsonProperty
	@Schema(example = "20", description = "Size of each page")
	private Integer size;

	@JsonProperty
	@Schema(example = "5", description = "Total number of pages")
	private Integer totalPages;

	@JsonProperty
	@Schema(example = "100", description = "Total number of elements")
	private long totalElements;

	public Pagination() {

	}

	public Pagination(final Integer page, final Integer size) {

		this.page = page == null ? 0 : page;
		this.size = size == null ? 50 : size;
	}

	public Pagination(final Integer page, final Integer size, final int totalElements) {

		this.page = page;
		this.size = size;
		setTotalElements(totalElements);
	}

	@JsonIgnore
	public boolean isPaginationRequested() {

		return page != null && size != null;
	}

	@JsonIgnore
	public boolean isInvalidPageSize() {

		return size < 0;
	}

	@JsonIgnore
	public boolean isInvalidPageNumber() {

		return page < 0;
	}

	public Integer getPage() {

		return page;
	}

	public void setPage(final Integer page) {

		this.page = page;
	}

	public Integer getSize() {

		return size;
	}

	public void setSize(final Integer size) {

		this.size = size;
	}

	public Integer getTotalPages() {

		return totalPages;
	}

	public void setTotalPages(final Integer totalPages) {

		this.totalPages = totalPages;
	}

	public long getTotalElements() {

		return totalElements;
	}

	public void setTotalElements(final int totalElements) {

		this.totalElements = totalElements;
		if (isPaginationRequested()) {
			// We don't test if the size is zero because it is a business
			// validation in the service.
			totalPages = (int) Math.ceil((double) totalElements / (double) size);
		}
	}

	@Override
	public int hashCode() {

		int result = page != null ? page.hashCode() : 0;
		result = 31 * result + (size != null ? size.hashCode() : 0);
		result = 31 * result + (totalPages != null ? totalPages.hashCode() : 0);
		result = 31 * result + (int) (totalElements ^ totalElements >>> 32);
		return result;
	}

	@Override
	public boolean equals(final Object o) {

		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final Pagination that = (Pagination) o;

		if (totalElements != that.totalElements) {
			return false;
		}
		if (page != null ? !page.equals(that.page) : that.page != null) {
			return false;
		}
		if (size != null ? !size.equals(that.size) : that.size != null) {
			return false;
		}
		return totalPages != null ? totalPages.equals(that.totalPages) : that.totalPages == null;
	}

	@Override
	public String toString() {

		return String.format("Pagination{page=%s, size=%s, totalPages=%s, totalElements=%s}", page, size, totalPages, totalElements);
	}
}

