import codecs
import json

if __name__ == '__main__':
    f = codecs.open('../input/meta_project.json', 'r', encoding='utf-8')
    origin_content = f.read()
    f.close()
    origin_dict = json.loads(origin_content)

    origin_module_list = origin_dict['module']
    origin_report_list = origin_dict['report_project']

    f = codecs.open('../input/meta_project2.json', 'r', encoding='utf-8')
    new_content = f.read()
    f.close()
    new_dict = json.loads(new_content)

    new_module_list = new_dict['module']
    new_report_list = new_dict['report_project']

    add_module = list()
    add_report = list()

    for new_module in new_module_list:
        if new_module not in origin_module_list:
            add_module.append(new_module)

    for new_report in new_report_list:
        if new_report not in origin_report_list:
            add_report.append(new_report)

    f = codecs.open('../output/add_module.json', 'w', encoding='utf-8')
    f.write(json.dumps(add_module, indent=4))
    f.close()

    f = codecs.open('../output/add_report.json', 'w', encoding='utf-8')
    f.write(json.dumps(add_report, indent=4))
    f.close()


