import codecs
import json
import time

from UserEmail import EmailUtils3
from UserEmail.EmailGenerator import EmailGenerator

if __name__ == '__main__':

    report_dir = 'input/report/total/'
    mail_content_path = 'input/random_mail.json'
    survey_content_path = 'input/survey_urls.txt'
    report_content_path = 'input/report_urls.txt'

    # sender = 'fdu_se_lab@163.com'
    # passwd = 'fdse401fdse401'
    # mailserver = 'smtp.163.com'
    # port = '25'

    # sender = 'fdu_se_lab@163.com'
    # passwd = 'fdse401fdse401'
    # mailserver = 'smtp.163.com'
    # port = '25'

    # sender = 'thirdPartyLib@126.com'
    # passwd = 'fdse401fdse401'
    # mailserver = 'smtp.126.com'
    # port = '25'

    # sender = 'captain.bastian31@gmail.com'
    #     # passwd = 'vucsxdzaocghggjm'
    #     # mailserver = 'smtp.gmail.com'
    #     # port = '587'

    # sender = 'hkf@sedu.page'
    # passwd = 'ideppvmexngmeyvj'
    # mailserver = 'smtp.gmail.com'
    # port = '587'

    # sender = 'hkf3rdparty@gmail.com'
    # passwd = 'tiyrokproiufubtc'
    # mailserver = 'smtp.gmail.com'
    # port = '25'
    #
    # sender = 'selab_fdu@outlook.com'
    # passwd = 'calvin7%'
    # port = '587'
    # mailserver = 'smtp.office365.com'
    sender = 'calvinhkf@hotmail.com'
    passwd = 'calvin7%'
    port = '587'
    mailserver = 'smtp.office365.com'

    success_result_output_dir = 'input/report_send_success.json'
    failure_result_output_dir = 'input/report_failure_success.json'

    image_json_path = 'input/large_image.json'

    f = codecs.open(image_json_path, 'r', encoding='utf-8')
    image_content = f.read()
    f.close()
    image_map = json.loads(image_content)

    f = codecs.open(failure_result_output_dir, 'r', encoding='utf-8')
    failure_content = f.read()
    f.close()
    failure_result_list = [] if failure_content == '' else json.loads(failure_content)

    f = codecs.open(success_result_output_dir, 'r', encoding='utf-8')
    success_content = f.read()
    f.close()
    success_result_list = [] if success_content == '' else json.loads(success_content)

    f = codecs.open('input/format_contributors4.json', 'r', encoding='utf-8')
    content = f.read()
    f.close()

    contributors = json.loads(content)
    count = 0
    for contributor_name in contributors.keys():
        if contributor_name in success_result_list:
            continue

        print(contributor_name)
        contributor_info = contributors[contributor_name]

        generator = EmailGenerator(mail_content_path, survey_content_path, report_content_path)
        flag_list = generator.generate_flag(contributor_info)
        detail_map = generator.generate_detail_map(contributor_name)
        contents = generator.generate_random_content(flag_list, detail_map)

        # contents = EmailUtils3.generateContent(contributor_info, contributor_name)
        message_title = contents[0]
        files = EmailUtils3.generateFiles(contributor_info, report_dir, image_map)
        receiver = contributor_info['email']
        # receiver = 'captain.bastian31@gmail.com'
        print(receiver)
        emailUtil = EmailUtils3.EmailUtil(sender, receiver, message_title,
                                          contents[1], mailserver, port,
                                          'SELab of Fudan University',
                                          contributor_name, passwd, files)
        success = emailUtil.send()
        if success:
            success_result_list.append(contributor_name)
            f = codecs.open(success_result_output_dir, 'w', encoding='utf-8')
            f.write(json.dumps(success_result_list))
            f.close()
            print('success    ' + contributor_name)
            time.sleep(15)
        else:
            failure_result_list.append(contributor_name)
            f = codecs.open(failure_result_output_dir, 'w', encoding='utf-8')
            f.write(json.dumps(failure_result_list))
            f.close()
            print('failure    ' + contributor_name)
