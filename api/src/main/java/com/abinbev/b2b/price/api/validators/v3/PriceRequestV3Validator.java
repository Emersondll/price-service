package com.abinbev.b2b.price.api.validators.v3;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.abinbev.b2b.price.api.config.ApiConfig;
import com.abinbev.b2b.price.api.exceptions.BadRequestException;
import com.abinbev.b2b.price.api.exceptions.IssueEnum;
import com.abinbev.b2b.price.api.rest.vo.v3.PriceRequestV3;

@Component
public class PriceRequestV3Validator {

	private final ApiConfig apiConfig;

	@Autowired
	public PriceRequestV3Validator(final ApiConfig apiConfig) {

		this.apiConfig = apiConfig;
	}

	public void validate(final List<PriceRequestV3> priceRequestV3List) {

		if (isEmpty(priceRequestV3List)) {
			throw BadRequestException.customBadRequest(IssueEnum.REQUEST_BODY_MUST_BE_A_NONEMPTY_ARRAY);
		}

		final int itemsLimit = apiConfig.getPriceV3RequestBodyItemsLimit();
		if (priceRequestV3List.size() > itemsLimit) {
			throw BadRequestException.customBadRequest(IssueEnum.EXCEEDED_LIMIT_OF_ITEMS_REQUEST, itemsLimit);
		}

		priceRequestV3List.forEach(priceRequestV3 -> {
			if (isNotBlank(priceRequestV3.getPriceListId()) && isBlank(priceRequestV3.getContractId())) {
				throw BadRequestException.customBadRequest(IssueEnum.CONTRACT_ID_IS_REQUIRED_FOR_PRICE_LIST);
			}
		});
	}
}
