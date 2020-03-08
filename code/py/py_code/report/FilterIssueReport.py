import json
import os
import shutil

import FileUtils

if __name__ == '__main__':
    input_path = '../output/issues.json'
    report_dir = 'H:/shibowen/report/total/'
    report_out_dir = 'H:/shibowen/report/issues/'

    repository_map = json.loads(FileUtils.read(input_path))

    for url in repository_map.keys():
        has_issues = repository_map[url]
        if has_issues:
            owner = url.split('/')[-2]
            repository = url.split('/')[-1]
            report_path = report_dir + owner + ' ' + repository + '.pdf'
            if not os.path.exists(report_path):
                print(report_path)
                continue
            shutil.copy(report_path, report_out_dir + owner + ' ' + repository + '.pdf')
