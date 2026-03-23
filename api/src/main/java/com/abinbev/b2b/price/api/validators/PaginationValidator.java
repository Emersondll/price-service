package com.abinbev.b2b.price.api.validators;

import org.springframework.stereotype.Component;

import com.abinbev.b2b.price.api.exceptions.BadRequestException;
import com.abinbev.b2b.price.api.exceptions.IssueEnum;
import com.abinbev.b2b.price.api.rest.vo.Pagination;

@Component
public class PaginationValidator {

	public void validatePagination(final Pagination pagination) {

		if (pagination != null) {
			if (pagination.isInvalidPageSize()) {
				throw BadRequestException.invalidPagination(IssueEnum.INVALID_PAGE_SIZE);
			}

			if (pagination.isInvalidPageNumber()) {
				throw BadRequestException.invalidPagination(IssueEnum.INVALID_PAGE_NUMBER);
			}
		}
	}

}
