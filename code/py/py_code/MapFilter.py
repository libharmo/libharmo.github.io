import codecs
import json

if __name__ == '__main__':
    f = codecs.open('RQ3Meta1.30.json', 'r', encoding='utf-8')
    content = f.read()
    f.close()

    content_json = json.loads(content)

    for jar_issue in content_json:
        issues = jar_issue['issues']
        for issue_name in issues.keys():
            print(issue_name)