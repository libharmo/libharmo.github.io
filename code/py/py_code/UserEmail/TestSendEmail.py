from UserEmail import EmailUtils3

if __name__ == '__main__':
    # sender = 'fdu_se_lab@163.com'  # 发件人邮箱账号
    # sender = 'captain.bastian31@gmail.com'  # 发件人邮箱账号
    sender = 'SELab_FDU@outlook.com'
    receive = '876572078@qq.com'  # 收件人邮箱账号
    # passwd = 'fdse401fdse401'
    # passwd = 'fpcvplejhheyespv'
    passwd = 'calvin7%'
    # mailserver = 'smtp.gmail.com'
    # mailserver = 'smtp.gmail.com'
    mailserver = 'smtp.office365.com'
    port = '25'
    port = '587'

    files = []
    file1 = []
    file1.append('filename1.pdf')
    file1.append('../input/1286.pdf')
    files.append(file1)

    file2 = []
    file2.append('werqwd.txt')
    file2.append('d:/source.txt')
    files.append(file2)

    emailUtil = EmailUtils3.EmailUtil(sender, receive, "项目生成报告标题",
                                      '项目生成报告<a href="https://www.baidu.com">正文</a>', mailserver, port,
                                      'SELab of Fudan University',
                                      '施博文', passwd, files)
    success = emailUtil.send()
