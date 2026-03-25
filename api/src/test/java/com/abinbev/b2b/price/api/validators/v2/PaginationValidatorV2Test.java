package com.abinbev.b2b.price.api.validators.v2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abinbev.b2b.price.api.exceptions.BadRequestException;
import com.abinbev.b2b.price.api.exceptions.IssueEnum;
import com.abinbev.b2b.price.api.rest.vo.v2.PaginationResponseVoV2;

@ExtendWith(MockitoExtension.class)
class PaginationValidatorV2Test {

	@InjectMocks
	private PaginationValidatorV2 paginationValidatorV2;

	@Test
	void shouldValidatePaginationWhenHasValidParameters() {

		assertDoesNotThrow(() -> paginationValidatorV2.validatePaginationV2(new PaginationResponseVoV2(1, 10)));
	}

	@Test
	void shouldNotValidateWhenHasInvalidPageSize() {

		final BadRequestException badRequestException = assertThrows(BadRequestException.class,
				() -> paginationValidatorV2.validatePaginationV2(new PaginationResponseVoV2(1, -10)));

		assertThat(badRequestException.getIssue().getMessage(), equalTo(IssueEnum.INVALID_PAGE_SIZE.getFormattedMessage()));
	}

	@Test
	void shouldNotValidateWhenHasInvalidPageNumber() {

		final BadRequestException badRequestException = assertThrows(BadRequestException.class,
				() -> paginationValidatorV2.validatePaginationV2(new PaginationResponseVoV2(-1, 10)));

		assertThat(badRequestException.getIssue().getMessage(), equalTo(IssueEnum.INVALID_PAGE_NUMBER.getFormattedMessage()));
	}

	@Test
	void shouldValidateWhenPaginationIsNull() {

		assertDoesNotThrow(() -> paginationValidatorV2.validatePaginationV2(null));
	}

}
