-- 角色初始化（与后端实体 Role.java 一致）
CREATE TABLE IF NOT EXISTS `roles` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `code` VARCHAR(255) NOT NULL,
  `description` VARCHAR(255),
  `status` VARCHAR(255) NOT NULL,
  `permissions` TEXT,
  `created_at` DATETIME(6),
  `updated_at` DATETIME(6),
  UNIQUE KEY `uk_roles_code` (`code`),
  KEY `idx_roles_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 基础系统角色（如已存在则忽略）
INSERT INTO `roles` (`name`, `code`, `description`, `status`, `permissions`, `created_at`, `updated_at`)
SELECT '系统管理员', 'ADMIN', '系统管理与配置', '启用',
       '["device:view","device:edit","task:view","task:assign","alert:view","alert:handle","alert:rule","graph:view","graph:analysis","user:view","user:edit"]',
       NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `roles` WHERE `code`='ADMIN');

INSERT INTO `roles` (`name`, `code`, `description`, `status`, `permissions`, `created_at`, `updated_at`)
SELECT '巡检员', 'INSPECTOR', '现场巡检与处理', '启用',
       '["device:view","task:view","task:assign","alert:view","alert:handle"]',
       NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `roles` WHERE `code`='INSPECTOR');

INSERT INTO `roles` (`name`, `code`, `description`, `status`, `permissions`, `created_at`, `updated_at`)
SELECT '观察员', 'VIEWER', '只读查看权限', '启用',
       '["device:view","task:view","alert:view","graph:view"]',
       NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `roles` WHERE `code`='VIEWER');

-- 扩展常用角色
INSERT INTO `roles` (`name`, `code`, `description`, `status`, `permissions`, `created_at`, `updated_at`)
SELECT '维保员', 'MAINTAINER', '设备维护保养', '启用',
       '["device:view","device:edit","task:view","task:assign","alert:view"]',
       NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `roles` WHERE `code`='MAINTAINER');

INSERT INTO `roles` (`name`, `code`, `description`, `status`, `permissions`, `created_at`, `updated_at`)
SELECT '调度员', 'DISPATCHER', '任务调度与分配', '启用',
       '["task:view","task:assign","device:view","alert:view"]',
       NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `roles` WHERE `code`='DISPATCHER');