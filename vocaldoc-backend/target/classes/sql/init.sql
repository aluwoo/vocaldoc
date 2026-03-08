-- ============================================================
-- 声乐课堂嗓音档案管理系统 - 数据库初始化脚本
-- 创建日期: 2026-03-08
-- ============================================================

-- 创建数据库(如果不存在)
CREATE DATABASE IF NOT EXISTS vocaldoc DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE vocaldoc;

-- ============================================================
-- 1. 用户模块
-- ============================================================

-- 1.1 用户表
CREATE TABLE `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `password_hash` VARCHAR(128) COMMENT '密码哈希(可选,手机号登录时可为空)',
    `name` VARCHAR(50) COMMENT '姓名',
    `avatar` VARCHAR(500) COMMENT '头像URL',
    `role` ENUM('ADMIN', 'TEACHER', 'STUDENT') NOT NULL DEFAULT 'STUDENT' COMMENT '角色',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    `last_login_at` DATETIME COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) COMMENT '最后登录IP',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_role` (`role`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 1.2 教师扩展表
CREATE TABLE `teachers` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '教师ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `title` VARCHAR(50) COMMENT '职称(教授/副教授/讲师)',
    `specialty` VARCHAR(200) COMMENT '专业方向',
    `bio` TEXT COMMENT '个人简介',
    `invite_code` VARCHAR(20) NOT NULL COMMENT '邀请码(学生加入用)',
    `invite_count` INT NOT NULL DEFAULT 0 COMMENT '已邀请学生数',
    `max_students` INT NOT NULL DEFAULT 100 COMMENT '最大学生数',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    UNIQUE KEY `uk_invite_code` (`invite_code`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教师扩展表';

-- 1.3 学生扩展表
CREATE TABLE `students` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '学生ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `teacher_id` BIGINT COMMENT '所属教师ID',
    `age` TINYINT COMMENT '年龄',
    `gender` ENUM('MALE', 'FEMALE', 'OTHER') COMMENT '性别',
    `voice_type` ENUM('SOPRANO', 'MEZZO', 'ALTO', 'TENOR', 'BARITONE', 'BASS', 'CHILD') COMMENT '声部类型',
    `level` VARCHAR(50) COMMENT '学习等级(初/中/高级)',
    `parent_name` VARCHAR(50) COMMENT '家长姓名(未成年)',
    `parent_phone` VARCHAR(20) COMMENT '家长电话',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_teacher_id` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生扩展表';

-- 1.4 学生-教师关联表
CREATE TABLE `student_teacher` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `teacher_id` BIGINT NOT NULL COMMENT '教师ID',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-已移除, 1-正常',
    `joined_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    `removed_at` DATETIME COMMENT '移除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_teacher` (`student_id`, `teacher_id`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_teacher_id` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生教师关联表';

-- ============================================================
-- 2. 嗓音档案模块
-- ============================================================

-- 2.1 嗓音档案表
CREATE TABLE `voice_records` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '档案ID',
    `record_no` VARCHAR(32) NOT NULL COMMENT '档案编号(VD+日期+序号)',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `teacher_id` BIGINT NOT NULL COMMENT '录制教师ID',
    
    /* 档案分类 */
    `record_category` ENUM('VOWEL', 'DURATION', 'SONG') NOT NULL COMMENT '档案分类: VOWEL-元音录音, DURATION-声时长测试, SONG-完整曲目',
    `vowel_type` ENUM('A', 'I', 'O', 'U', 'E', 'MIXED') COMMENT '元音类型(元音录音时)',
    `duration_seconds` DECIMAL(6,2) COMMENT '声时长测试结果(秒)',
    
    /* 基本信息 */
    `title` VARCHAR(200) COMMENT '档案标题(如: 演唱《xxx》)',
    `song_name` VARCHAR(200) COMMENT '曲目名称',
    `record_type` ENUM('LESSON', 'PRACTICE', 'EXAM', 'ASSIGNMENT') NOT NULL DEFAULT 'LESSON' COMMENT '录制类型',
    `record_device` ENUM('PHONE', 'PROFESSIONAL', 'UPLOAD') NOT NULL COMMENT '录制设备',
    
    /* 音频文件 */
    `audio_url` VARCHAR(500) NOT NULL COMMENT '音频文件URL(OSS)',
    `audio_duration` INT NOT NULL COMMENT '音频时长(秒)',
    `audio_format` VARCHAR(20) NOT NULL COMMENT '音频格式',
    `audio_size` BIGINT COMMENT '文件大小(字节)',
    `original_filename` VARCHAR(255) COMMENT '原始文件名',
    
    /* 录音环境 */
    `record_location` VARCHAR(200) COMMENT '录音地点',
    `environment_noise` VARCHAR(50) COMMENT '环境噪音估计',
    
    /* 状态 */
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-已删除, 1-正常, 2-分析中, 3-分析完成',
    `analysis_status` TINYINT NOT NULL DEFAULT 0 COMMENT '分析状态: 0-未分析, 1-分析中, 2-分析完成, 3-分析失败',
    
    /* 教师评价 */
    `teacher_comment` TEXT COMMENT '教师评语',
    `teacher_rating` TINYINT COMMENT '星级评分(1-5)',
    `teacher_tags` VARCHAR(500) COMMENT '评价标签(JSON数组)',
    
    /* 时间戳 */
    `record_at` DATETIME NOT NULL COMMENT '录音时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_record_no` (`record_no`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_teacher_id` (`teacher_id`),
    KEY `idx_record_at` (`record_at`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='嗓音档案表';

-- 2.2 语音分析结果表
CREATE TABLE `voice_analysis` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分析ID',
    `record_id` BIGINT NOT NULL COMMENT '档案ID',
    `vowel_type` ENUM('A', 'I', 'O', 'U', 'E') COMMENT '元音类型(元音录音时)',
    
    /* 基础参数 */
    `mean_f0` DECIMAL(10,2) COMMENT '平均基频(Hz)',
    `min_f0` DECIMAL(10,2) COMMENT '最低基频(Hz)',
    `max_f0` DECIMAL(10,2) COMMENT '最高基频(Hz)',
    `f0_range` DECIMAL(10,2) COMMENT '音域范围(Hz)',
    
    /* 共振峰 */
    `f1_mean` DECIMAL(10,2) COMMENT 'F1均值(Hz)',
    `f2_mean` DECIMAL(10,2) COMMENT 'F2均值(Hz)',
    `f3_mean` DECIMAL(10,2) COMMENT 'F3均值(Hz)',
    `f4_mean` DECIMAL(10,2) COMMENT 'F4均值(Hz)',
    
    /* 音量 */
    `mean_dB` DECIMAL(10,2) COMMENT '平均响度(dB)',
    `max_dB` DECIMAL(10,2) COMMENT '最大响度(dB)',
    
    /* 频率微扰 */
    `jitter_local` DECIMAL(10,4) COMMENT 'Jitter(local) %',
    `jitter_rap` DECIMAL(10,4) COMMENT 'Jitter(rap) %',
    `jitter_ppq5` DECIMAL(10,4) COMMENT 'Jitter(ppq5) %',
    `jitter_ddp` DECIMAL(10,4) COMMENT 'Jitter(ddp) %',
    
    /* 振幅微扰 */
    `shimmer_local` DECIMAL(10,4) COMMENT 'Shimmer(local) %',
    `shimmer_local_dB` DECIMAL(10,4) COMMENT 'Shimmer(local,dB)',
    `shimmer_apq3` DECIMAL(10,4) COMMENT 'Shimmer(apq3) %',
    `shimmer_apq5` DECIMAL(10,4) COMMENT 'Shimmer(apq5) %',
    
    /* 谐噪比 */
    `hnr` DECIMAL(10,2) COMMENT '谐噪比(dB)',
    `cpp` DECIMAL(10,2) COMMENT '倒谱峰显性(dB)',
    
    /* 颤音 */
    `tremolo_rate` DECIMAL(10,2) COMMENT '颤音频率(Hz)',
    `tremolo_extent` DECIMAL(10,2) COMMENT '颤音振幅',
    
    /* 评分 */
    `overall_score` DECIMAL(5,2) COMMENT '综合评分(0-100)',
    `pitch_score` DECIMAL(5,2) COMMENT '音高评分',
    `timbre_score` DECIMAL(5,2) COMMENT '音色评分',
    `stability_score` DECIMAL(5,2) COMMENT '稳定性评分',
    
    /* AI建议 */
    `ai_suggestion` TEXT COMMENT 'AI改进建议',
    `ai_comparison` TEXT COMMENT '与标准对比(JSON)',
    
    /* 原始数据(JSON) */
    `raw_pitch_contour` JSON COMMENT '音高曲线原始数据',
    `raw_intensity_contour` JSON COMMENT '音量曲线原始数据',
    `raw_spectrogram` JSON COMMENT '频谱数据(可选)',
    
    /* 状态 */
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-无效, 1-有效',
    `error_message` VARCHAR(500) COMMENT '分析失败原因',
    `analyzed_at` DATETIME COMMENT '分析完成时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_record_id` (`record_id`),
    KEY `idx_record_id` (`record_id`),
    KEY `idx_overall_score` (`overall_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='语音分析结果表';

-- ============================================================
-- 3. 验证码与认证
-- ============================================================

-- 3.1 短信验证码表
CREATE TABLE `sms_codes` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `code` VARCHAR(10) NOT NULL COMMENT '验证码',
    `type` ENUM('LOGIN', 'REGISTER', 'RESET_PWD', 'BIND_PHONE') NOT NULL COMMENT '验证码类型',
    `expired_at` DATETIME NOT NULL COMMENT '过期时间',
    `used_at` DATETIME COMMENT '使用时间',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-未使用, 1-已使用, 2-已过期',
    `ip` VARCHAR(50) COMMENT '请求IP',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_phone_type` (`phone`, `type`),
    KEY `idx_expired_at` (`expired_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='短信验证码表';

-- 3.2 用户Token表
CREATE TABLE `user_tokens` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `token` VARCHAR(128) NOT NULL COMMENT 'Token',
    `token_type` ENUM('ACCESS', 'REFRESH') NOT NULL DEFAULT 'ACCESS' COMMENT 'Token类型',
    `device_info` VARCHAR(200) COMMENT '设备信息',
    `ip` VARCHAR(50) COMMENT '登录IP',
    `expired_at` DATETIME NOT NULL COMMENT '过期时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_token` (`token`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_expired_at` (`expired_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户Token表';

-- ============================================================
-- 4. 系统配置
-- ============================================================

-- 4.1 系统配置表
CREATE TABLE `system_configs` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `config_type` VARCHAR(50) COMMENT '配置类型',
    `description` VARCHAR(200) COMMENT '描述',
    `is_public` TINYINT NOT NULL DEFAULT 0 COMMENT '是否公开',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- ============================================================
-- 5. 索引优化建议
-- ============================================================

-- 档案查询常用索引
CREATE INDEX idx_voice_records_student_date ON voice_records(student_id, record_at DESC);
CREATE INDEX idx_voice_records_teacher_date ON voice_records(teacher_id, record_at DESC);
CREATE INDEX idx_voice_analysis_score ON voice_analysis(overall_score);

-- ============================================================
-- 6. 初始化数据
-- ============================================================

-- 插入默认系统配置
INSERT INTO `system_configs` (`config_key`, `config_value`, `config_type`, `description`, `is_public`) VALUES
('sms.enabled', 'true', 'BOOLEAN', '是否启用短信验证码', 0),
('sms.expire_minutes', '5', 'INT', '验证码有效期(分钟)', 0),
('sms.max_daily', '10', 'INT', '每日最大发送次数', 0),
('token.access_expire_hours', '2', 'INT', 'AccessToken有效期(小时)', 0),
('token.refresh_expire_days', '7', 'INT', 'RefreshToken有效期(天)', 0),
('voice.max_duration_seconds', '120', 'INT', '最大录音时长(秒)', 1),
('voice.allowed_formats', 'wav,flac,mp3,m4a', 'STRING', '支持的音频格式', 1),
('teacher.max_students_default', '100', 'INT', '教师默认最大学生数', 0),
('system.version', '1.0.0', 'STRING', '系统版本', 1);

-- ============================================================
-- 完成
-- ============================================================
