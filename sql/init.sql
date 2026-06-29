-- ============================================
-- Java从入门到精通实战项目 - 数据库初始化脚本
-- ============================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS java_mastery 
    DEFAULT CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE java_mastery;

-- ============================================
-- 1. 用户表
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `phone` VARCHAR(20) COMMENT '手机号',
    `email` VARCHAR(100) COMMENT '邮箱',
    `avatar` VARCHAR(255) COMMENT '头像URL',
    `gender` TINYINT DEFAULT 0 COMMENT '性别 0未知 1男 2女',
    `age` INT COMMENT '年龄',
    `status` TINYINT DEFAULT 1 COMMENT '状态 1正常 0禁用',
    `last_login_time` DATETIME COMMENT '最后登录时间',
    `create_by` BIGINT COMMENT '创建人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` BIGINT COMMENT '更新人',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
    INDEX `idx_username` (`username`),
    INDEX `idx_phone` (`phone`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB COMMENT='系统用户表';

-- ============================================
-- 2. 角色表
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_role` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `code` VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码(ROLE_ADMIN)',
    `description` VARCHAR(200) COMMENT '描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态 1启用 0禁用',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_code` (`code`)
) ENGINE=InnoDB COMMENT='角色表';

-- ============================================
-- 3. 权限表（菜单+按钮）
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_permission` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父级ID',
    `name` VARCHAR(50) NOT NULL COMMENT '权限名称',
    `code` VARCHAR(100) COMMENT '权限标识(sys:user:add)',
    `type` TINYINT NOT NULL COMMENT '类型 1目录 2菜单 3按钮',
    `path` VARCHAR(200) COMMENT '路由路径',
    `component` VARCHAR(200) COMMENT '组件路径',
    `icon` VARCHAR(50) COMMENT '图标',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_parent` (`parent_id`),
    INDEX `idx_code` (`code`)
) ENGINE=InnoDB COMMENT='权限表';

-- ============================================
-- 4. 用户-角色关联表
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`user_id`, `role_id`),
    INDEX `idx_role` (`role_id`)
) ENGINE=InnoDB COMMENT='用户角色关联表';

-- ============================================
-- 5. 角色-权限关联表
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    PRIMARY KEY (`role_id`, `permission_id`),
    INDEX `idx_perm` (`permission_id`)
) ENGINE=InnoDB COMMENT='角色权限关联表';

-- ============================================
-- 6. 商品分类表
-- ============================================
CREATE TABLE IF NOT EXISTS `product_category` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `icon` VARCHAR(255) COMMENT '图标',
    `sort` INT DEFAULT 0,
    `status` TINYINT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_parent` (`parent_id`)
) ENGINE=InnoDB COMMENT='商品分类表';

-- ============================================
-- 7. 商品表
-- ============================================
CREATE TABLE IF NOT EXISTS `product` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(200) NOT NULL COMMENT '商品名称',
    `description` TEXT COMMENT '商品描述',
    `category_id` BIGINT COMMENT '分类ID',
    `price` DECIMAL(10,2) NOT NULL COMMENT '单价',
    `stock` INT NOT NULL DEFAULT 0 COMMENT '库存',
    `images` JSON COMMENT '商品图片JSON数组',
    `status` TINYINT DEFAULT 1 COMMENT '1上架 0下架',
    `sales` INT DEFAULT 0 COMMENT '销量',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_category` (`category_id`),
    INDEX `idx_status` (`status`),
    FULLTEXT INDEX `ft_name` (`name`, `description`)
) ENGINE=InnoDB COMMENT='商品表';

-- ============================================
-- 8. 订单表
-- ============================================
CREATE TABLE IF NOT EXISTS `mall_order` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `order_no` VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '总金额',
    `pay_amount` DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    `discount_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '优惠金额',
    `pay_type` VARCHAR(20) COMMENT '支付方式 ALIPAY/WECHAT',
    `status` VARCHAR(20) NOT NULL COMMENT 'PENDING/PAID/SHIPPING/DELIVERED/CANCELLED/REFUNDED',
    `receiver_name` VARCHAR(50) COMMENT '收货人',
    `receiver_phone` VARCHAR(20) COMMENT '收货电话',
    `receiver_address` VARCHAR(255) COMMENT '收货地址',
    `remark` VARCHAR(500) COMMENT '备注',
    `pay_time` DATETIME COMMENT '支付时间',
    `deliver_time` DATETIME COMMENT '发货时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_order_no` (`order_no`),
    INDEX `idx_user` (`user_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB COMMENT='订单表';

-- ============================================
-- 9. 订单明细表
-- ============================================
CREATE TABLE IF NOT EXISTS `mall_order_item` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(200) COMMENT '商品名称(冗余)',
    `product_image` VARCHAR(255) COMMENT '商品图片(冗余)',
    `price` DECIMAL(10,2) NOT NULL COMMENT '单价',
    `quantity` INT NOT NULL COMMENT '数量',
    `total_price` DECIMAL(10,2) NOT NULL COMMENT '小计',
    INDEX `idx_order` (`order_id`),
    FOREIGN KEY (`order_id`) REFERENCES `mall_order`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='订单明细表';

-- ============================================
-- 10. 购物车表
-- ============================================
CREATE TABLE IF NOT EXISTS `cart_item` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
    `checked` TINYINT DEFAULT 1 COMMENT '1选中 0未选',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`)
) ENGINE=InnoDB COMMENT='购物车表';

