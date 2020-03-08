import json
import random

import FileUtils


def generate_attr_map():
    attr_map = dict()
    attr_map['1'] = '<p></p>'
    attr_map['2'] = ' '
    attr_map['3'] = '<p></p>'
    attr_map['4'] = ' '
    attr_map['5'] = ' '
    attr_map['6'] = ' '
    attr_map['7'] = ' '
    attr_map['8'] = '<p></p>'
    attr_map['9'] = ' '
    attr_map['10'] = ' '
    attr_map['11'] = ' '
    attr_map['12'] = '<p></p>'
    attr_map['13'] = '<p></p>'
    attr_map['14'] = ' '
    attr_map['15'] = ' '
    attr_map['16'] = ' '
    attr_map['17'] = '<p></p>'
    attr_map['18'] = '<p></p>'
    attr_map['19'] = '<p></p>'
    attr_map['20'] = ''
    return attr_map


class EmailGenerator(object):
    content_list = None
    attr_map = None
    source_map = None

    survey_urls = None
    report_urls = None

    def __init__(self, file_path, survey_path, report_path):
        self.path = file_path
        self.survey_path = survey_path
        self.report_path = report_path
        if EmailGenerator.source_map is None:
            survey_url_content = FileUtils.read(self.survey_path)
            EmailGenerator.survey_urls = json.loads(survey_url_content)

            report_url_content = FileUtils.read(self.report_path)
            EmailGenerator.report_urls = json.loads(report_url_content)

    def generate_random_content(self, flag_list, detail_map):
        result = list()
        content_result = ''
        if EmailGenerator.source_map is None:
            content = FileUtils.read(self.path)
            EmailGenerator.source_map = json.loads(content)
            EmailGenerator.attr_map = generate_attr_map()


        for index in range(0, len(EmailGenerator.source_map)):
            if index in flag_list:
                continue
            content_item_key = str(index)
            content_list = EmailGenerator.source_map[content_item_key]
            size = len(content_list)
            begin = 1 if size > 1 else 0
            random_index = random.randint(begin, size - 1)
            content = content_list[random_index]
            if content_item_key in detail_map.keys() and '%s' in content:
                replace_word = detail_map[content_item_key]
                if index == 1:
                    content = content % replace_word
                if index == 13 or index == 17:

                    content = content % (replace_word, replace_word)
            if index == 0:
                result.append(content)
            else:
                content_result += content
                content_result += EmailGenerator.attr_map[content_item_key]
        result.append(content_result)
        return result

    def generate_flag(self, contributorInfo):
        flag_list = list()
        if 'report_projects' in contributorInfo.keys() and len(contributorInfo['report_projects']) >= 1:
            return flag_list
        flag_list.append(14)
        flag_list.append(15)
        flag_list.append(16)
        flag_list.append(17)
        return flag_list

    def generate_detail_map(self, name):
        survey_size = len(EmailGenerator.survey_urls)
        report_size = len(EmailGenerator.report_urls)
        detail_map = dict()
        detail_map['1'] = name
        detail_map['13'] = EmailGenerator.survey_urls[random.randint(0, survey_size - 1)]
        detail_map['17'] = EmailGenerator.report_urls[random.randint(0, report_size - 1)]
        return detail_map
