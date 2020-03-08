import smtplib

if __name__ == '__main__':
    pw = 'fpcvplejhheyespv'

    s = smtplib.SMTP('smtp.gmail.com', 587, None, 30)
    s.starttls()
    s.login("captain.bastian31@gmail.com", pw)
    message = "Message_you_need_to_send"
    s.sendmail("sender_email_id", '876572078@qq.com', message)
    s.quit()
