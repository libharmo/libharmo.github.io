from UserEmail import EmailUtils3

if __name__ == '__main__':
    sender = 'fdu_se_lab@163.com'  # 发件人邮箱账号
    receive = '876572078@qq.com'  # 收件人邮箱账号
    passwd = 'fdse401fdse401'
    mailserver = 'smtp.163.com'
    port = '25'

    #     email = EmailUtil('fdu_se_lab@163.com', '876572078@qq.com', '数据库', 'smtp.163.com',
    #                       'Fudan_Se_Lab<fdu_se_lab@163.com>',
    #                       'Github<876572078@qq.com>',
    #                       'fdsefdse', 'fdse401fdse401', 'd:/test.pdf', 'test.pdf')
    emailUtil = EmailUtils3.EmailUtil(sender, receive, "项目生成报告标题",
                            '项目生成报告<a href="https://www.baidu.com">正文</a>', mailserver,port, 'SELab of Fudan University',
                            '施博文', passwd, '', '')
    emailUtil.send()
