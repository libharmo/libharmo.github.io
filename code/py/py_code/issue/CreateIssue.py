import json
import time

import FileUtils
from issue.IssueUtils import IssueUtils

if __name__ == '__main__':
    repository_path = '../output/issues.json'
    repository_map = json.loads(FileUtils.read(repository_path))

    success_path = 'input/success.json'
    success_content = FileUtils.read(success_path)

    success_list = json.loads('[]' if success_content == '' else success_content)

    for repository_item in repository_map.keys():
        if not repository_map[repository_item]:
            continue
        if repository_item in success_list:
            continue
        # repository_item = 'https://github.com/prestodb/presto'
        print(repository_item)
        owner = repository_item.split('/')[-2]
        repository = repository_item.split('/')[-1]
        contents = IssueUtils.generate_content(owner, repository)

        r = IssueUtils.send_issue(owner, repository, contents)
        # r = IssueUtils.send_issue('basti-shi031', 'ContactUtil', contents)
        if r.ok:
            success_list.append(repository_item)
            FileUtils.write(success_path, json.dumps(success_list))
            print('success')
        else:
            print(r.text)

        time.sleep(15)
