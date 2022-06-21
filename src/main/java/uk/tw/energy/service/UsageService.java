package uk.tw.energy.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.MeterReadings;
import uk.tw.energy.domain.PricePlan;

@Service
public class UsageService {

	private final Map<String, List<ElectricityReading>> meterAssociatedReadings;
	private final Map<String, String> smartMeterToPricePlanAccounts;
	private final List<PricePlan> pricePlans;
	
	public UsageService(Map<String, List<ElectricityReading>> meterAssociatedReadings, 
			Map<String, String> smartMeterToPricePlanAccounts, List<PricePlan> pricePlans) {
		this.meterAssociatedReadings = meterAssociatedReadings;
		this.smartMeterToPricePlanAccounts = smartMeterToPricePlanAccounts;
		this.pricePlans = pricePlans;
	}

	public BigDecimal getCostOfLastWeekUsage(String smartMeterId) {
		PricePlan pricePlan = pricePlans.stream().filter(plan -> plan.getPlanName()
				.equals(this.smartMeterToPricePlanAccounts.get(smartMeterId))).collect(Collectors.toList()).get(0);
		BigDecimal costOfUsage = new BigDecimal(0);		
		final int dayOfWeek = LocalDate.now().getDayOfWeek().getValue();
	    final LocalDate startDate = LocalDate.now().minusDays(dayOfWeek + 7);
	    final LocalDate endDate = LocalDate.now().minusDays(dayOfWeek+1);
	    
		for(ElectricityReading eReadings : meterAssociatedReadings.get(smartMeterId)) {
			if(LocalDateTime.ofInstant(eReadings.getTime(), ZoneOffset.UTC).toLocalDate().isAfter(startDate) && 
					LocalDateTime.ofInstant(eReadings.getTime(), ZoneOffset.UTC).toLocalDate().isBefore(endDate)) {
				costOfUsage = costOfUsage.add(pricePlan.getPrice(LocalDateTime.ofInstant(eReadings.getTime(), ZoneId.systemDefault())));
			}
		}
		return costOfUsage;
	}
	
	public boolean isMeterIdExists(String smartMeterId) {
        return meterAssociatedReadings.get(smartMeterId) != null;
    }
	
	public boolean isMeterReadingValid(String smartMeterId) {
		List<ElectricityReading> electricityReadings = meterAssociatedReadings.get(smartMeterId);
        return smartMeterId != null && !smartMeterId.isEmpty()
                && electricityReadings != null && !electricityReadings.isEmpty();
    }
}
