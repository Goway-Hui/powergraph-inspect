-- 建表：告警规则（与后端实体 AlertRule.java 对齐）
CREATE TABLE IF NOT EXISTS `alert_rules` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `level` VARCHAR(50),
  `condition` VARCHAR(1000),
  `scope` VARCHAR(255),
  `notify_channels` VARCHAR(255),
  `enabled` TINYINT(1) NOT NULL DEFAULT 1,
  `created_at` DATETIME,
  `updated_at` DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 初始化示例数据（贴合项目常见设备与场景）
INSERT INTO `alert_rules` (`name`, `level`, `condition`, `scope`, `notify_channels`, `enabled`, `created_at`, `updated_at`) VALUES
('温度过高', '高', '温度>85°C 持续5分钟', '断路器', '站内信,邮件', 1, NOW(), NOW()),
('油压异常', '中', '油压<设定阈值', '变压器', '站内信', 1, NOW(), NOW()),
('振动超标', '严重', '轴承振动>30mm/s', '电机', '站内信,短信', 1, NOW(), NOW()),
('烟雾检测', '紧急', '烟雾浓度>报警值', '开关柜', '站内信,短信,邮件', 1, NOW(), NOW()),
('电压偏低', '一般', '母线电压<额定值-10%', '母线', '站内信', 1, NOW(), NOW()),
('电流不平衡', '中', '三相电流不平衡>20%', '馈线', '站内信,邮件', 1, NOW(), NOW());