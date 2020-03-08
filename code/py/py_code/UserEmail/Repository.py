import codecs
import json
import os

import requests

USERNAME = 'basti-shi031'
PASSWORD = '1994S06b20w'


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
    f = codecs.open('input_project.txt', 'r', encoding='utf-8')
    content = f.read()
    f.close()

    project_list = json.loads(content)

    for project in project_list:
        m_id = project['id']
        url = project['url']
        print(str(m_id), url)

        if os.path.exists('H:/shibowen/contributors/' + str(m_id) + '.txt'):
            continue

        urls = url.split('/')
        developer_name = urls[-2]
        repository_name = urls[-1]
        contributors = fetchContributors(developer_name, repository_name)
        f = codecs.open('H:/shibowen/contributors/' + str(m_id) + '.txt', 'w', encoding='utf-8')
        f.write(json.dumps(contributors, indent=4))
        f.close()
