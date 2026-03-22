package com.abinbev.b2b.price.api.rest.vo.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "PaginationResponseVoV2")
public class PaginationResponseVoV2 {

	@JsonProperty
	@Schema(description = "Current page number. The first page is zero", example = "4")
	private Integer page;

	@JsonProperty
	@Schema(description = "Size of each page", example = "20")
	private Integer size;

	@JsonProperty
	@Schema(description = "Total number of pages", example = "5")
	private Integer totalPages;

	@JsonProperty
	@Schema(description = "Total number of elements", example = "100")
	private long totalElements;

	public PaginationResponseVoV2() {

	}

	public PaginationResponseVoV2(final Integer page, final Integer size) {

		this.page = page == null ? 0 : page;
		this.size = size == null ? 50 : size;
	}

	public PaginationResponseVoV2(final Integer page, final Integer size, final int totalElements) {

		this.page = page;
		this.size = size;
		setTotalElements(totalElements);
	}

	private boolean isPaginationRequested() {

		return page != null && size != null;
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

		final PaginationResponseVoV2 that = (PaginationResponseVoV2) o;

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
