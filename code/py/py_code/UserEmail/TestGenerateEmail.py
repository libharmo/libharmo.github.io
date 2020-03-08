from UserEmail.EmailGenerator import EmailGenerator

if __name__ == '__main__':
    attr_map = dict()

    mail_content_path = 'input/random_mail.json'
    survey_content_path = 'input/survey_urls.txt'
    report_content_path = 'input/report_urls.txt'

    generator = EmailGenerator(mail_content_path,survey_content_path,report_content_path)
    flag_list = generator.generate_flag(attr_map)
    detail_map = generator.generate_detail_map('testName')
    content = generator.generate_random_content(flag_list, detail_map)
    print(content)
