import pymysql
import sys
if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')
conn = pymysql.connect(host='localhost', port=3306, user='root', password='12345678')
cur = conn.cursor()
cur.execute("SELECT `key`, `value`, `type`, `status` FROM liochio_app_db.system_settings")
for r in cur.fetchall():
    print(r)
cur.close()
conn.close()
