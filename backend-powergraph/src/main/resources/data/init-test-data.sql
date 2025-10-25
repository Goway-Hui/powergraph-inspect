-- 初始化测试数据脚本

-- 创建测试用户数据
-- 密码使用BCrypt加密，原始密码为 "password123"

-- 清空现有数据（可选）
-- SET FOREIGN_KEY_CHECKS = 0;
-- TRUNCATE TABLE users;
-- TRUNCATE TABLE stations;
-- TRUNCATE TABLE devices;
-- TRUNCATE TABLE tasks;
-- TRUNCATE TABLE alerts;
-- SET FOREIGN_KEY_CHECKS = 1;

-- 系统管理员 (ADMIN)
INSERT INTO users (username, password, name, role, avatar, created_at, updated_at) VALUES
('admin', '$2a$12$dGGhFrHmhA90sHmSY3dYce2cAdZuRnbszb5b0A5IeboWI2EyFQfae', '系统管理员', 'ADMIN', 'https://avatars.githubusercontent.com/u/123456', NOW(), NOW());

-- 巡检人员 (INSPECTOR)
INSERT INTO users (username, password, name, role, avatar, created_at, updated_at) VALUES
('inspector1', '$2a$12$dGGhFrHmhA90sHmSY3dYce2cAdZuRnbszb5b0A5IeboWI2EyFQfae', '巡检员张三', 'INSPECTOR', 'https://avatars.githubusercontent.com/u/123457', NOW(), NOW()),
('inspector2', '$2a$12$dGGhFrHmhA90sHmSY3dYce2cAdZuRnbszb5b0A5IeboWI2EyFQfae', '巡检员李四', 'INSPECTOR', 'https://avatars.githubusercontent.com/u/123458', NOW(), NOW()),
('inspector3', '$2a$12$dGGhFrHmhA90sHmSY3dYce2cAdZuRnbszb5b0A5IeboWI2EyFQfae', '巡检员王五', 'INSPECTOR', 'https://avatars.githubusercontent.com/u/123459', NOW(), NOW());

-- 只读用户 (VIEWER)
INSERT INTO users (username, password, name, role, avatar, created_at, updated_at) VALUES
('viewer1', '$2a$12$dGGhFrHmhA90sHmSY3dYce2cAdZuRnbszb5b0A5IeboWI2EyFQfae', '观察员甲', 'VIEWER', 'https://avatars.githubusercontent.com/u/123460', NOW(), NOW()),
('viewer2', '$2a$12$dGGhFrHmhA90sHmSY3dYce2cAdZuRnbszb5b0A5IeboWI2EyFQfae', '观察员乙', 'VIEWER', 'https://avatars.githubusercontent.com/u/123461', NOW(), NOW());

-- 创建变电站数据
INSERT INTO stations (name, location, description, status, created_at, updated_at) VALUES
('500kV超高压变电站', '北京市朝阳区', '主要负责市区供电', '正常', NOW(), NOW()),
('220kV枢纽变电站', '上海市浦东新区', '连接多个区域电网', '正常', NOW(), NOW()),
('110kV配电变电站', '广州市天河区', '为商业区供电', '维护中', NOW(), NOW());

-- 创建设备数据
INSERT INTO devices (name, type, status, location, station_id, created_at, updated_at) VALUES
('主变压器#001', '变压器', '正常', 'A区', 1, NOW(), NOW()),
('主变压器#002', '变压器', '正常', 'B区', 1, NOW(), NOW()),
('断路器#001', '断路器', '正常', 'C区', 1, NOW(), NOW()),
('断路器#002', '断路器', '异常', 'D区', 1, NOW(), NOW()),
('隔离开关#001', '隔离开关', '正常', 'E区', 1, NOW(), NOW()),
('避雷器#001', '避雷器', '正常', 'F区', 1, NOW(), NOW()),

('主变压器#101', '变压器', '正常', 'A区', 2, NOW(), NOW()),
('断路器#101', '断路器', '正常', 'B区', 2, NOW(), NOW()),
('隔离开关#101', '隔离开关', '正常', 'C区', 2, NOW(), NOW()),
('互感器#101', '互感器', '正常', 'D区', 2, NOW(), NOW()),

