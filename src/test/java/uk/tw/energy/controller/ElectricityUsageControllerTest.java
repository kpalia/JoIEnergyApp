package uk.tw.energy.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import uk.tw.energy.builders.MeterReadingsBuilder;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.MeterReadings;
import uk.tw.energy.domain.PricePlan;
import uk.tw.energy.service.UsageService;

class ElectricityUsageControllerTest {

	private String SMART_METER_ID = "123456789";
	private String PRICE_PLAN_ID = "Price_Plan_01";
	private String PRICE_PLAN_NAME = "UK_GAS_01";
	private UsageService usageService;
	private Map<String, List<ElectricityReading>> meterAssociatedReadings;
	private Map<String, String> smartMeterToPricePlanAccounts;
	private ElectricityUsageController electricityUsageController;
	private PricePlan pricePlan;
	
	@BeforeEach
    public void setUp() {
		meterAssociatedReadings = new HashMap<>();
		meterAssociatedReadings = new HashMap<>();
		smartMeterToPricePlanAccounts = new HashMap<>();
		smartMeterToPricePlanAccounts.put(SMART_METER_ID, PRICE_PLAN_ID);
		pricePlan = new PricePlan(PRICE_PLAN_ID, PRICE_PLAN_NAME, BigDecimal.TEN, 
				Arrays.asList(new PricePlan.PeakTimeMultiplier(DayOfWeek.MONDAY, BigDecimal.valueOf(5))));
		usageService = new UsageService(meterAssociatedReadings, smartMeterToPricePlanAccounts, 
				Arrays.asList(pricePlan));
		electricityUsageController = new ElectricityUsageController(usageService);
    }
	
	@AfterEach
	public void tearDown() {
		meterAssociatedReadings = null;
		smartMeterToPricePlanAccounts = null;
		pricePlan = null;
		usageService = null;
		electricityUsageController = null;
	}

	@Test
	public void givenCorrectMeterIdWithReadingShouldReturnOkResponse() {
		 MeterReadings readings = new MeterReadingsBuilder().setSmartMeterId(SMART_METER_ID)
	                .generateElectricityReadings()
	                .build();
		meterAssociatedReadings.put(readings.getSmartMeterId(), readings.getElectricityReadings());
		assertThat(electricityUsageController.lastWeekUsage(readings.getSmartMeterId()).getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	
	@Test
	public void givenNoElectricityReadingsAreSuppliedShouldGetErrorResponse() {
		MeterReadings readings = new MeterReadings(SMART_METER_ID, Collections.emptyList());
		meterAssociatedReadings.put(readings.getSmartMeterId(), readings.getElectricityReadings());
		assertThat(electricityUsageController.lastWeekUsage(readings.getSmartMeterId()).getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Test
	public void givenMeterIdIsUnkownThenShouldGetNotFound() {
		MeterReadings readings = new MeterReadings(SMART_METER_ID, Collections.emptyList());
		meterAssociatedReadings.put(readings.getSmartMeterId(), readings.getElectricityReadings());
		assertThat(electricityUsageController.lastWeekUsage("unkwon-id").getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
}
