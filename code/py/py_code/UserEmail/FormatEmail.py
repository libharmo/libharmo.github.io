import codecs
import json
import os

if __name__ == '__main__':
    email_dir = 'C:/cs/pyspace/github/output/contributors/'
    project_files = os.listdir(email_dir)

    meta_project_path = '../input/meta_project3.json'
    f = codecs.open(meta_project_path, 'r', encoding='utf-8')
    content = f.read()
    f.close()
    report_projects = json.loads(content)['report_project']

    result = dict()

    for project_file in project_files:
        project_name = project_file.split(".json")[0]
        owner = project_name.split("__fdse__")[0]
        repository = project_name.split("__fdse__")[1]

        f = codecs.open(email_dir + project_file)
        content = f.read()
        f.close()

        contributors = json.loads(content)
        for contributor in contributors:
            if contributor['email'] is not None:
                login = contributor['login']
                id = contributor['id']
                if login not in result.keys():
                    result[login] = dict()
                result[login]['email'] = contributor['email']
                result[login]['id'] = contributor['id']
                url = 'https://github.com/' + owner + '/' + repository
                if url in report_projects:
                    # has report
                    if 'report_projects' not in result[login].keys():
                        result[login]['report_projects'] = list()
                    result[login]['report_projects'].append(url)
                else:
                    # not report
                    if 'no_report_projects' not in result[login].keys():
                        result[login]['no_report_projects'] = list()
                    result[login]['no_report_projects'].append(url)
    f = codecs.open('../input/format_contributors.json', 'w', encoding='utf-8')
    f.write(json.dumps(result, indent=4))
    f.close()