('主变压器#201', '变压器', '维护中', 'A区', 3, NOW(), NOW()),
('断路器#201', '断路器', '正常', 'B区', 3, NOW(), NOW());

-- 创建任务数据
INSERT INTO tasks (title, description, device_id, assignee_id, status, priority, deadline, created_at, updated_at) VALUES
('变压器定期巡检', '检查主变压器#001的运行状态', 1, 2, 'COMPLETED', 'MEDIUM', DATE_ADD(NOW(), INTERVAL 7 DAY), NOW(), NOW()),
('断路器温度检测', '检测断路器#002的温度是否正常', 4, 2, 'IN_PROGRESS', 'HIGH', DATE_ADD(NOW(), INTERVAL 2 DAY), NOW(), NOW()),
('隔离开关维护', '对隔离开关#001进行常规维护', 5, 3, 'PENDING', 'LOW', DATE_ADD(NOW(), INTERVAL 14 DAY), NOW(), NOW()),
('避雷器绝缘测试', '测试避雷器#001的绝缘性能', 6, 4, 'PENDING', 'MEDIUM', DATE_ADD(NOW(), INTERVAL 10 DAY), NOW(), NOW()),
('变压器油样分析', '对主变压器#101进行油样分析', 7, 2, 'COMPLETED', 'HIGH', DATE_ADD(NOW(), INTERVAL 5 DAY), NOW(), NOW());

-- 创建告警数据
INSERT INTO alerts (device_id, level, content, status, assigned_to, created_at, updated_at) VALUES
(4, 'HIGH', '断路器#002温度过高，超过阈值85°C', 'PENDING', 2, NOW(), NOW()),
(6, 'MEDIUM', '避雷器#001接地电阻略有增大', 'PROCESSING', 3, DATE_ADD(NOW(), INTERVAL -1 HOUR), DATE_ADD(NOW(), INTERVAL -30 MINUTE)),
(12, 'HIGH', '主变压器#201正在进行维护，暂停操作', 'RESOLVED', 4, DATE_ADD(NOW(), INTERVAL -2 DAY), NOW());

-- 角色初始数据
INSERT INTO roles (name, code, description, status, permissions, created_at, updated_at) VALUES
('系统管理员','ADMIN','系统最高权限','启用','["device:view","device:edit","task:view","task:assign","alert:view","alert:handle","alert:rule","graph:view","graph:analysis","user:view","user:edit"]', NOW(), NOW()),
('巡检员','INSPECTOR','执行巡检任务','启用','["device:view","task:view","alert:view"]', NOW(), NOW()),
('观察员','VIEWER','只读查看','禁用','["device:view","task:view","alert:view","graph:view"]', NOW(), NOW());

-- 部门初始数据
INSERT INTO departments (name, parent_id, leader, status, description, created_at, updated_at) VALUES
('总部', NULL, NULL, '启用', '公司总部', NOW(), NOW()),
('研发中心', 1, '张三', '启用', NULL, NOW(), NOW()),
('后端组', 2, '李四', '启用', NULL, NOW(), NOW()),
('前端组', 2, '王五', '禁用', NULL, NOW(), NOW()),
('运维中心', 1, '赵六', '启用', NULL, NOW(), NOW());

-- 查询验证数据
SELECT 'Users Count' as Table_Name, COUNT(*) as Count FROM users
UNION ALL
SELECT 'Stations Count' as Table_Name, COUNT(*) as Count FROM stations
UNION ALL
SELECT 'Devices Count' as Table_Name, COUNT(*) as Count FROM devices
UNION ALL
SELECT 'Tasks Count' as Table_Name, COUNT(*) as Count FROM tasks
UNION ALL
SELECT 'Alerts Count' as Table_Name, COUNT(*) as Count FROM alerts
UNION ALL
SELECT 'Roles Count' as Table_Name, COUNT(*) as Count FROM roles
UNION ALL
SELECT 'Departments Count' as Table_Name, COUNT(*) as Count FROM departments;