import json

import FileUtils
from github.Repository import Repository

if __name__ == '__main__':
    input_file = '../input/jira-ic-url.json'
    content = FileUtils.read(input_file)

    projects_map = json.loads(content)

    output_fetch_path = '../output/has_fetch.json'
    fetch_content = FileUtils.read(output_fetch_path)
    fetch_list = json.loads('[]' if fetch_content == '' else fetch_content)

    issues_path = '../output/issues.json'
    issues_content = FileUtils.read(issues_path)
    issues_map = json.loads('{}' if fetch_content == '' else issues_content)

    for project_id in projects_map.keys():
        url = projects_map[project_id]
        print(project_id,url)
        if url in fetch_list:
            continue
        repository = Repository(url)
        has_issue = repository.has_issues()
        if has_issue is not None:
            # get success
            fetch_list.append(url)
            issues_map[url] = has_issue

            FileUtils.write(output_fetch_path, json.dumps(fetch_list, indent=4))
            FileUtils.write(issues_path, json.dumps(issues_map, indent=4))
