package pt.ulisboa.tecnico.softeng.car.domain

import org.joda.time.LocalDate

class RentACarGetRentingSpockTest extends SpockRollbackTestAbstractClass {
    private static final String ADVENTURE_ID = "AdventureId"
    private static final String NAME1='eartz'
    private static final String PLATE_CAR1='aa-00-11'
    private static final String DRIVING_LICENSE='br123'
    private static final LocalDate date1= LocalDate.parse('2018-01-06')
    private static final LocalDate date2= LocalDate.parse('2018-01-07')
    private static final LocalDate date3= LocalDate.parse('2018-01-08')
    private static final LocalDate date4= LocalDate.parse('2018-01-09')
    private static final String NIF='NIF'
    private static final String IBAN='IBAN'
    private static final String IBAN_BUYER='IBAN'
    private Renting renting

    @Override
    def populate4Test() {
        RentACar rentACar1 = new RentACar(NAME1,NIF,IBAN)
        Vehicle car1 = new Car(PLATE_CAR1,10,10,rentACar1)

        renting = car1.rent(DRIVING_LICENSE,date1,date2,NIF,IBAN_BUYER,ADVENTURE_ID)

        car1.rent(DRIVING_LICENSE,date3,date4,NIF,IBAN_BUYER,ADVENTURE_ID)
    }

    def 'get renting'() {
        expect:
        RentACar.getRenting(renting.getReference()) == renting
    }

    def 'non existing'() {
        expect:
        null == RentACar.getRenting("a")
    }
}
