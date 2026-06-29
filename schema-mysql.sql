CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    avatar VARCHAR(255),
    gender TINYINT DEFAULT 0,
    age INT,
    status TINYINT DEFAULT 1,
    last_login_time DATETIME,
    create_by BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50),
    code VARCHAR(50) UNIQUE,
    description VARCHAR(200),
    status TINYINT DEFAULT 1,
    sort INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT DEFAULT 0,
    name VARCHAR(50),
    code VARCHAR(100),
    type TINYINT,
    path VARCHAR(200),
    component VARCHAR(200),
    icon VARCHAR(50),
    sort INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY(user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY(role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT DEFAULT 0,
    name VARCHAR(50),
    icon VARCHAR(255),
    sort INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200),
    description TEXT,
    category_id BIGINT,
    price DECIMAL(10,2),
    stock INT DEFAULT 0,
    images VARCHAR(1000),
    status TINYINT DEFAULT 1,
    sales INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS mall_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(32) UNIQUE,
    user_id BIGINT,
    total_amount DECIMAL(10,2),
    pay_amount DECIMAL(10,2),
    discount_amount DECIMAL(10,2) DEFAULT 0,
    pay_type VARCHAR(20),
    status VARCHAR(20),
    receiver_name VARCHAR(50),
    receiver_phone VARCHAR(20),
    receiver_address VARCHAR(255),
    remark VARCHAR(500),
    pay_time DATETIME,
    deliver_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS mall_order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(200),
    product_image VARCHAR(255),
    price DECIMAL(10,2),
    quantity INT,
    total_price DECIMAL(10,2)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS cart_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    product_id BIGINT,
    quantity INT DEFAULT 1,
    checked TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_file (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    original_name VARCHAR(255),
    file_name VARCHAR(255),
    file_path VARCHAR(500),
    file_size BIGINT,
    file_type VARCHAR(50),
    md5 VARCHAR(32),
    create_by BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ====== SEED DATA ======
INSERT INTO sys_user VALUES
(1,'admin','admin123','Admin','13800138000','admin@mastery.com',NULL,0,NULL,1,NULL,NULL,NOW(),NULL,NOW(),0),
(2,'zhangsan','admin123','ZhangSan','13800138001','zs@mastery.com',NULL,0,NULL,1,NULL,NULL,NOW(),NULL,NOW(),0),
(3,'lisi','admin123','LiSi','13800138002','ls@mastery.com',NULL,0,NULL,1,NULL,NULL,NOW(),NULL,NOW(),0);

INSERT INTO sys_role VALUES
(1,'Admin','ROLE_ADMIN','Full access',1,1,NOW(),NOW()),
(2,'User','ROLE_USER','Basic access',1,2,NOW(),NOW());

INSERT INTO sys_permission VALUES
(1,0,'System','',1,'/sys',NULL,'Setting',1,1,NOW()),
(2,1,'Users','sys:user:list',2,'/sys/user',NULL,'User',1,1,NOW()),
(3,2,'Add','sys:user:add',3,NULL,NULL,NULL,1,1,NOW()),
(4,2,'Edit','sys:user:edit',3,NULL,NULL,NULL,2,1,NOW()),
(5,2,'Delete','sys:user:delete',3,NULL,NULL,NULL,3,1,NOW()),
(6,1,'Roles','sys:role:list',2,'/sys/role',NULL,'UserFilled',2,1,NOW()),
(14,6,'RoleAdd','sys:role:add',3,NULL,NULL,NULL,3,1,NOW()),
(15,6,'RoleEdit','sys:role:edit',3,NULL,NULL,NULL,4,1,NOW()),
(16,6,'RoleDel','sys:role:delete',3,NULL,NULL,NULL,5,1,NOW()),
(7,0,'Products','',1,'/product',NULL,'Goods',2,1,NOW()),
(8,7,'List','product:list',2,'/product/list',NULL,'List',1,1,NOW()),
(9,7,'Category','product:category',2,'/product/category',NULL,'Grid',2,1,NOW()),
(10,0,'Orders','',1,'/order',NULL,'Tickets',3,1,NOW()),
(11,10,'List','order:list',2,'/order/list',NULL,'List',1,1,NOW());

INSERT INTO sys_role_permission VALUES
(1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),(1,10),(1,11),(1,14),(1,15),(1,16),
(2,7),(2,8),(2,10),(2,11);

INSERT INTO sys_user_role VALUES (1,1),(2,2),(3,2);

INSERT INTO product_category VALUES
(1,0,'Electronics',NULL,1,1,NOW()),
(2,1,'Phones',NULL,1,1,NOW()),
(3,1,'Computers',NULL,2,1,NOW()),
(4,1,'Accessories',NULL,3,1,NOW()),
(5,0,'Clothing',NULL,2,1,NOW());

INSERT INTO product VALUES
(1,'iPhone 16 Pro Max','Apple flagship',2,9999.00,100,'[]',1,0,NOW(),NOW(),0),
(2,'MacBook Pro 16 M4','Pro laptop',3,19999.00,50,'[]',1,0,NOW(),NOW(),0),
(3,'Huawei MateBook X Pro','Ultra-thin',3,8999.00,80,'[]',1,0,NOW(),NOW(),0),
(4,'AirPods Pro 2','ANC earbuds',4,1899.00,500,'[]',1,0,NOW(),NOW(),0),
(5,'Xiaomi 14 Ultra','Leica phone',2,5999.00,200,'[]',1,0,NOW(),NOW(),0),
(6,'iPad Pro M4','Tablet',3,7999.00,60,'[]',1,0,NOW(),NOW(),0),
(7,'Sony WH-1000XM5','Headphones',4,2499.00,150,'[]',1,0,NOW(),NOW(),0),
(8,'Samsung Galaxy S24','AI phone',2,6999.00,120,'[]',1,0,NOW(),NOW(),0),
(9,'Dell XPS 15','Laptop',3,12999.00,40,'[]',1,0,NOW(),NOW(),0),
(10,'Apple Watch Ultra','Watch',4,6499.00,80,'[]',1,0,NOW(),NOW(),0),
(11,'Nike Air Max','Shoes',6,899.00,300,'[]',1,0,NOW(),NOW(),0),
(12,'Adidas Ultraboost','Shoes',6,1299.00,200,'[]',1,0,NOW(),NOW(),0);

INSERT INTO mall_order VALUES
(1,'OM20240621001',2,19999.00,19999.00,0,NULL,'PAID','ZhangSan','13800138001','Beijing',NULL,'2024-06-21 10:00:00','2024-06-21 10:00:00','2024-06-21 10:00:00','2024-06-21 10:00:00',0),
(2,'OM20240622001',3,9999.00,9999.00,0,NULL,'PAID','LiSi','13800138002','Shanghai',NULL,'2024-06-22 14:00:00','2024-06-22 14:00:00','2024-06-22 14:00:00','2024-06-22 14:00:00',0),
(3,'OM20240623001',2,8999.00,8999.00,0,NULL,'PAID','ZhangSan','13800138001','Beijing',NULL,'2024-06-23 09:00:00','2024-06-23 09:00:00','2024-06-23 09:00:00','2024-06-23 09:00:00',0),
(4,'OM20240624001',1,1899.00,1899.00,0,NULL,'PAID','Admin','13800138000','Shenzhen',NULL,'2024-06-24 16:00:00','2024-06-24 16:00:00','2024-06-24 16:00:00','2024-06-24 16:00:00',0),
(5,'OM20240625001',2,5999.00,5999.00,0,NULL,'PAID','ZhangSan','13800138001','Beijing',NULL,'2024-06-25 11:00:00','2024-06-25 11:00:00','2024-06-25 11:00:00','2024-06-25 11:00:00',0),
(6,'OM20240626001',3,2499.00,2499.00,0,NULL,'PAID','LiSi','13800138002','Shanghai',NULL,'2024-06-26 08:00:00','2024-06-26 08:00:00','2024-06-26 08:00:00','2024-06-26 08:00:00',0),
(7,'OM20240627001',1,7999.00,7999.00,0,NULL,'PAID','Admin','13800138000','Shenzhen',NULL,'2024-06-27 12:00:00','2024-06-27 12:00:00','2024-06-27 12:00:00','2024-06-27 12:00:00',0),
(8,'OM20240627002',2,6999.00,6999.00,0,NULL,'PENDING','ZhangSan','13800138001','Beijing',NULL,NULL,NULL,NOW(),NOW(),0),
(9,'OM20240627003',3,12999.00,12999.00,0,NULL,'PENDING','LiSi','13800138002','Shanghai',NULL,NULL,NULL,NOW(),NOW(),0),
(10,'OM20240627004',1,6499.00,6499.00,0,NULL,'CANCELLED','Admin','13800138000','Shenzhen',NULL,NULL,NULL,NOW(),NOW(),0),
(11,'OM20240620001',2,899.00,899.00,0,NULL,'DELIVERED','ZhangSan','13800138001','Beijing',NULL,'2024-06-20 13:00:00','2024-06-20 13:00:00','2024-06-20 13:00:00','2024-06-20 13:00:00',0),
(12,'OM20240620002',3,1299.00,1299.00,0,NULL,'DELIVERED','LiSi','13800138002','Shanghai',NULL,'2024-06-20 13:00:00','2024-06-20 13:00:00','2024-06-20 13:00:00','2024-06-20 13:00:00',0);
