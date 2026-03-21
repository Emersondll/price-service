package com.abinbev.b2b.price.api.converters.v3;

import static com.abinbev.b2b.commons.platformId.core.enuns.PlatformIdEnum.CONTRACT;
import static com.abinbev.b2b.commons.platformId.core.enuns.PlatformIdEnum.DELIVERY_CENTER;
import static com.abinbev.b2b.commons.platformId.core.enuns.PlatformIdEnum.ITEM;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.abinbev.b2b.commons.platformId.core.PlatformIdEncoderDecoder;
import com.abinbev.b2b.commons.platformId.core.vo.ContractPlatformId;
import com.abinbev.b2b.commons.platformId.core.vo.DeliveryCenterPlatformId;
import com.abinbev.b2b.commons.platformId.core.vo.ItemPlatformId;
import com.abinbev.b2b.price.api.domain.v3.PriceNormalizedInfo;
import com.abinbev.b2b.price.api.exceptions.BadRequestException;
import com.abinbev.b2b.price.api.rest.vo.v3.PriceRequestV3;

@Component
public class PriceRequestV3ToPriceNormalizedInfoConverter {

	private final PlatformIdEncoderDecoder platformIdEncoderDecoder;

	@Autowired
	public PriceRequestV3ToPriceNormalizedInfoConverter(final PlatformIdEncoderDecoder platformIdEncoderDecoder) {

		this.platformIdEncoderDecoder = platformIdEncoderDecoder;
	}

	public PriceNormalizedInfo convert(final PriceRequestV3 priceRequestV3) {

		final PriceNormalizedInfo priceNormalizedInfo = new PriceNormalizedInfo();

		priceNormalizedInfo.setItemId(priceRequestV3.getItemId());
		priceNormalizedInfo.setContractId(priceRequestV3.getContractId());
		priceNormalizedInfo.setPriceListId(priceRequestV3.getPriceListId());
		priceNormalizedInfo.setDeliveryCenterId(priceRequestV3.getDeliveryCenterId());

		final ItemPlatformId itemPlatformId = (ItemPlatformId) platformIdEncoderDecoder.decodePlatformId(priceRequestV3.getItemId(), ITEM);
		validateDecoding(itemPlatformId.getVendorId(), itemPlatformId.getVendorId(), "itemId");
		priceNormalizedInfo.setVendorId(itemPlatformId.getVendorId());
		priceNormalizedInfo.setVendorItemId(itemPlatformId.getVendorItemId());

		if (isNotBlank(priceRequestV3.getContractId())) {
			final ContractPlatformId contractPlatformId = (ContractPlatformId) platformIdEncoderDecoder.decodePlatformId(
					priceRequestV3.getContractId(), CONTRACT);
			validateDecoding(contractPlatformId.getVendorId(), contractPlatformId.getVendorAccountId(), "contractId");
			priceNormalizedInfo.setAccountId(contractPlatformId.getVendorAccountId());
		}

		if (isNotBlank(priceRequestV3.getDeliveryCenterId())) {
			final DeliveryCenterPlatformId deliveryCenterPlatformId = (DeliveryCenterPlatformId) platformIdEncoderDecoder.decodePlatformId(
					priceRequestV3.getDeliveryCenterId(), DELIVERY_CENTER);
			validateDecoding(deliveryCenterPlatformId.getVendorId(), deliveryCenterPlatformId.getVendorId(), "deliveryCenterId");
			priceNormalizedInfo.setVendorDeliveryCenterId(deliveryCenterPlatformId.getVendorDeliveryCenterId());
		}

		return priceNormalizedInfo;
	}

	private void validateDecoding(final String vendorId, final String keyId, final String field) {

		final List<String> ids = Arrays.asList(vendorId, keyId);
		if (ids.contains(null) || ids.contains("null")) {
			throw BadRequestException.requestDecodingError(field);
		}
	}
}
