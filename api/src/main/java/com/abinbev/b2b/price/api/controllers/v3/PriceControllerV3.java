package com.abinbev.b2b.price.api.controllers.v3;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.abinbev.b2b.price.api.converters.v3.PriceNormalizedInfoToPriceResponseVoV3Converter;
import com.abinbev.b2b.price.api.converters.v3.PriceRequestV3ToPriceNormalizedInfoConverter;
import com.abinbev.b2b.price.api.domain.v3.PriceNormalizedInfo;
import com.abinbev.b2b.price.api.exceptions.Issue;
import com.abinbev.b2b.price.api.helpers.ApiConstants;
import com.abinbev.b2b.price.api.helpers.ApiResponseConstants;
import com.abinbev.b2b.price.api.rest.vo.v3.PriceRequestV3;
import com.abinbev.b2b.price.api.rest.vo.v3.PriceResponseVoV3;
import com.abinbev.b2b.price.api.services.v3.SearchForPricesAndFillIntoPriceNormalizedInfoServiceV3;
import com.abinbev.b2b.price.api.validators.RequestValidatorHelper;
import com.abinbev.b2b.price.api.validators.v3.PriceRequestV3Validator;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;

@Validated
@RestController
@Tag(name = ApiConstants.API_NAME_PRICES)
@SecurityScheme(scheme = "bearer", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)
public class PriceControllerV3 {

	private static final Logger LOGGER = LoggerFactory.getLogger(PriceControllerV3.class);

	private final SearchForPricesAndFillIntoPriceNormalizedInfoServiceV3 searchForPricesAndFillIntoPriceNormalizedInfoServiceV3;
	private final PriceNormalizedInfoToPriceResponseVoV3Converter priceNormalizedInfoToPriceResponseVoV3Converter;
	private final PriceRequestV3ToPriceNormalizedInfoConverter priceRequestV3ToPriceNormalizedInfoConverter;
	private final PriceRequestV3Validator priceRequestV3Validator;
	private final RequestValidatorHelper requestValidatorHelper;

	@Autowired
	public PriceControllerV3(
			final SearchForPricesAndFillIntoPriceNormalizedInfoServiceV3 searchForPricesAndFillIntoPriceNormalizedInfoServiceV3,
			final PriceNormalizedInfoToPriceResponseVoV3Converter priceNormalizedInfoToPriceResponseVoV3Converter,
			final PriceRequestV3ToPriceNormalizedInfoConverter priceRequestV3ToPriceNormalizedInfoConverter,
			final PriceRequestV3Validator priceRequestV3Validator, final RequestValidatorHelper requestValidatorHelper) {

		this.searchForPricesAndFillIntoPriceNormalizedInfoServiceV3 = searchForPricesAndFillIntoPriceNormalizedInfoServiceV3;
		this.priceNormalizedInfoToPriceResponseVoV3Converter = priceNormalizedInfoToPriceResponseVoV3Converter;
		this.priceRequestV3ToPriceNormalizedInfoConverter = priceRequestV3ToPriceNormalizedInfoConverter;
		this.priceRequestV3Validator = priceRequestV3Validator;
		this.requestValidatorHelper = requestValidatorHelper;
	}

	@Operation(summary = ApiConstants.API_PRICES_DESCRIPTION, responses = {
			@ApiResponse(responseCode = "200", description = ApiResponseConstants.STATUS_200_GET_OK, content = @Content(array = @ArraySchema(schema = @Schema(implementation = PriceResponseVoV3.class)))),
			@ApiResponse(responseCode = "204", description = ApiResponseConstants.STATUS_204_NO_CONTENT, content = @Content(schema = @Schema(implementation = Issue.class))),
			@ApiResponse(responseCode = "400", description = ApiResponseConstants.STATUS_400_BAD_REQUEST, content = @Content(schema = @Schema(implementation = Issue.class))),
			@ApiResponse(responseCode = "403", description = ApiResponseConstants.STATUS_403_FORBIDDEN, content = @Content(schema = @Schema(implementation = Issue.class))),
			@ApiResponse(responseCode = "503", description = ApiResponseConstants.STATUS_503_SERVER_ERROR, content = @Content(schema = @Schema(implementation = Issue.class))) })
	@Parameter(description = ApiConstants.REQUEST_TRACE_ID_DESCRIPTION, required = true, name = ApiConstants.REQUEST_TRACE_ID_HEADER, in = ParameterIn.HEADER)
	@PostMapping(path = "/v3", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<List<PriceResponseVoV3>> getPricesV3(
			@Parameter(name = ApiConstants.COUNTRY_HEADER, description = ApiConstants.COUNTRY_DESCRIPTION, required = true) @RequestHeader(name = ApiConstants.COUNTRY_HEADER) final String country,
			@Parameter(name = ApiConstants.REQUEST_TRACE_ID_HEADER, description = ApiConstants.REQUEST_TRACE_ID_DESCRIPTION, required = true) @RequestHeader(ApiConstants.REQUEST_TRACE_ID_HEADER) final String requestTraceId,
			@Parameter(name = ApiConstants.AUTHORIZATION_HEADER, description = ApiConstants.AUTHORIZATION_DESCRIPTION, hidden = true) @RequestHeader(name = ApiConstants.AUTHORIZATION_HEADER, required = false) final String authorization,
			@Parameter(name = ApiConstants.IGNORE_VALID_FROM, description = ApiConstants.IGNORE_VALID_FROM_DESCRIPTION) @RequestParam(value = ApiConstants.IGNORE_VALID_FROM, required = false, defaultValue = "false") final Boolean ignoreValidFrom,
			@RequestBody final List<@Valid PriceRequestV3> itemsRequest) {

		LOGGER.info("[PriceControllerV3] Starting finding prices");

		priceRequestV3Validator.validate(itemsRequest);

		requestValidatorHelper.validateHeadersV3(authorization, country);

		final List<PriceNormalizedInfo> priceNormalizedInfoList = convertToPriceNormalizedInfo(itemsRequest);

		List<PriceNormalizedInfo> priceNormalizedInfoListResult =
				searchForPricesAndFillIntoPriceNormalizedInfoServiceV3.execute(priceNormalizedInfoList, country, ignoreValidFrom);

		final List<PriceResponseVoV3> priceResponseVoV3List = convertToPriceResponseList(priceNormalizedInfoListResult);

		LOGGER.info("[PriceControllerV3] {} prices converted", priceResponseVoV3List.size());

		if (CollectionUtils.isEmpty(priceResponseVoV3List)) {
			return ResponseEntity.noContent().build();
		}

		LOGGER.info("[PriceControllerV3] Find prices finished");

		return ResponseEntity.ok(priceResponseVoV3List);
	}

	private List<PriceNormalizedInfo> convertToPriceNormalizedInfo(final List<PriceRequestV3> itemsRequest) {

		return itemsRequest.stream().map(priceRequestV3ToPriceNormalizedInfoConverter::convert).collect(Collectors.toList());
	}

	private List<PriceResponseVoV3> convertToPriceResponseList(final List<PriceNormalizedInfo> priceNormalizedInfoList) {

		return priceNormalizedInfoList.stream().map(priceNormalizedInfoToPriceResponseVoV3Converter::convert).filter(Objects::nonNull)
				.collect(Collectors.toList());
	}
}
