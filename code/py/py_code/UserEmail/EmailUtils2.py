# coding:utf-8
# smtplib模块负责连接服务器和发送邮件
# MIMEText：定义邮件的文字数据
# MIMEImage：定义邮件的图片数据
# MIMEMultipart：负责将文字图片音频组装在一起添加附件
import smtplib  # 加载smtplib模块
from email.mime.text import MIMEText
from email.utils import formataddr

from email.mime.multipart import MIMEMultipart

sender = 'fdu_se_lab@163.com'  # 发件人邮箱账号
receive = '876572078@qq.com'  # 收件人邮箱账号
passwd = 'fdse401fdse401'
mailserver = 'smtp.163.com'
port = '25'
sub = 'Python3 test'

try:
    msg = MIMEMultipart()
    msg['From'] = formataddr(["sender", sender])  # 发件人邮箱昵称、发件人邮箱账号
    msg['To'] = formataddr(["receiver", receive])  # 收件人邮箱昵称、收件人邮箱账号
    msg['Subject'] = sub
    # 文本信息
    txt = MIMEText('this is a test <a href="https://www.baidu.com">mail</a>', 'html', 'utf-8')
    msg.attach(txt)

    # #附件信息
    # attach = MIMEApplication(open("D:\xx\\tool\pycharm\\1.csv").read())
    # attach.add_header('Content-Disposition', 'attachment', filename='1.csv')
    # msg.attach(attach)
    # msg.attach(pic)
    server = smtplib.SMTP(mailserver, port)  # 发件人邮箱中的SMTP服务器，端口是25
    server.login(sender, passwd)  # 发件人邮箱账号、邮箱密码
    server.sendmail(sender, receive, msg.as_string())  # 发件人邮箱账号、收件人邮箱账号、发送邮件
    server.quit()
    print('success')
except Exception as e:
    print(e)
