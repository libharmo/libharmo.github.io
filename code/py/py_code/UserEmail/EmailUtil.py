# _*_coding:utf-8_*_
import os
import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
from email.mime.base import MIMEBase
from email.header import Header
from email import encoders
import time

from email.MIMEMultipart import MIMEMultipart
from email.MIMEBase import MIMEBase


class EmailUtil(object):
    def __init__(self, sender, receivers, msg_title, sender_server, From, To, password, mail_pass, file, file_name):
        self.curDateTime = str(time.strftime('%Y-%m-%d %H:%M:%S', time.localtime()))  # 当前日期时间
        self.sender = sender
        self.receivers = receivers  # 从配置文件获取，接收人
        self.msg_title = msg_title  # 从配置文件获取，邮件标题
        self.sender_server = sender_server  # 从配置文件获取，发送服务器
        self.From = From
        self.To = To
        self.password = password
        self.mail_pass = mail_pass
        self.file = file
        self.file_name = file_name

    '''
    配置邮件内容
    '''

    @property
    def setMailContent(self):
        msg = MIMEMultipart()
        # 邮件正文是MIMEText:
        msg.attach(MIMEText("软件工程", 'plain', 'utf-8'))
        msg['From'] = Header(self.From, 'utf-8')
        msg['To'] = self.To
        msg['Subject'] = Header(self.msg_title, 'utf-8')
        # # 增加附件
        # addition = self.addAttach(self.file, self.file_name)  # 自动化测试报告附件
        # msg.attach(addition)

        return msg

    '''
    增加附件
    '''

    def addAttach(self, apath, filename):
        with open(apath, 'rb') as fp:
            attach = MIMEBase('application', 'octet-stream')
            attach.set_payload(fp.read())
            attach.add_header('Content-Disposition', 'attachment', filename=filename)
            encoders.encode_base64(attach)
            fp.close()
            return attach

    '''
    发送电子邮件
    '''

    def sendEmail(self, message):
        try:
            smtpObj = smtplib.SMTP()
            smtpObj.connect(self.sender_server, 25)
            smtpObj.login(self.sender, self.mail_pass)
            smtpObj.sendmail(self.sender, self.receivers, message.as_string())
            smtpObj.quit()
            print("邮件发送成功")

        except smtplib.SMTPException as ex:
            print("Error: 无法发送邮件.%s" % ex)

    # 发送调用
    @property
    def send(self):
        self.sendEmail(self.setMailContent())


if __name__ == "__main__":
    # 18210240163@fudan.edu.cn
    email = EmailUtil('fdu_se_lab@163.com', '876572078@qq.com', '数据库', 'smtp.163.com',
                      'Fudan_Se_Lab<fdu_se_lab@163.com>',
                      'Github<876572078@qq.com>',
                      'fdsefdse', 'fdse401fdse401', 'd:/test.pdf', 'test.pdf')
    email.send
