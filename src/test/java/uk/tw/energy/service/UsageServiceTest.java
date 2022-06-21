package uk.tw.energy.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.tw.energy.builders.MeterReadingsBuilder;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.MeterReadings;
import uk.tw.energy.domain.PricePlan;

public class UsageServiceTest {

	private String SMART_METER_ID = "123456789";
	private String PRICE_PLAN_ID = "Price_Plan_01";
	private String PRICE_PLAN_NAME = "UK_GAS_01";
	private UsageService usageService;
	private Map<String, List<ElectricityReading>> meterAssociatedReadings;
	private Map<String, String> smartMeterToPricePlanAccounts;
	private PricePlan pricePlan;
	
	@BeforeEach
    public void setUp() {
		meterAssociatedReadings = new HashMap<>();
		smartMeterToPricePlanAccounts = new HashMap<>();		
		pricePlan = new PricePlan(PRICE_PLAN_ID, PRICE_PLAN_NAME, BigDecimal.TEN, 
				Arrays.asList(new PricePlan.PeakTimeMultiplier(DayOfWeek.MONDAY, BigDecimal.valueOf(5))));
		smartMeterToPricePlanAccounts.put(SMART_METER_ID, PRICE_PLAN_ID);
		usageService = new UsageService(meterAssociatedReadings, smartMeterToPricePlanAccounts, Arrays.asList(pricePlan));
    }
	
	@AfterEach
	public void tearDown() {
		meterAssociatedReadings = null;
		smartMeterToPricePlanAccounts = null;
		pricePlan = null;
		usageService = null;
	}
	
	@Test
	public void givenNoLastWeekUsageIsSuppliedShouldGetZeroCost() {
		storeReadings(0);
		assertThat(usageService.getCostOfLastWeekUsage(SMART_METER_ID)).isEqualTo(BigDecimal.ZERO);
	}
	
	@Test
	public void givenLastWeekUsageIsSuppliedShouldGetTotalCost() {
		storeReadings(5);
		assertThat(usageService.getCostOfLastWeekUsage(SMART_METER_ID)).isEqualTo(BigDecimal.valueOf(40));
	}
	
	private void storeReadings(int prevWeekReadings) {
		List<ElectricityReading> eReadings = new ArrayList<ElectricityReading>();
		final int dayOfWeek = LocalDate.now().getDayOfWeek().getValue();
	    final LocalDate startDate = LocalDate.now().minusDays(dayOfWeek + 6);
		for(int i = 1; i<prevWeekReadings; i++) {
			eReadings.add(new ElectricityReading(startDate.plusDays(i).atStartOfDay(ZoneOffset.UTC).toInstant(), new BigDecimal(Double.parseDouble("0.0503")+i)));
		}
		MeterReadings readings = new MeterReadingsBuilder().setSmartMeterId(SMART_METER_ID).generateElectricityReadings(4).build();
		meterAssociatedReadings.put(readings.getSmartMeterId(), readings.getElectricityReadings());
		meterAssociatedReadings.get(readings.getSmartMeterId()).addAll(eReadings);
	}
}
