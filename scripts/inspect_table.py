import pymysql
import sys
if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')
conn = pymysql.connect(host='localhost', port=3306, user='root', password='12345678')
cur = conn.cursor()
cur.execute("SHOW CREATE TABLE liochio_app_db.system_settings")
row = cur.fetchone()
print(row[1])
cur.close()
conn.close()
