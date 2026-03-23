package com.abinbev.b2b.price.api.validators.v2;

import org.springframework.stereotype.Component;

import com.abinbev.b2b.price.api.exceptions.BadRequestException;
import com.abinbev.b2b.price.api.exceptions.IssueEnum;
import com.abinbev.b2b.price.api.rest.vo.v2.PaginationResponseVoV2;

@Component
public class PaginationValidatorV2 { 

	public void validatePaginationV2(final PaginationResponseVoV2 paginationResponseVoV2) {

		if (paginationResponseVoV2 != null) {
			if (paginationResponseVoV2.getSize() < 0) {
				throw BadRequestException.invalidPagination(IssueEnum.INVALID_PAGE_SIZE);
			}

			if (paginationResponseVoV2.getPage() < 0) {
				throw BadRequestException.invalidPagination(IssueEnum.INVALID_PAGE_NUMBER);
			}
		}
	}

}
