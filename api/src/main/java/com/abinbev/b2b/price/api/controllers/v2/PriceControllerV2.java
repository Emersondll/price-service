package com.abinbev.b2b.price.api.controllers.v2;

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

import com.abinbev.b2b.price.api.converters.v2.PriceResultListV2ToPriceResponseVoV2Converter;
import com.abinbev.b2b.price.api.domain.v2.PriceResultListV2;
import com.abinbev.b2b.price.api.exceptions.Issue;
import com.abinbev.b2b.price.api.helpers.ApiConstants;
import com.abinbev.b2b.price.api.helpers.ApiResponseConstants;
import com.abinbev.b2b.price.api.rest.vo.v2.PaginationResponseVoV2;
import com.abinbev.b2b.price.api.rest.vo.v2.PriceResponseVoV2;
import com.abinbev.b2b.price.api.services.v2.PriceServiceV2;
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
public class PriceControllerV2 {

	private static final Logger LOGGER = LoggerFactory.getLogger(PriceControllerV2.class);
	private final RequestValidatorHelper requestValidatorHelper;
	private final PriceServiceV2 priceServiceV2;
	private final PriceResultListV2ToPriceResponseVoV2Converter priceResultListV2ToPriceResponseVoV2Converter;

	@Autowired
	public PriceControllerV2(final RequestValidatorHelper requestValidatorHelper, final PriceServiceV2 priceServiceV2,
			final PriceResultListV2ToPriceResponseVoV2Converter priceResultListV2ToPriceResponseVoV2Converter) {

		this.requestValidatorHelper = requestValidatorHelper;
		this.priceServiceV2 = priceServiceV2;
		this.priceResultListV2ToPriceResponseVoV2Converter = priceResultListV2ToPriceResponseVoV2Converter;
	}

	@Operation(summary = ApiConstants.API_PRICES_DESCRIPTION, responses = {
			@ApiResponse(responseCode = "200", description = ApiResponseConstants.STATUS_200_GET_OK, content = @Content(schema = @Schema(implementation = PriceResponseVoV2.class))),
			@ApiResponse(responseCode = "400", description = ApiResponseConstants.STATUS_400_BAD_REQUEST, content = @Content(schema = @Schema(implementation = Issue.class))),
			@ApiResponse(responseCode = "503", description = ApiResponseConstants.STATUS_503_SERVER_ERROR, content = @Content(schema = @Schema(implementation = Issue.class))),
			@ApiResponse(responseCode = "403", description = ApiResponseConstants.STATUS_403_FORBIDDEN, content = @Content(schema = @Schema(implementation = Issue.class))),
			@ApiResponse(responseCode = "404", description = ApiResponseConstants.STATUS_404_NOT_FOUND, content = @Content(schema = @Schema(implementation = Issue.class))) })
			@Parameter(description = ApiConstants.REQUEST_TRACE_ID_DESCRIPTION, required = true, name = ApiConstants.REQUEST_TRACE_ID_HEADER, in = ParameterIn.HEADER)
	@GetMapping(path = "/v2", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<PriceResponseVoV2> get(
			@Parameter(name = ApiConstants.COUNTRY_HEADER, description = ApiConstants.COUNTRY_DESCRIPTION, required = true) @RequestHeader(name = ApiConstants.COUNTRY_HEADER) final String country,
			@Parameter(name = ApiConstants.VENDOR_ID_HEADER, description = ApiConstants.VENDOR_ID_DESCRIPTION, required = true) @RequestHeader(name = ApiConstants.VENDOR_ID_HEADER) final String vendorId,
			@Parameter(name = ApiConstants.VENDOR_ACCOUNT_ID_HEADER, description = ApiConstants.VENDOR_ACCOUNT_ID_DESCRIPTION, required = true) @RequestHeader(name = ApiConstants.VENDOR_ACCOUNT_ID_HEADER) final String vendorAccountId,
			@Parameter(name = ApiConstants.REQUEST_TRACE_ID_HEADER, description = ApiConstants.REQUEST_TRACE_ID_DESCRIPTION, required = true) @RequestHeader(ApiConstants.REQUEST_TRACE_ID_HEADER) final String requestTraceId,
			@Parameter(name = ApiConstants.AUTHORIZATION_HEADER, description = ApiConstants.AUTHORIZATION_DESCRIPTION, hidden = true) @RequestHeader(name = ApiConstants.AUTHORIZATION_HEADER, required = false) final String authorization,
			@Parameter(name = ApiConstants.PRICE_LIST_ID_HEADER, description = ApiConstants.PRICE_LIST_ID_DESCRIPTION) @RequestHeader(name = ApiConstants.PRICE_LIST_ID_HEADER, required = false) final String priceListId,
			@Parameter(name = ApiConstants.VENDOR_ITEM_IDS_HEADER, description = ApiConstants.VENDOR_ITEM_ID_DESCRIPTION) @RequestParam(name = ApiConstants.VENDOR_ITEM_IDS_HEADER, required = false) final List<String> vendorItemIds,
			@Parameter(name = ApiConstants.PAGE_HEADER, description = ApiConstants.PAGE_DESCRIPTION) @RequestParam(name = ApiConstants.PAGE_HEADER, required = false) final Integer page,
			@Parameter(name = ApiConstants.SIZE_HEADER, description = ApiConstants.SIZE_DESCRIPTION) @RequestParam(name = ApiConstants.SIZE_HEADER, required = false) final Integer size) {

		LOGGER.info("[PriceControllerV2] Starting finding prices");

		requestValidatorHelper.validateHeadersV2(vendorId, vendorAccountId, country, authorization);

		final PriceResultListV2 priceResultListV2 = priceServiceV2.getAllPrices(vendorId, vendorAccountId, country, vendorItemIds,
				priceListId, normalizePagination(page, size));

		LOGGER.info("[PriceControllerV2] Find prices finished");

		return ResponseEntity.ok(priceResultListV2ToPriceResponseVoV2Converter.convert(priceResultListV2));
	}

	private PaginationResponseVoV2 normalizePagination(final Integer page, final Integer size) {

		PaginationResponseVoV2 pagination = null;

		if (page != null || size != null) {
			pagination = new PaginationResponseVoV2(page, size);
		}

		return pagination;
	}

}
