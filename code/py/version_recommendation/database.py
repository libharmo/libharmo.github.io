import pymysql

def connectdb():
    print('连接到mysql服务器...')
    # 打开数据库连接
    #  db = mysql.connector.connect(user="root", passwd="123456.", database="TESTDB", use_unicode=True)
    db = pymysql.connect(host='127.0.0.1', port=3306, user="root", passwd="123456", database="third_party_library")
    # db = pymysql.connect(host='192.168.1.115', port=3306, user="root", passwd="123456", database="third_party_library")
    # db = mysql.connector.connect(host='10.141.221.73', port=3306, user="root", passwd="root",database="codehub")
    print('连接成功!')
    return db

def execute_sql(db,sql):
    # 使用cursor()方法获取操作游标
    cursor = db.cursor()

    cursor.execute(sql)
    # 提交到数据库执行
    db.commit()
    # try:
    #     # 执行sql语句
    #     cursor.execute(sql)
    #     # 提交到数据库执行
    #     db.commit()
    # except:
    #     print('插入数据失败!')
    #     db.rollback()

def querydb(db,sql):
    # 使用cursor()方法获取操作游标
    cursor = db.cursor()

    # SQL 查询语句
    # sql = "SELECT * FROM library_url"
    # try:
        # 执行SQL语句
    cursor.execute(sql)
        # 获取所有记录列表
    results = cursor.fetchall()
    return results
        # for row in results:
        #     ID = row[0]
        #     Name = row[1]
        #     Grade = row[2]
        #     # 打印结果
        #     print ("ID: %s, Name: %s, Grade: %d" % \
        #         (ID, Name, Grade))
    # except:
    #     print("Error: unable to fecth data")

def closedb(db):
    db.close()

# db = connectdb()
# re=querydb(db,"SELECT * FROM library_url")
# print(re)


