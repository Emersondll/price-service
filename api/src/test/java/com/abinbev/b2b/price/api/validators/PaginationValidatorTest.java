package com.abinbev.b2b.price.api.validators;

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
import com.abinbev.b2b.price.api.rest.vo.Pagination;

@ExtendWith(MockitoExtension.class)
class PaginationValidatorTest {

	@InjectMocks
	private PaginationValidator paginationValidator;

	@Test
	void shouldValidatePaginationWhenHasValidParameters() {

		assertDoesNotThrow(() -> paginationValidator.validatePagination(new Pagination(1, 10)));
	}

	@Test
	void shouldNotValidateWhenHasInvalidPageSize() {

		final BadRequestException badRequestException = assertThrows(BadRequestException.class,
				() -> paginationValidator.validatePagination(new Pagination(1, -10)));

		assertThat(badRequestException.getIssue().getMessage(), equalTo(IssueEnum.INVALID_PAGE_SIZE.getFormattedMessage()));
	}

	@Test
	void shouldNotValidateWhenHasInvalidPageNumber() {

		final BadRequestException badRequestException = assertThrows(BadRequestException.class,
				() -> paginationValidator.validatePagination(new Pagination(-1, 10)));

		assertThat(badRequestException.getIssue().getMessage(), equalTo(IssueEnum.INVALID_PAGE_NUMBER.getFormattedMessage()));
	}

	@Test
	void shouldNotValidateWhenPaginationIsNull() {

		assertDoesNotThrow(() -> paginationValidator.validatePagination(null));
	}

}
