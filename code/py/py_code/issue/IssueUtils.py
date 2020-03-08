import json

import requests

import FileUtils


def generate_image_list(owner, repository):
    image_path = '../UserEmail/input/large_image.json'
    image_map = json.loads(FileUtils.read(image_path))

    if owner + ' ' + repository in image_map.keys():
        image_list = image_map[owner + ' ' + repository]
        return image_list
    return None


class IssueUtils(object):

    @staticmethod
    def generate_content(owner, repository):
        pdf_link = '[%s](%s)'
        image_link = '[%s](%s)'
        contents = list()
        # title
        content = ''
        contents.append('Third-Party Library Version Inconsistencies in your project\n')
        second_part = 'We are currently investigating the usage of different versions of the same third-party library in different modules of a project (i.e., the library version inconsistency problem). It is a non-trivial and time-consuming task to maintain the evolving third-party libraries. In that sense, it seems necessary to harmonize the different versions of the same third-party library in different modules into one single version. Thus we developed a prototype tool to detect library version inconsistencies and suggest harmonized version with detailed maintenance efforts. We applied our tool on your GitHub project, and attached the generated report. We hope that the report could be useful for you to be aware of the library version inconsistencies in your project and to decide whether to harmonize inconsistent library versions.'
        content += second_part
        content += '\n'
        content += 'Here is the report:\n'

        pdf_name = owner + ' ' + repository + '.pdf'
        pdf_path = 'https://github.com/ThirdLibResearch/ThirdLibSource/blob/master/issues/report/' + owner + '%20' + repository + '.pdf'
        content += pdf_link % (pdf_name, pdf_path) + '\n'

        raw_image_list = generate_image_list(owner, repository)
        image_list = list()
        if raw_image_list is not None:
            content += '\n'
            content += '\n'
            content += '\n'
            content += 'Some pictures may be too large to present in the pdf so we attach them below: \n'
            for raw_image in raw_image_list:
                image_list.append(image_link % (
                    raw_image,
                    'https://raw.githubusercontent.com/ThirdLibResearch/ThirdLibSource/master/issues/image/' + raw_image))

        for image in image_list:
            content += image + '\n'

        content += '\n'

        content += 'We will appreciate your comments very much.\n\n'
        content += 'Best regards'
        contents.append(content)

        return contents

    @classmethod
    def send_issue(cls, owner, repository, contents):
        USERNAME = 'ThirdLibResearch'
        PASSWORD = 'fdsefdse401'
        session = requests.Session()
        session.auth = (USERNAME, PASSWORD)
        title = contents[0]
        body = contents[1]

        # Our url to create issues via POST
        url = 'https://api.github.com/repos/%s/%s/issues' % (owner, repository)

        # Create our issue
        issue = {'title': title,
                 'body': body,
                 'assignee': None,
                 'milestone': None,
                 'labels': ['warning']}
        # Add the issue to our repository
        r = session.post(url, json.dumps(issue))
        return r
