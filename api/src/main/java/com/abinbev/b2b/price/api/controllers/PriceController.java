package com.abinbev.b2b.price.api.controllers;

import static com.abinbev.b2b.price.api.helpers.ApiConstants.AUTHORIZATION_HEADER;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.abinbev.b2b.price.api.exceptions.Issue;
import com.abinbev.b2b.price.api.helpers.ApiConstants;
import com.abinbev.b2b.price.api.helpers.ApiResponseConstants;
import com.abinbev.b2b.price.api.rest.vo.Pagination;
import com.abinbev.b2b.price.api.rest.vo.PriceResponseVo;
import com.abinbev.b2b.price.api.services.PriceService;
import com.abinbev.b2b.price.api.validators.RequestValidatorHelper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = ApiConstants.API_NAME_PRICES)
@SecurityScheme(scheme = "bearer", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)
public class PriceController {

	private static final Logger LOGGER = LoggerFactory.getLogger(PriceController.class);
	private final PriceService priceService;
	private final RequestValidatorHelper requestValidatorHelper;

	@Autowired
	public PriceController(final PriceService priceService, final RequestValidatorHelper requestValidatorHelper) {

		this.priceService = priceService;
		this.requestValidatorHelper = requestValidatorHelper;
	}

	@Operation(summary = ApiConstants.API_PRICES_DESCRIPTION, responses = {
			@ApiResponse(responseCode = "200", description = ApiResponseConstants.STATUS_200_GET_OK, content = @Content(schema = @Schema(implementation = PriceResponseVo.class))),
			@ApiResponse(responseCode = "400", description = ApiResponseConstants.STATUS_400_BAD_REQUEST, content = @Content(schema = @Schema(implementation = Issue.class))),
			@ApiResponse(responseCode = "503", description = ApiResponseConstants.STATUS_503_SERVER_ERROR, content = @Content(schema = @Schema(implementation = Issue.class))),
			@ApiResponse(responseCode = "403", description = ApiResponseConstants.STATUS_403_FORBIDDEN, content = @Content(schema = @Schema(implementation = Issue.class))),
			@ApiResponse(responseCode = "404", description = ApiResponseConstants.STATUS_404_NOT_FOUND, content = @Content(schema = @Schema(implementation = Issue.class))) })
			@Parameter(description = ApiConstants.REQUEST_TRACE_ID_DESCRIPTION, required = true, name = ApiConstants.REQUEST_TRACE_ID_HEADER, in = ParameterIn.HEADER)
	@GetMapping(path = "/v1", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public ResponseEntity<PriceResponseVo> get(
			@Parameter(name = ApiConstants.ACCOUNT_ID_PARAM, description = ApiConstants.ACCOUNT_DESCRIPTION, required = true) @RequestParam(name = "accountId") final String accountId,
			@Parameter(name = ApiConstants.COUNTRY_HEADER, description = ApiConstants.COUNTRY_DESCRIPTION, required = true) @RequestHeader final String country,
			@Parameter(name = ApiConstants.REQUEST_TRACE_ID_HEADER, description = ApiConstants.REQUEST_TRACE_ID_DESCRIPTION, required = true) @RequestHeader(ApiConstants.REQUEST_TRACE_ID_HEADER) final String requestTraceId,
			@Parameter(name = AUTHORIZATION_HEADER, description = ApiConstants.AUTHORIZATION_DESCRIPTION, hidden = true) @RequestHeader(name = ApiConstants.AUTHORIZATION_HEADER, required = false) final String authorization,
			@Parameter(name = ApiConstants.SKUS_FILTER_HEADER, description = ApiConstants.SKUS_FILTER_DESCRIPTION) @RequestParam(name = "skus", required = false) final List<String> skus,
			@Parameter(name = ApiConstants.PAGE_HEADER, description = ApiConstants.PAGE_DESCRIPTION) @RequestParam(name = "page", required = false) final Integer page,
			@Parameter(name = ApiConstants.SIZE_HEADER, description = ApiConstants.SIZE_DESCRIPTION) @RequestParam(name = "size", required = false) final Integer size) {

		final List<String> headersToValidate = Arrays.asList(ApiConstants.COUNTRY_HEADER, ApiConstants.REQUEST_TRACE_ID_HEADER,
				ApiConstants.AUTHORIZATION_HEADER);

		requestValidatorHelper.validateHeaders(headersToValidate, accountId, country, authorization);

		LOGGER.debug("Received GET request for requestTraceId: {}", requestTraceId);
		return ResponseEntity.ok(priceService.getAllPrices(accountId, country, skus, getPagination(page, size)));
	}

	private Pagination getPagination(final Integer page, final Integer size) {

		Pagination pagination = null;

		if (page != null || size != null) {
			pagination = new Pagination(page, size);
		}

		return pagination;
	}
}
