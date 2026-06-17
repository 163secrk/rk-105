"""
rk-105 Talent-Bridge 完整种子数据
用法: python seed.py
前提: 后端至少启动过一次（数据库文件已自动创建）
"""
import sqlite3
import os
import bcrypt
from datetime import date, datetime

DB_PATH = os.path.join(os.path.dirname(__file__), "backend", "data", "talent_bridge.db")

def hash_pwd(pwd: str = "123456") -> str:
    return bcrypt.hashpw(pwd.encode(), bcrypt.gensalt(rounds=10)).decode()

def conn():
    c = sqlite3.connect(DB_PATH)
    c.execute("PRAGMA foreign_keys = ON")
    return c

def seed(c: sqlite3.Connection):
    hp = hash_pwd()
    now = "2026-06-17 00:00:00"

    # ============================
    # 1. 额外用户 (外派员工) — sys_user
    # ============================
    employees = [
        (6, "zhangwei", hp, "张伟", "zhangwei@example.com", "13800000001", 1),
        (7, "liming",   hp, "李明", "liming@example.com",   "13800000002", 1),
        (8, "wangfang", hp, "王芳", "wangfang@example.com", "13800000003", 1),
        (9, "zhaoming", hp, "赵明", "zhaoming@example.com", "13800000004", 1),
        (10,"chenjing", hp, "陈静", "chenjing@example.com", "13800000005", 1),
    ]
    for e in employees:
        c.execute("INSERT OR IGNORE INTO sys_user(id,username,password,real_name,email,phone,status,create_time,update_time) VALUES(?,?,?,?,?,?,?,?,?)",
                  (*e, now, now))
        c.execute("INSERT OR IGNORE INTO sys_user_role(user_id,role_id,create_time) VALUES(?,5,?)", (e[0], now))

    # ============================
    # 2. 人才库 — res_talent (5人: 3在项 + 2待岗)
    # ============================
    talents = [
        (101, "张伟", "男", "13800000001", "zhangwei@example.com", "SENIOR", 32000.00, "Java,Spring,K8s,Docker", "ON_PROJECT", "10年架构经验", now, now),
        (102, "李明", "男", "13800000002", "liming@example.com",     "MIDDLE", 21000.00, "Vue,React,TypeScript",  "ON_PROJECT", "5年前端经验",   now, now),
        (103, "王芳", "女", "13800000003", "wangfang@example.com",   "JUNIOR", 12000.00, "Python,Django,Flask",    "ON_PROJECT", "2年后端经验",   now, now),
        (104, "赵明", "男", "13800000004", "zhaoming@example.com",   "SENIOR", 35000.00, "Go,K8s,Istio,gRPC",      "IDLE",       "8年云原生经验", now, now),
        (105, "陈静", "女", "13800000005", "chenjing@example.com",   "MIDDLE", 18000.00, "Angular,Node.js,Express","IDLE",       "4年全栈经验",   now, now),
    ]
    c.executemany("INSERT OR IGNORE INTO res_talent(id,name,gender,phone,email,level,monthly_salary,tech_stack,status,remark,create_time,update_time) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)", talents)

    # ============================
    # 3. 项目池 — res_project (3个项目)
    # ============================
    projects = [
        (1, "华为云平台迁移项目", "华为", "王经理", "13900000001", 18000.00, 28000.00, 40000.00, "云迁移二期", now, now),
        (2, "阿里数据中心项目",   "阿里", "李经理", "13900000002", 15000.00, 23000.00, 32000.00, "数据中心运维", now, now),
        (3, "腾讯智慧园区项目",   "腾讯", "赵经理", "13900000003", 16000.00, 25000.00, 38000.00, "智慧园区平台", "2026-06-01 00:00:00", now),
    ]
    c.executemany("INSERT OR IGNORE INTO res_project(id,project_name,client_name,contact_person,contact_phone,price_junior,price_middle,price_senior,remark,create_time,update_time) VALUES(?,?,?,?,?,?,?,?,?,?,?)", projects)

    # PM映射 (project 1已在DataInitializer中绑定PM user 4)
    c.execute("INSERT OR IGNORE INTO res_project_pm(project_id,pm_user_id,create_time) VALUES(2,4,?)", (now,))
    c.execute("INSERT OR IGNORE INTO res_project_pm(project_id,pm_user_id,create_time) VALUES(3,4,?)", (now,))

    # ============================
    # 4. 指派记录 — res_assignment (3条)
    # ============================
    assignments = [
        (301, 101, "张伟", 1, "华为云平台迁移项目", "2026-01-01", "2026-12-31", 40000.00, "SENIOR", "ACTIVE", "", now, now),
        (302, 102, "李明", 1, "华为云平台迁移项目", "2026-03-01", "2026-12-31", 28000.00, "MIDDLE", "ACTIVE", "", now, now),
        (303, 103, "王芳", 2, "阿里数据中心项目",   "2026-04-01", "2026-12-31", 15000.00, "JUNIOR", "ACTIVE", "", now, now),
    ]
    c.executemany("INSERT OR IGNORE INTO res_assignment(id,talent_id,talent_name,project_id,project_name,start_date,end_date,unit_price,talent_level,status,remark,create_time,update_time) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)", assignments)

    # ============================
    # 5. 法定工作日 — fin_workday_setting (1-6月)
    # ============================
    workdays = [
        ("2026-01", 19, "1月", now, now),
        ("2026-02", 17, "2月(春节)", now, now),
        ("2026-03", 22, "3月", now, now),
        ("2026-04", 22, "4月", now, now),
        ("2026-05", 20, "5月(劳动节)", now, now),
        ("2026-06", 21, "6月(端午节)", now, now),
    ]
    c.executemany("INSERT OR IGNORE INTO fin_workday_setting(month,workday_count,remark,create_time,update_time) VALUES(?,?,?,?,?)", workdays)

    # ============================
    # 6. 工时单 + 日历明细 (每人每月1条)
    # data: (user_id, talent_id, talent_name, project_id, project_name, month, start_day, end_day)
    # ============================
    timesheet_configs = [
        # 张伟: 1-6月全勤, 华为项目
        (6, 101, "张伟", 1, "华为云平台迁移项目", "2026-01", 1, 19),
        (6, 101, "张伟", 1, "华为云平台迁移项目", "2026-02", 1, 17),
        (6, 101, "张伟", 1, "华为云平台迁移项目", "2026-03", 1, 22),
        (6, 101, "张伟", 1, "华为云平台迁移项目", "2026-04", 1, 22),
        (6, 101, "张伟", 1, "华为云平台迁移项目", "2026-05", 1, 20),
        (6, 101, "张伟", 1, "华为云平台迁移项目", "2026-06", 1, 21),
        # 李明: 3-6月全勤, 华为项目
        (7, 102, "李明", 1, "华为云平台迁移项目", "2026-03", 1, 22),
        (7, 102, "李明", 1, "华为云平台迁移项目", "2026-04", 1, 22),
        (7, 102, "李明", 1, "华为云平台迁移项目", "2026-05", 1, 20),
        (7, 102, "李明", 1, "华为云平台迁移项目", "2026-06", 1, 21),
        # 王芳: 4-6月全勤, 阿里项目
        (8, 103, "王芳", 2, "阿里数据中心项目",   "2026-04", 1, 22),
        (8, 103, "王芳", 2, "阿里数据中心项目",   "2026-05", 1, 20),
        (8, 103, "王芳", 2, "阿里数据中心项目",   "2026-06", 1, 21),
    ]

    ts_id = 1001
    settlement_data = []  # (user_id, talent_id, project_id, unit_price, workday_count, attendance, overtime, month)

    for (uid, tid, tname, pid, pname, month, start_d, end_d) in timesheet_configs:
        workday_count = [r[1] for r in workdays if r[0] == month][0]
        attendance = end_d - start_d + 1  # 全勤
        overtime = 0

        # 根据指派记录查unit_price
        for a in assignments:
            if a[1] == tid and a[3] == pid:
                unit_price = a[7]
                break

        # insert timesheet
        approve_time = f"{month}-05 10:00:00"
        c.execute(
            "INSERT INTO ts_timesheet(id,user_id,user_name,talent_id,talent_name,project_id,project_name,month,status,approver_id,approver_name,approve_time,create_time,update_time) "
            "VALUES(?,?,?,?,?,?,?,?,'APPROVED',4,'客户PM',?,?,?)",
            (ts_id, uid, tname, tid, tname, pid, pname, month, approve_time, f"{month}-01 00:00:00", now)
        )
        # insert daily records (全年工作日排布: 按星期一到五)
        weekday_map = {0: 1, 1: 2, 2: 3, 3: 4, 4: 5, 5: 6, 6: 7,
                       7: 8, 8: 9, 9: 10,10:11,11:12,12:13,13:14,14:15,
                       15:16,16:17,17:18,18:19,19:20,20:21,21:22,22:23,
                       23:24,24:25,25:26,26:27,27:28,28:29,29:30,30:31}
        # Simplified: just create NORMAL for day indices 1..workday_count mapped to actual weekdays
        # For a realistic demo, we'll map days 1-something to actual dates
        import calendar
        y, m = 2026, int(month[5:])
        cal_days = calendar.monthrange(y, m)[1]
        day_seq = 0
        for d in range(1, cal_days + 1):
            wd = calendar.weekday(y, m, d)  # 0=Monday
            if wd < 5:  # weekday
                day_seq += 1
                if start_d <= day_seq <= end_d:
                    c.execute(
                        "INSERT INTO ts_timesheet_day(timesheet_id,day_date,status) VALUES(?,?,'NORMAL')",
                        (ts_id, f"2026-{month[5:]}-{d:02d}")
                    )

        settlement_data.append((uid, tid, pid, unit_price, workday_count, attendance, overtime, month))
        ts_id += 1

    # ============================
    # 7. 结算单 — fin_settlement + fin_settlement_item (1-6月, 按项目汇总)
    # ============================
    total_workdays = {r[0]: r[1] for r in workdays}
    months_order = ["2026-01","2026-02","2026-03","2026-04","2026-05","2026-06"]
    sett_id = 2001

    for month in months_order:
        wd = total_workdays[month]
        items_for_month = [(uid, tid, pid, up, wd, att, ot)
                           for (uid, tid, pid, up, _wd, att, ot, mo) in settlement_data if mo == month]

        total_amount = 0.0
        item_ids = []

        for (uid, tid, pid, unit_price, wd_cnt, att_days, ot_days) in items_for_month:
            daily = round(unit_price / wd_cnt, 4)
            base = round(daily * att_days, 2)
            ot_amt = round(daily * ot_days, 2)
            final = round(base + ot_amt, 2)
            total_amount += final

            item_id = sett_id * 10 + len(item_ids) + 1
            # 查找对应的timesheet_id
            c2 = sqlite3.connect(DB_PATH)
            row = c2.execute(
                "SELECT id FROM ts_timesheet WHERE talent_id=? AND project_id=? AND month=?",
                (tid, pid, month)
            ).fetchone()
            c2.close()
            ts_ref_id = row[0] if row else 0

            tname_ = [a[2] for a in assignments + [(0, tid, "", pid, "", "", "", 0, "", "", "", "", "")] if a[1] == tid and a[3] == pid][0]
            pname_ = [a[4] for a in assignments if a[1] == tid and a[3] == pid][0]

            c.execute(
                "INSERT INTO fin_settlement_item(id,settlement_id,timesheet_id,user_id,user_name,talent_id,talent_name,project_id,project_name,unit_price_snapshot,workday_count_snapshot,actual_attendance_days,overtime_days,overtime_amount,base_amount,final_amount,calc_detail,create_time) "
                "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                (item_id, sett_id, ts_ref_id, uid, tname_, tid, tname_, pid, pname_,
                 unit_price, wd_cnt, att_days, ot_days, ot_amt, base, final,
                 f"{unit_price}÷{wd_cnt}×{att_days}+加班{ot_days}天", now)
            )
            item_ids.append(item_id)

        c.execute(
            "INSERT INTO fin_settlement(id,settlement_no,month,workday_count,total_amount,item_count,status,creator_id,creator_name,create_time,update_time) "
            "VALUES(?,?,?,?,?,?,'GENERATED',3,'财务专员',?,?)",
            (sett_id, f"STL-{month[5:]}", month, wd, round(total_amount, 2), len(item_ids), now, now)
        )
        sett_id += 1

    c.commit()
    print("=" * 60)
    print("[OK] 种子数据已写入")
    print(f"  数据库: {DB_PATH}")
    print(f"  人才: 5人 (3在项 + 2待岗)")
    print(f"  项目: 3个")
    print(f"  指派: 3条")
    print(f"  工时单: {ts_id - 1001}条 (6个月)")
    print(f"  结算单: {sett_id - 2001}份")
    print()
    print("  登录账号:")
    print("    admin    / 123456  (管理员)")
    print("    hr       / 123456  (HR)")
    print("    finance  / 123456  (财务)")
    print("    pm       / 123456  (客户PM)")
    print("    zhangwei / 123456  (张伟-外派员工)")
    print("    liming   / 123456  (李明-外派员工)")
    print("    wangfang / 123456  (王芳-外派员工)")
    print("    zhaoming / 123456  (赵明-外派员工)")
    print("    chenjing / 123456  (陈静-外派员工)")
    print("=" * 60)

if __name__ == "__main__":
    if not os.path.exists(DB_PATH):
        print(f"[ERROR] 找不到数据库: {DB_PATH}")
        print("   请先启动后端至少一次，让 DataInitializer 创建数据库和基础数据。")
        exit(1)

    seed(conn())
