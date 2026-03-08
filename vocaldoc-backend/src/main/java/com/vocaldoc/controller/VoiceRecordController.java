package com.vocaldoc.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vocaldoc.entity.VoiceRecord;
import com.vocaldoc.service.VoiceRecordService;
import com.vocaldoc.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/records")
public class VoiceRecordController {

    @Autowired
    private VoiceRecordService voiceRecordService;

    @GetMapping
    public Result<IPage<VoiceRecord>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String recordType,
            @RequestParam(required = false) Integer status
    ) {
        Page<VoiceRecord> page = new Page<>(current, size);
        IPage<VoiceRecord> result = voiceRecordService.page(page, recordType, status);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<VoiceRecord> getById(@PathVariable Long id) {
        VoiceRecord record = voiceRecordService.getById(id);
        return Result.success(record);
    }

    @PostMapping("/upload")
    public Result<VoiceRecord> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String recordType
    ) {
        try {
            VoiceRecord record = voiceRecordService.saveRecord(file, description, recordType);
            return Result.success(record);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        voiceRecordService.deleteById(id);
        return Result.success(null);
    }

    @GetMapping("/user/{userId}")
    public Result<List<VoiceRecord>> getByUserId(@PathVariable Long userId) {
        List<VoiceRecord> records = voiceRecordService.getByUserId(userId);
        return Result.success(records);
    }
}
