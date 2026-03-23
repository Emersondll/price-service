package com.abinbev.b2b.price.api.services.v3;

import static java.util.Optional.ofNullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.abinbev.b2b.price.api.domain.v3.PriceNormalizedInfo;
import com.abinbev.b2b.price.domain.model.v2.EntityTypeEnum;
import com.abinbev.b2b.price.domain.model.v2.PriceEntityV2;

@Service
public class PrioritizePriceByTypeServiceV3 {

	public void execute(final List<PriceEntityV2> filteredPrices, final List<PriceNormalizedInfo> priceNormalizedInfos) {

		final Map<String, Map<String, PriceEntityV2>> pricesByVendorIdByVendorItemId = new HashMap<>();
		final Map<String, Map<String, Map<String, PriceEntityV2>>> pricesByAccountIdByVendorItemIdByVendorId = new HashMap<>();
		final Map<String, Map<String, Map<String, PriceEntityV2>>> pricesByVendorDeliveryCenterIdByVendorItemIdByVendorId = new HashMap<>();
		final Map<String, Map<String, Map<String, PriceEntityV2>>> pricesByPriceListByVendorItemIdByVendorId = new HashMap<>();

		fillPricesIntoMapsByType(filteredPrices, pricesByVendorIdByVendorItemId, pricesByAccountIdByVendorItemIdByVendorId,
				pricesByVendorDeliveryCenterIdByVendorItemIdByVendorId, pricesByPriceListByVendorItemIdByVendorId);

		fillSelectedPriceIntoNormalizedInfos(priceNormalizedInfos, pricesByVendorIdByVendorItemId,
				pricesByAccountIdByVendorItemIdByVendorId,
				pricesByVendorDeliveryCenterIdByVendorItemIdByVendorId, pricesByPriceListByVendorItemIdByVendorId);
	}

	private void fillSelectedPriceIntoNormalizedInfos(final List<PriceNormalizedInfo> priceNormalizedInfos,
			final Map<String, Map<String, PriceEntityV2>> pricesByVendorIdByVendorItemId,
			final Map<String, Map<String, Map<String, PriceEntityV2>>> pricesByAccountIdByVendorItemIdByVendorId,
			final Map<String, Map<String, Map<String, PriceEntityV2>>> pricesByVendorDeliveryCenterIdByVendorItemIdByVendorId,
			final Map<String, Map<String, Map<String, PriceEntityV2>>> pricesByPriceListByVendorItemIdByVendorId) {

		priceNormalizedInfos.forEach(priceNormalizedInfo -> {
			PriceEntityV2 prioritizePrice = getPriceByAccount(pricesByAccountIdByVendorItemIdByVendorId, priceNormalizedInfo);
			if (prioritizePrice == null) {
				prioritizePrice = getPriceByPriceList(pricesByPriceListByVendorItemIdByVendorId, priceNormalizedInfo);
			}
			if (prioritizePrice == null) {
				prioritizePrice = getPriceByDeliveryCenter(pricesByVendorDeliveryCenterIdByVendorItemIdByVendorId,
						priceNormalizedInfo);
			}
			if (prioritizePrice == null) {
				prioritizePrice = getPriceByVendor(pricesByVendorIdByVendorItemId, priceNormalizedInfo);
			}

			priceNormalizedInfo.setSelectedPrice(prioritizePrice);
		});
	}

