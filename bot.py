# -*- coding: UTF-8 -*-

import ConfigParser 
import sqlite3
import time
from wxpy import *

cf = ConfigParser.SafeConfigParser()
cf.read(".config")
group_name=unicode(cf.get("all", "group_name"), 'utf-8')
cache_path=cf.get("all", "cache_path")
db_path=cf.get("all", "db_path")

bot = Bot(cache_path, 1)
group = bot.groups().search(group_name)[0]

@bot.register(group, TEXT)
def recv(msg):
  if not msg.is_at:
    return
  conn = sqlite3.connect(db_path)
  conn.execute("INSERT INTO Receive (MSG_ID, TYPE, MEMBER, CONTENT) VALUES (?, ?, ?, ?)", (str(msg.id), msg.type, msg.member.name, msg.text))
  conn.commit()
  conn.close()

def send(conn):
  cursor = conn.execute("SELECT MIN(ID) FROM Send")
  mid = cursor.fetchone()[0]
  cursor.close()
  if mid is None:
    return
  cursor = conn.execute("SELECT CONTENT FROM Send WHERE ID = ?", (mid, ))
  content = cursor.fetchone()[0]
  cursor.close()
  conn.execute("DELETE FROM Send WHERE ID = ?", (mid, ))
  conn.commit()
  group.send_msg(content)

conn = sqlite3.connect(db_path)
while True:
  send(conn)
  time.sleep(2)
  
  


