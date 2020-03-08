import codecs
import json
import os

import requests

USERNAME = 'basti-shi031'
# USERNAME = 'FDU-SE-LAB'
# USERNAME = 'bastiloveyao'
PASSWORD = '1994S06b20w'
# PASSWORD = 'fdsefdse401'


def get_contributors(owner, repository, session):
    url = ('https://api.github.com/repos/%s/%s/contributors?per_page=999&page=1') % (owner, repository)
    print(url)
    response = session.get(url)
    contributors = json.loads(response.text)
    return contributors


def add_email(contributors, session):
    size = len(contributors)
    index = 0
    for contributor in contributors:
        print(str(index) + '/' + str(size))
        index += 1
        url = contributor['url']
        response = session.get(url)
        email = json.loads(response.text)['email']
        contributor['email'] = email
    return contributors


def fetchContributors(developer_name, repository_name):
    session = requests.Session()
    session.auth = (USERNAME, PASSWORD)
    print('starting fetching contributors')
    contributor_list = get_contributors(developer_name, repository_name, session)
    print('ending fetching contributors')
    contributor_list = add_email(contributor_list, session)
    return contributor_list


if __name__ == '__main__':
    f = codecs.open('../input/modules_maven.json', 'r', encoding='utf-8')
    content = f.read()
    f.close()

    project_list = json.loads(content)

    for project_name in project_list.keys():
        result_path = '../output/contributors/' + str(project_name) + '.json'
        if os.path.exists(result_path):
            continue
        owner = project_name.split("__fdse__")[0]
        repository = project_name.split("__fdse__")[1]
        if owner == 'stuxuhai' and repository == 'HData':
            continue


        print(owner, repository)
        contributors = fetchContributors(owner, repository)
        f = codecs.open('../output/contributors/' + str(project_name) + '.json', 'w', encoding='utf-8')
        f.write(json.dumps(contributors, indent=4))
        f.close()
