package uk.tw.energy.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.MeterReadings;
import uk.tw.energy.service.UsageService;

@RestController
@RequestMapping("/usage")
public class ElectricityUsageController {

	public final static String LAST_WEEK_USAGE_COST = "lastWeekUsageCost";
	private final UsageService usageService;

	public ElectricityUsageController(UsageService usageService) {
		this.usageService = usageService;
	}

	@GetMapping("/lastWeekUsage/{smartMeterId}")
	public ResponseEntity lastWeekUsage(@PathVariable String smartMeterId) {
		
		if (!usageService.isMeterIdExists(smartMeterId)) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
		if (!usageService.isMeterReadingValid(smartMeterId)) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    }
		
		BigDecimal lastWeekUsage = usageService.getCostOfLastWeekUsage(smartMeterId);

		Map<String, Object> usageResponse = new HashMap<>();
		usageResponse.put(LAST_WEEK_USAGE_COST, lastWeekUsage);

		return ResponseEntity.ok(usageResponse);
	}
	
}
