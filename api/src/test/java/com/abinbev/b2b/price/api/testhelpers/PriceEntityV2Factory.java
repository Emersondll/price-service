package com.abinbev.b2b.price.api.testhelpers;

import java.math.BigDecimal;

import com.abinbev.b2b.price.domain.model.v2.EntityTypeEnum;
import com.abinbev.b2b.price.domain.model.v2.PriceCompoundKeyV2;
import com.abinbev.b2b.price.domain.model.v2.PriceEntityV2;

public class PriceEntityV2Factory {

	private static final String VENDOR_ID = "VENDOR_ID_01";

	private static final String VENDOR_ACCOUNT_ID = "VENDOR_ACCOUNT_ID_01";

	private static final String VENDOR_ITEM_ID_0001 = "BRAHMA";
	private static final String VENDOR_ITEM_ID_0002 = "ANTARTICA";
	private static final String VENDOR_ITEM_ID_0003 = "STELLA";


	public static PriceEntityV2 generatePriceEntity1() {

		final PriceEntityV2 priceEntityV2 = new PriceEntityV2();

		final PriceCompoundKeyV2 priceCompoundKey1 = new PriceCompoundKeyV2();
		priceCompoundKey1.setVendorId(VENDOR_ID);
		priceCompoundKey1.setId(VENDOR_ACCOUNT_ID);
		priceCompoundKey1.setType(EntityTypeEnum.PRICE_LIST.toString());
		priceCompoundKey1.setVendorItemId(VENDOR_ITEM_ID_0001);

		priceEntityV2.setId(priceCompoundKey1);
		priceEntityV2.setDeleted(false);
		priceEntityV2.setBasePrice(BigDecimal.ONE);

		return priceEntityV2;
	}

	public static PriceEntityV2 generatePriceEntity2() {

		final PriceEntityV2 priceEntityV2 = new PriceEntityV2();

		final PriceCompoundKeyV2 priceCompoundKey2 = new PriceCompoundKeyV2();
		priceCompoundKey2.setVendorId(VENDOR_ID);
		priceCompoundKey2.setId(VENDOR_ACCOUNT_ID);
		priceCompoundKey2.setType(EntityTypeEnum.ACCOUNT.toString());
		priceCompoundKey2.setVendorItemId(VENDOR_ITEM_ID_0001);

		priceEntityV2.setId(priceCompoundKey2);
		priceEntityV2.setDeleted(false);
		priceEntityV2.setBasePrice(BigDecimal.TEN);

		return priceEntityV2;
	}

	public static PriceEntityV2 generatePriceEntity3() {

		final PriceEntityV2 priceEntityV2 = new PriceEntityV2();

		final PriceCompoundKeyV2 priceCompoundKey3 = new PriceCompoundKeyV2();
		priceCompoundKey3.setVendorId(VENDOR_ID);
		priceCompoundKey3.setId(VENDOR_ACCOUNT_ID);
		priceCompoundKey3.setType(EntityTypeEnum.PRICE_LIST.toString());
		priceCompoundKey3.setVendorItemId(VENDOR_ITEM_ID_0002);

		priceEntityV2.setId(priceCompoundKey3);
		priceEntityV2.setDeleted(false);
		priceEntityV2.setBasePrice(BigDecimal.TEN);

		return priceEntityV2;
	}

	public static PriceEntityV2 generatePriceEntity4() {

		final PriceEntityV2 priceEntityV2 = new PriceEntityV2();

		final PriceCompoundKeyV2 priceCompoundKey4 = new PriceCompoundKeyV2();
		priceCompoundKey4.setVendorId(VENDOR_ID);
		priceCompoundKey4.setId(VENDOR_ACCOUNT_ID);
		priceCompoundKey4.setType(EntityTypeEnum.ACCOUNT.toString());
		priceCompoundKey4.setVendorItemId(VENDOR_ITEM_ID_0003);

		priceEntityV2.setId(priceCompoundKey4);
		priceEntityV2.setDeleted(false);
		priceEntityV2.setBasePrice(BigDecimal.TEN);

		return priceEntityV2;
	}

}