package com.abinbev.b2b.price.api.controllers;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.abinbev.b2b.price.api.config.ApiConfig;
import com.abinbev.b2b.price.api.domain.BrowsePrice;
import com.abinbev.b2b.price.api.domain.OffersAliasParameters;
import com.abinbev.b2b.price.api.exceptions.Issue;
import com.abinbev.b2b.price.api.helpers.ApiConstants;
import com.abinbev.b2b.price.api.helpers.ApiResponseConstants;
import com.abinbev.b2b.price.api.helpers.offers.enums.VersionFlag;
import com.abinbev.b2b.price.api.rest.vo.CustomerProductPriceResponseVo;
import com.abinbev.b2b.price.api.services.CalculateBrowsePricesV1OrchestratorService;
import com.abinbev.b2b.price.api.services.v2.CalculateBrowsePricesV2OrchestratorService;
import com.abinbev.b2b.price.api.validators.PriceOffersRequestValidator;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = ApiConstants.API_NAME_PRICES_OFFERS)
@SecurityScheme(scheme = "bearer", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)
public class PriceOffersController {

	private static final Logger LOGGER = LoggerFactory.getLogger(PriceOffersController.class);

	private final CalculateBrowsePricesV1OrchestratorService calculateBrowsePricesV1OrchestratorService;
	private final CalculateBrowsePricesV2OrchestratorService calculateBrowsePricesV2OrchestratorService;
	private final PriceOffersRequestValidator priceOffersRequestValidator;
	private final ApiConfig apiConfig;

	@Autowired
	public PriceOffersController(final CalculateBrowsePricesV1OrchestratorService calculateBrowsePricesV1OrchestratorService,
			final CalculateBrowsePricesV2OrchestratorService calculateBrowsePricesV2OrchestratorService,
			final PriceOffersRequestValidator priceOffersRequestValidator, final ApiConfig apiConfig) {

		this.calculateBrowsePricesV1OrchestratorService = calculateBrowsePricesV1OrchestratorService;
		this.calculateBrowsePricesV2OrchestratorService = calculateBrowsePricesV2OrchestratorService;
		this.priceOffersRequestValidator = priceOffersRequestValidator;
		this.apiConfig = apiConfig;
	}