	private void fillPricesIntoMapsByType(final List<PriceEntityV2> filteredPrices,
			final Map<String, Map<String, PriceEntityV2>> pricesByVendorIdByVendorItemId,
			final Map<String, Map<String, Map<String, PriceEntityV2>>> pricesByAccountIdByVendorItemIdByVendorId,
			final Map<String, Map<String, Map<String, PriceEntityV2>>> pricesByVendorDeliveryCenterIdByVendorItemIdByVendorId,
			final Map<String, Map<String, Map<String, PriceEntityV2>>> pricesByPriceListByVendorItemIdByVendorId) {

		filteredPrices.forEach(priceEntityV2 -> {
			if (EntityTypeEnum.VENDOR.name().equals(priceEntityV2.getId().getType())) {
				pricesByVendorIdByVendorItemId.computeIfAbsent(priceEntityV2.getId().getId(), k -> new HashMap<>())
						.put(priceEntityV2.getId().getVendorItemId(), priceEntityV2);
			} else if (EntityTypeEnum.ACCOUNT.name().equals(priceEntityV2.getId().getType())) {
				pricesByAccountIdByVendorItemIdByVendorId.computeIfAbsent(priceEntityV2.getId().getId(), k -> new HashMap<>())
						.computeIfAbsent(priceEntityV2.getId().getVendorItemId(), k -> new HashMap<>())
						.put(priceEntityV2.getId().getVendorId(), priceEntityV2);
			} else if (EntityTypeEnum.DELIVERY_CENTER.name().equals(priceEntityV2.getId().getType())) {
				pricesByVendorDeliveryCenterIdByVendorItemIdByVendorId.computeIfAbsent(priceEntityV2.getId().getId(), k -> new HashMap<>())
						.computeIfAbsent(priceEntityV2.getId().getVendorItemId(), k -> new HashMap<>())
						.put(priceEntityV2.getId().getVendorId(), priceEntityV2);
			} else if (EntityTypeEnum.PRICE_LIST.name().equals(priceEntityV2.getId().getType())) {
				pricesByPriceListByVendorItemIdByVendorId.computeIfAbsent(priceEntityV2.getId().getId(), k -> new HashMap<>())
						.computeIfAbsent(priceEntityV2.getId().getVendorItemId(), k -> new HashMap<>())
						.put(priceEntityV2.getId().getVendorId(), priceEntityV2);
			}
		});
	}

	private PriceEntityV2 getPriceByVendor(final Map<String, Map<String, PriceEntityV2>> pricesByVendorIdByVendorItemId,
			final PriceNormalizedInfo priceNormalizedInfo) {

		return ofNullable(pricesByVendorIdByVendorItemId.get(priceNormalizedInfo.getVendorId())).map(
				map -> map.get(priceNormalizedInfo.getVendorItemId())).orElse(null);
	}

	private PriceEntityV2 getPriceByAccount(
			final Map<String, Map<String, Map<String, PriceEntityV2>>> pricesByAccountIdByVendorItemIdByVendorId,
			final PriceNormalizedInfo priceNormalizedInfo) {

		return ofNullable(
				pricesByAccountIdByVendorItemIdByVendorId.get(priceNormalizedInfo.getAccountId())).map(
						map -> map.get(priceNormalizedInfo.getVendorItemId())).map(map -> map.get(priceNormalizedInfo.getVendorId()))
				.orElse(null);
	}

	private PriceEntityV2 getPriceByDeliveryCenter(
			final Map<String, Map<String, Map<String, PriceEntityV2>>> pricesByVendorDeliveryCenterIdByVendorItemIdByVendorId,
			final PriceNormalizedInfo priceNormalizedInfo) {

		return ofNullable(
				pricesByVendorDeliveryCenterIdByVendorItemIdByVendorId.get(priceNormalizedInfo.getVendorDeliveryCenterId())).map(
						map -> map.get(priceNormalizedInfo.getVendorItemId()))
				.map(map -> map.get(priceNormalizedInfo.getVendorId()))
				.orElse(null);
	}

	private PriceEntityV2 getPriceByPriceList(
			final Map<String, Map<String, Map<String, PriceEntityV2>>> pricesByPriceListByVendorItemIdByVendorId,
			final PriceNormalizedInfo priceNormalizedInfo) {

		return ofNullable(
				pricesByPriceListByVendorItemIdByVendorId.get(priceNormalizedInfo.getPriceListId())).map(
						map -> map.get(priceNormalizedInfo.getVendorItemId())).map(map -> map.get(priceNormalizedInfo.getVendorId()))
				.orElse(null);
	}

}