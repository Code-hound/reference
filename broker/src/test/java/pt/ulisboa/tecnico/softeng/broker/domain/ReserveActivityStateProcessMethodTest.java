package pt.ulisboa.tecnico.softeng.broker.domain;

import mockit.Delegate;
import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import pt.ulisboa.tecnico.softeng.activity.exception.ActivityException;
import pt.ulisboa.tecnico.softeng.broker.domain.Adventure.State;
import pt.ulisboa.tecnico.softeng.broker.exception.RemoteAccessException;
import pt.ulisboa.tecnico.softeng.broker.interfaces.ActivityInterface;
import pt.ulisboa.tecnico.softeng.broker.interfaces.TaxInterface;

@RunWith(JMockit.class)
public class ReserveActivityStateProcessMethodTest {
	private static final String BROKER_IBAN = "BROKER_IBAN";
	private static final String NIF_AS_BUYER = "buyerNIF";
	private static final String NIF_AS_SELLER = "sellerNIF";
	private static final String NIF = "123456789";
	private static final String IBAN = "BK01987654321";
	private static final String DRIVING_LICENSE = "IMT1234";
	private static final double MARGIN = 0.3;
	private static final int AGE = 20;
	private static final String ACTIVITY_CONFIRMATION = "ActivityConfirmation";
	private static final LocalDate begin = new LocalDate(2016, 12, 19);
	private static final LocalDate end = new LocalDate(2016, 12, 21);
	private Adventure adventure;

	private Broker broker;
	private Client client;

    @Mocked private TaxInterface taxInterface;

	@Before
	public void setUp() {
		this.broker = new Broker("Br013", "HappyWeek", NIF_AS_SELLER, NIF_AS_BUYER, BROKER_IBAN);
		this.client = new Client(this.broker, IBAN, NIF, DRIVING_LICENSE, AGE);

		this.adventure = new Adventure(this.broker, begin, end, this.client, MARGIN);
		this.adventure.setState(State.RESERVE_ACTIVITY);
	}

	@Test
	public void successNoBookRoom(@Mocked final ActivityInterface activityInterface) {
		Adventure sameDayAdventure = new Adventure(this.broker, begin, begin, this.client, MARGIN);
		sameDayAdventure.setState(State.RESERVE_ACTIVITY);

		new Expectations() {
			{
				ActivityInterface.reserveActivity(begin, begin, AGE, this.anyString, this.anyString);
				this.result = ACTIVITY_CONFIRMATION;
			}
		};

		sameDayAdventure.process();

		Assert.assertEquals(State.PROCESS_PAYMENT, sameDayAdventure.getState());
	}


	@Test
	public void successToRentVehicle(@Mocked final ActivityInterface activityInterface) {
		Adventure adv = new Adventure(broker, begin, begin, client, MARGIN, true);
		adv.setState(State.RESERVE_ACTIVITY);

		new Expectations() {
			{
				ActivityInterface.reserveActivity(begin, begin, AGE, this.anyString, this.anyString);
				this.result = ACTIVITY_CONFIRMATION;
			}
		};

		adv.process();

		Assert.assertEquals(State.RENT_VEHICLE, adv.getState());
	}

	@Test
	public void successBookRoom(@Mocked final ActivityInterface activityInterface) {
		new Expectations() {
			{
				ActivityInterface.reserveActivity(begin, end, AGE, this.anyString, this.anyString);
				this.result = ACTIVITY_CONFIRMATION;
			}
		};

		this.adventure.process();

		Assert.assertEquals(State.BOOK_ROOM, this.adventure.getState());
	}

	@Test
	public void activityException(@Mocked final ActivityInterface activityInterface) {
		new Expectations() {
			{
				ActivityInterface.reserveActivity(begin, end, AGE, this.anyString, this.anyString);
				this.result = new ActivityException();
			}
		};

		this.adventure.process();

		Assert.assertEquals(State.UNDO, this.adventure.getState());
	}

	@Test
	public void singleRemoteAccessException(@Mocked final ActivityInterface activityInterface) {
		new Expectations() {
			{
				ActivityInterface.reserveActivity(begin, end, AGE, this.anyString, this.anyString);
				this.result = new RemoteAccessException();
			}
		};

		this.adventure.process();

		Assert.assertEquals(State.RESERVE_ACTIVITY, this.adventure.getState());
	}

	@Test
	public void maxRemoteAccessException(@Mocked final ActivityInterface activityInterface) {
		new Expectations() {
			{
				ActivityInterface.reserveActivity(begin, end, AGE, this.anyString, this.anyString);
				this.result = new RemoteAccessException();
			}
		};

		this.adventure.process();
		this.adventure.process();
		this.adventure.process();
		this.adventure.process();
		this.adventure.process();

		Assert.assertEquals(State.UNDO, this.adventure.getState());
	}

	@Test
	public void maxMinusOneRemoteAccessException(@Mocked final ActivityInterface activityInterface) {
		new Expectations() {
			{
				ActivityInterface.reserveActivity(begin, end, AGE, this.anyString, this.anyString);
				this.result = new RemoteAccessException();
			}
		};

		this.adventure.process();
		this.adventure.process();
		this.adventure.process();
		this.adventure.process();

		Assert.assertEquals(State.RESERVE_ACTIVITY, this.adventure.getState());
	}

	@Test
	public void twoRemoteAccessExceptionOneSuccess(@Mocked final ActivityInterface activityInterface) {
		new Expectations() {
			{
				ActivityInterface.reserveActivity(begin, end, AGE, this.anyString, this.anyString);
				this.result = new Delegate() {
					int i = 0;

					public String delegate() {
						if (this.i < 2) {
							this.i++;
							throw new RemoteAccessException();
						} else {
							return ACTIVITY_CONFIRMATION;
						}
					}
				};
				this.times = 3;

			}
		};

		this.adventure.process();
		this.adventure.process();
		this.adventure.process();

		Assert.assertEquals(State.BOOK_ROOM, this.adventure.getState());
	}

	@Test
	public void oneRemoteAccessExceptionOneActivityException(@Mocked final ActivityInterface activityInterface) {
		new Expectations() {
			{
				ActivityInterface.reserveActivity(begin, end, AGE, this.anyString, this.anyString);
				this.result = new Delegate() {
					int i = 0;

					public String delegate() {
						if (this.i < 1) {
							this.i++;
							throw new RemoteAccessException();
						} else {
							throw new ActivityException();
						}
					}
				};
				this.times = 2;
			}
		};

		this.adventure.process();
		this.adventure.process();

		Assert.assertEquals(State.UNDO, this.adventure.getState());
	}

	@After
	public void tearDown() {
		Broker.brokers.clear();
	}
}