	@Operation(summary = ApiConstants.API_PRICES_OFFERS_DESCRIPTION, responses = {
			@ApiResponse(responseCode = "200", description = ApiResponseConstants.STATUS_200_GET_OK, content = @Content(array = @ArraySchema(schema = @Schema(implementation = CustomerProductPriceResponseVo.class)))),
			@ApiResponse(responseCode = "400", description = ApiResponseConstants.STATUS_400_BAD_REQUEST, content = @Content(schema = @Schema(implementation = Issue.class))),
			@ApiResponse(responseCode = "503", description = ApiResponseConstants.STATUS_503_SERVER_ERROR, content = @Content(schema = @Schema(implementation = Issue.class))),
			@ApiResponse(responseCode = "403", description = ApiResponseConstants.STATUS_403_FORBIDDEN, content = @Content(schema = @Schema(implementation = Issue.class))),
			@ApiResponse(responseCode = "404", description = ApiResponseConstants.STATUS_404_NOT_FOUND, content = @Content(schema = @Schema(implementation = Issue.class))) })
	@Parameter(description = ApiConstants.REQUEST_TRACE_ID_DESCRIPTION, required = true, name = ApiConstants.REQUEST_TRACE_ID_HEADER, in = ParameterIn.HEADER)
	@GetMapping(path = "/v1/offers", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<CustomerProductPriceResponseVo>> getOffers(
			@Parameter(name = ApiConstants.COUNTRY_HEADER, description = ApiConstants.COUNTRY_DESCRIPTION, required = true) @RequestHeader(name = ApiConstants.COUNTRY_HEADER) final String country,
			@Parameter(name = ApiConstants.AUTHORIZATION_HEADER, description = ApiConstants.AUTHORIZATION_DESCRIPTION, hidden = true) @RequestHeader(value = ApiConstants.AUTHORIZATION_HEADER, required = false) @NotEmpty final String authorization,
			@Parameter(name = ApiConstants.REQUEST_TRACE_ID_HEADER, description = ApiConstants.REQUEST_TRACE_ID_DESCRIPTION, required = true) @RequestHeader(ApiConstants.REQUEST_TRACE_ID_HEADER) final String requestTraceId,
			@Parameter(name = ApiConstants.VENDOR_ID_HEADER, description = ApiConstants.VENDOR_ID_DESCRIPTION) @RequestHeader(name = ApiConstants.VENDOR_ID_HEADER, required = false) final String vendorId,
			@Parameter(name = ApiConstants.VENDOR_ACCOUNT_ID_HEADER, description = ApiConstants.VENDOR_ACCOUNT_ID_DESCRIPTION) @RequestHeader(name = ApiConstants.VENDOR_ACCOUNT_ID_HEADER, required = false) final String vendorAccountId,
			@Parameter(name = ApiConstants.ACCOUNT_ID_PARAM, description = ApiConstants.ACCOUNT_DESCRIPTION) @RequestParam(name = ApiConstants.ACCOUNT_ID_PARAM, required = false) final String accountId,
			@Parameter(name = ApiConstants.SKUS_FILTER_HEADER, description = ApiConstants.SKUS_FILTER_DESCRIPTION) @RequestParam(name = ApiConstants.SKUS_FILTER_HEADER, required = false) final List<String> skus,
			@Parameter(name = ApiConstants.VENDOR_ITEM_IDS_HEADER, description = ApiConstants.VENDOR_ITEM_ID_DESCRIPTION) @RequestParam(name = ApiConstants.VENDOR_ITEM_IDS_HEADER, required = false) final List<String> vendorItemIds) {

		final String finalVendorId = getVendorId(vendorId, country);

		LOGGER.debug("Received GET request for requestTraceId: {}", requestTraceId);

		final OffersAliasParameters offersAliasParameters = priceOffersRequestValidator.validatePriceOffersAndCheckVersion(accountId,
				vendorAccountId, skus, vendorItemIds, finalVendorId, country, authorization);

		if (VersionFlag.V2.equals(offersAliasParameters.getRequestVersion())) {
			return ResponseEntity.ok(convertFromBrowsePricesToCustomerProductPriceResponseVos(
					calculateBrowsePricesV2OrchestratorService.execute(finalVendorId, offersAliasParameters.getRequestAccountId(), country,
							offersAliasParameters.getRequestItems())));
		}

		return ResponseEntity.ok(convertFromBrowsePricesToCustomerProductPriceResponseVos(
				calculateBrowsePricesV1OrchestratorService.execute(offersAliasParameters.getRequestAccountId(), country,
						offersAliasParameters.getRequestItems())));
	}

	private List<CustomerProductPriceResponseVo> convertFromBrowsePricesToCustomerProductPriceResponseVos(
			final List<BrowsePrice> browsePrices) {

		final List<CustomerProductPriceResponseVo> customerProductPriceResponseVoList = new ArrayList<>();

		for (final BrowsePrice browsePrice : browsePrices) {
			customerProductPriceResponseVoList.add(
					new CustomerProductPriceResponseVo(browsePrice.getSku(), browsePrice.getPrice(), browsePrice.getOriginalPrice(),
							browsePrice.getValidUntil(), browsePrice.getVendorItemId()));
		}

		return customerProductPriceResponseVoList;
	}

	private String getVendorId(final String vendorId, final String country) {

		return (isNotEmpty(vendorId) && !apiConfig.shouldUseDefaultVendorId(country)) ?
				vendorId :
				apiConfig.getDefaultVendorIdByCountry(country);
	}
}
