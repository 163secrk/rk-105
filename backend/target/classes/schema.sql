-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    status INTEGER DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    role_name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 菜单表
CREATE TABLE IF NOT EXISTS sys_menu (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    menu_name VARCHAR(50) NOT NULL,
    menu_path VARCHAR(100),
    menu_icon VARCHAR(50),
    parent_id INTEGER DEFAULT 0,
    sort_order INTEGER DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    role_id INTEGER NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    role_id INTEGER NOT NULL,
    menu_id INTEGER NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 人才表
CREATE TABLE IF NOT EXISTS res_talent (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(50) NOT NULL,
    gender VARCHAR(10),
    phone VARCHAR(20),
    email VARCHAR(100),
    level VARCHAR(20) NOT NULL,
    monthly_salary DECIMAL(12,2),
    tech_stack TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'IDLE',
    remark TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 项目表
CREATE TABLE IF NOT EXISTS res_project (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    project_name VARCHAR(100) NOT NULL,
    client_name VARCHAR(100),
    contact_person VARCHAR(50),
    contact_phone VARCHAR(20),
    price_junior DECIMAL(12,2),
    price_middle DECIMAL(12,2),
    price_senior DECIMAL(12,2),
    remark TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 指派记录表
CREATE TABLE IF NOT EXISTS res_assignment (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    talent_id INTEGER NOT NULL,
    talent_name VARCHAR(50),
    project_id INTEGER NOT NULL,
    project_name VARCHAR(100),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    talent_level VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    remark TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 工时单表
CREATE TABLE IF NOT EXISTS ts_timesheet (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    user_name VARCHAR(50),
    talent_id INTEGER,
    talent_name VARCHAR(50),
    project_id INTEGER NOT NULL,
    project_name VARCHAR(100),
    month VARCHAR(7) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    approver_id INTEGER,
    approver_name VARCHAR(50),
    approve_time DATETIME,
    reject_reason TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 工时单每日明细表
CREATE TABLE IF NOT EXISTS ts_timesheet_day (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    timesheet_id INTEGER NOT NULL,
    day_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NORMAL'
);

-- 项目PM关联表
CREATE TABLE IF NOT EXISTS res_project_pm (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id INTEGER NOT NULL,
    pm_user_id INTEGER NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
