import codecs
import json
import smtplib
from email import encoders
from email.mime.application import MIMEApplication
from email.mime.base import MIMEBase
from email.mime.text import MIMEText
from email.utils import formataddr

from email.mime.multipart import MIMEMultipart


class EmailUtil(object):
    server = None
    count = 0

    def __init__(self, sender, receiver, msg_title, content, sender_server, port, From, To, mail_pass, files):
        EmailUtil.count += 1
        self.sender = sender
        self.receiver = receiver
        self.msg_title = msg_title
        self.sender_server = sender_server
        self.port = port
        self.content = content
        self.From = From
        self.To = To
        self.mail_pass = mail_pass
        self.files = files
        # if EmailUtil.server is None or EmailUtil.count >= 5:
        #     EmailUtil.count += 1
        #     EmailUtil.server = smtplib.SMTP(self.sender_server, self.port, timeout=10)
        #     EmailUtil.server.ehlo()
        #     EmailUtil.server.starttls()
        #     EmailUtil.server.login(self.sender, self.mail_pass)
        # if EmailUtil.count >= 10:
        #     EmailUtil.count = EmailUtil.count % 10

    def send(self):
        try:
            msg = MIMEMultipart()
            # todo
            msg['From'] = formataddr([self.From, self.sender])
            msg['To'] = formataddr([self.To, self.receiver])
            msg['Subject'] = self.msg_title
            txt = MIMEText(self.content, 'html', 'utf-8')
            msg.attach(txt)

            for file in self.files:
                file_name = file[0]
                file_path = file[1]
                if file_name.endswith('.pdf'):
                    f = codecs.open(file_path, 'rb')
                    content = f.read()
                    f.close()
                    attach = MIMEApplication(content)
                    attach.add_header('Content-Disposition', 'attachment', filename=file_name)
                    msg.attach(attach)
                if file_name.endswith('.png'):
                    with open(file_path, 'rb') as f:
                        mime = MIMEBase('image', 'png', filename=file_name)
                        mime.add_header('Content-Disposition', 'attachment', filename=file_name)
                        mime.add_header('Content-ID', '<0>')
                        mime.add_header('X-Attachment-Id', '0')

                        content = f.read()
                        f.close()
                        mime.set_payload(content)

                        encoders.encode_base64(mime)

                        msg.attach(mime)

            # EmailUtil.server.ehlo()
            # EmailUtil.server.starttls()
            # EmailUtil.server.login(self.sender, self.mail_pass)
            EmailUtil.count += 1

            # if EmailUtil.server is None or EmailUtil.count >= 5:
            EmailUtil.server = smtplib.SMTP(self.sender_server, self.port, timeout=10)
            EmailUtil.server.ehlo()
            EmailUtil.server.starttls()
            EmailUtil.server.login(self.sender, self.mail_pass)
            EmailUtil.count = EmailUtil.count % 5
            a = EmailUtil.server.sendmail(self.sender, self.receiver, msg.as_string())
            print(a)
            # if EmailUtil.count == 0:
            EmailUtil.server.quit()
            return True

        except Exception as e:
            print(e)
            return False


def generateContent(contributorInfo, contributor_name):
    f = codecs.open('input/survey.json', 'r', encoding='utf-8')
    content = f.read()
    f.close()

    survey_map = json.loads(content)

    survey = survey_map['survey']
    report = survey_map['report']
    regards = survey_map['regards']

    result = survey % contributor_name

    if 'report_projects' in contributorInfo.keys() and len(contributorInfo['report_projects']) >= 1:
        result += report

    result += regards

    return result


def generateFiles(contributor_info, report_dir, image_map):
    files = list()
    if 'report_projects' in contributor_info.keys():
        report_projects = contributor_info['report_projects']
        for report_project in report_projects:
            # 'https://github.com/alibaba/jstorm'
            owner = report_project.split('/')[-2]
            repository = report_project.split('/')[-1]
            file_name = repository + '.pdf'
            file_path = report_dir + owner + ' ' + repository + '.pdf'
            pdf_file = list()
            pdf_file.append(file_name)
            pdf_file.append(file_path)
            files.append(pdf_file)

            key = owner + ' ' + repository
            image_files = list() if key not in image_map else image_map[key]

            for image_file in image_files:
                image_file_result = list()
                image_file_result.append(image_file)
                image_file_result.append('input/pngs/pngs/' + image_file)
                files.append(image_file_result)

    return files
