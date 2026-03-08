package com.vocaldoc.controller;

import com.vocaldoc.common.result.Result;
import com.vocaldoc.service.ComparisonService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/analysis")
public class ComparisonController {

    private final ComparisonService comparisonService;

    public ComparisonController(ComparisonService comparisonService) {
        this.comparisonService = comparisonService;
    }

    @GetMapping("/compare")
    public Result<Map<String, Object>> compare(
            @RequestParam Long recordId1,
            @RequestParam Long recordId2) {
        Map<String, Object> result = comparisonService.compareTwoRecords(recordId1, recordId2);
        return Result.success(result);
    }

    @GetMapping("/trend")
    public Result<Map<String, Object>> trend(
            @RequestParam Long userId,
            @RequestParam String metric) {
        Map<String, Object> result = comparisonService.analyzeTrend(userId, metric);
        return Result.success(result);
    }

    @GetMapping("/progress")
    public Result<Map<String, Object>> progress(@RequestParam Long userId) {
        Map<String, Object> result = comparisonService.checkProgress(userId);
        return Result.success(result);
    }
}