-- ============================================
-- 11. 操作日志表
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_oper_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT COMMENT '操作用户',
    `username` VARCHAR(50) COMMENT '用户名',
    `module` VARCHAR(50) COMMENT '操作模块',
    `action` VARCHAR(50) COMMENT '操作类型',
    `method` VARCHAR(200) COMMENT '请求方法',
    `url` VARCHAR(200) COMMENT '请求URL',
    `ip` VARCHAR(50) COMMENT 'IP地址',
    `params` TEXT COMMENT '请求参数',
    `result` TEXT COMMENT '响应结果',
    `error_msg` TEXT COMMENT '错误信息',
    `cost_time` BIGINT COMMENT '耗时(毫秒)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_user` (`user_id`),
    INDEX `idx_time` (`create_time`)
) ENGINE=InnoDB COMMENT='操作日志表';

-- ============================================
-- 12. 文件上传记录表
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_file` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `original_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `file_name` VARCHAR(255) NOT NULL COMMENT '存储文件名',
    `file_path` VARCHAR(500) NOT NULL COMMENT '存储路径',
    `file_size` BIGINT COMMENT '文件大小(字节)',
    `file_type` VARCHAR(50) COMMENT '文件类型',
    `md5` VARCHAR(32) COMMENT '文件MD5',
    `create_by` BIGINT,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_md5` (`md5`)
) ENGINE=InnoDB COMMENT='文件表';

-- ============================================
-- 初始化数据
-- ============================================

-- 管理员用户（开发环境默认密码: admin123）
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `phone`, `email`, `status`) VALUES
('admin', '$2a$10$dzpY/XHgvJB1fCrRxnRMrOCqFB1pF8xjFJtMNKiZ.UJ4SRPyXjeVC', '系统管理员', '13800138000', 'admin@mastery.com', 1),
('zhangsan', '$2a$10$dzpY/XHgvJB1fCrRxnRMrOCqFB1pF8xjFJtMNKiZ.UJ4SRPyXjeVC', '张三', '13800138001', 'zhangsan@mastery.com', 1),
('lisi', '$2a$10$dzpY/XHgvJB1fCrRxnRMrOCqFB1pF8xjFJtMNKiZ.UJ4SRPyXjeVC', '李四', '13800138002', 'lisi@mastery.com', 1);

-- 角色
INSERT INTO `sys_role` (`name`, `code`, `description`, `sort`) VALUES
('超级管理员', 'ROLE_ADMIN', '拥有所有权限', 1),
('普通用户', 'ROLE_USER', '普通用户权限', 2);

-- 权限（菜单+按钮）
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `code`, `type`, `path`, `icon`, `sort`) VALUES
(1, 0, '系统管理', '', 1, '/sys', 'Setting', 1),
(2, 1, '用户管理', 'sys:user:list', 2, '/sys/user', 'User', 1),
(3, 2, '新增用户', 'sys:user:add', 3, '', '', 1),
(4, 2, '修改用户', 'sys:user:edit', 3, '', '', 2),
(5, 2, '删除用户', 'sys:user:delete', 3, '', '', 3),
(6, 1, '角色管理', 'sys:role:list', 2, '/sys/role', 'UserFilled', 2),
(11, 6, '新增角色', 'sys:role:add', 3, '', '', 3),
(12, 6, '修改角色', 'sys:role:edit', 3, '', '', 4),
(13, 6, '删除角色', 'sys:role:delete', 3, '', '', 5),
(7, 0, '商品管理', '', 1, '/product', 'Goods', 2),
(8, 7, '商品列表', 'product:list', 2, '/product/list', 'List', 1),
(9, 7, '分类管理', 'product:category', 2, '/product/category', 'Grid', 2),
(10, 0, '订单管理', '', 1, '/order', 'Tickets', 3),
(11, 10, '订单列表', 'order:list', 2, '/order/list', 'List', 1);

-- 角色-权限关联
INSERT INTO `sys_role_permission` VALUES
(1, 1),(1, 2),(1, 3),(1, 4),(1, 5),(1, 6),
(1, 7),(1, 8),(1, 9),(1, 10),(1, 11),(1, 12),(1, 13),
(2, 7),(2, 8),(2, 10),(2, 11);

-- 用户-角色关联
INSERT INTO `sys_user_role` VALUES (1, 1), (2, 2), (3, 2);

-- 商品分类
INSERT INTO `product_category` (`id`, `parent_id`, `name`, `sort`) VALUES
(1, 0, '电子产品', 1),
(2, 1, '手机', 1),
(3, 1, '电脑', 2),
(4, 0, '服装鞋帽', 2),
(5, 4, '男装', 1),
(6, 4, '女装', 2);

-- 商品数据
INSERT INTO `product` (`name`, `category_id`, `price`, `stock`, `images`, `status`) VALUES
('iPhone 16 Pro Max', 2, 9999.00, 100, '["https://img.example.com/iphone1.jpg"]', 1),
('MacBook Pro 16 M4', 3, 19999.00, 50, '["https://img.example.com/macbook1.jpg"]', 1),
('华为 MateBook X Pro', 3, 8999.00, 80, '["https://img.example.com/matebook1.jpg"]', 1),
('AirPods Pro 2', 1, 1899.00, 500, '["https://img.example.com/airpods1.jpg"]', 1),
('海尔智能冰箱', 1, 3999.00, 30, '["https://img.example.com/fridge1.jpg"]', 1);
