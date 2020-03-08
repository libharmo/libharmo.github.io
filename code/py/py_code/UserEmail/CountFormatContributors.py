import codecs
import json


def add(count, map):
    if count not in map.keys():
        map[count] = 1
    else:
        map[count] = map[count] + 1


if __name__ == '__main__':
    format_contributors_path = '../input/format_contributors.json'
    f = codecs.open(format_contributors_path, 'r', encoding='utf-8')
    content = f.read()
    f.close()

    contributors = json.loads(content)
    print(len(contributors))

    report_map = dict()
    no_report_map = dict()
    all_project_map = dict()

    for contributor in contributors.keys():
        info = contributors[contributor]
        report_count = 0
        no_report_count = 0
        all_project_count = 0

        report_count = len(info['report_projects']) if 'report_projects' in info.keys() else 0
        no_report_count = len(info['no_report_projects']) if 'no_report_projects' in info.keys() else 0
        all_project_count = report_count + no_report_count

        add(report_count, report_map)
        add(no_report_count, no_report_map)
        add(all_project_count, all_project_map)

    f = codecs.open('../input/report_map.json', 'w', encoding='utf-8')
    f.write(json.dumps(report_map, indent=4))
    f.close()

    f = codecs.open('../input/no_report_map.json', 'w', encoding='utf-8')
    f.write(json.dumps(no_report_map, indent=4))
    f.close()

    f = codecs.open('../input/all_project_map.json', 'w', encoding='utf-8')
    f.write(json.dumps(all_project_map, indent=4))
    f.close()
