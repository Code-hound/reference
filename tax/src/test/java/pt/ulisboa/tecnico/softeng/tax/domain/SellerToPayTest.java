package pt.ulisboa.tecnico.softeng.tax.domain;

import static org.junit.Assert.assertEquals;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import pt.ulisboa.tecnico.softeng.tax.exception.TaxException;

public class SellerToPayTest {
	private static final String SELLER_NIF = "123456789";
	private static final String BUYER_NIF = "987654321";
	private static final String FOOD = "FOOD";
	private static final int TAX = 10;
	private final LocalDate date = new LocalDate(2018, 02, 13);

	private Seller seller;
	private Buyer buyer;
	private ItemType itemType;

	@Before
	public void setUp() {
		IRS.getIRS();
		this.seller = new Seller(SELLER_NIF, "José Vendido", "Somewhere");
		this.buyer = new Buyer(BUYER_NIF, "Manuel Comprado", "Anywhere");
		this.itemType = new ItemType(FOOD, TAX);
	}

	@Test
	public void success() {
		new Invoice(100, this.date, this.itemType, this.seller, this.buyer);
		new Invoice(100, this.date, this.itemType, this.seller, this.buyer);
		new Invoice(50, this.date, this.itemType, this.seller, this.buyer);

		float value = this.seller.toPay(2018);

		assertEquals(25.0f, value, 0.00f);
	}

	@Test
	public void yearWithoutInvoices() {
		new Invoice(100, this.date, this.itemType, this.seller, this.buyer);
		new Invoice(100, this.date, this.itemType, this.seller, this.buyer);
		new Invoice(50, this.date, this.itemType, this.seller, this.buyer);

		assertEquals(0.0f, this.seller.toPay(2015), 0.0f);
	}

	@Test
	public void noInvoices() {
		float value = this.seller.toPay(2018);

		assertEquals(0.0f, value, 0.00f);
	}

	@Test(expected = TaxException.class)
	public void before1970() {
		this.seller.toPay(1969);
	}

	public void equal1970() {
		float value = this.seller.toPay(1969);

		assertEquals(0.0f, value, 0.00f);
	}

	@After
	public void tearDown() {
		IRS.getIRS().clearAll();
	}

}
