import codecs
import json


def read_file1(path, file1):
    f = codecs.open(path + file1, 'r', encoding='utf-8')
    content = f.read()
    f.close()
    return content


def create_input_data():
    path = 'C:/cs/pyspace/MultiVerion/'
    file1 = 'tongjiresult-7-11.json'
    file2 = 'tongji-un-unified-7-11.json'

    file1_content = read_file1(path, file1)
    file1_dict = json.loads(file1_content)

    file2_content = read_file1(path, file2)
    file2_dict = json.loads(file2_content)

    project_set = set()

    for key in file1_dict.keys():
        project_set.add(key)
    for key in file2_dict.keys():
        project_set.add(key)

    f = codecs.open('inputId.txt', 'w', encoding='utf-8')
    f.write(json.dumps(list(project_set)))
    f.close()


def search_id(all_project_list, id):
    for project in all_project_list:
        if project['id'] == id:
            return project['url']
    return None


def generate_input_project():
    result_list = []

    f = codecs.open('inputId.txt', 'r', encoding='utf-8')
    content = f.read()
    f.close()

    f = codecs.open('projs12.7.json', 'r', encoding='utf-8')
    all_content = f.read()
    f.close()

    input_list = json.loads(content)
    all_project_list = json.loads(all_content)

    for input_id in input_list:
        url = search_id(all_project_list, int(input_id))
        result = dict()
        result['id'] = input_id
        result['url'] = url
        result_list.append(result)

    f = codecs.open('input_project.txt', 'w', encoding='utf-8')
    f.write(json.dumps(result_list))
    f.close()


if __name__ == '__main__':
    generate_input_project()
