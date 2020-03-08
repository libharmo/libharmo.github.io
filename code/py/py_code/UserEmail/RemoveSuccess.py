import json

import FileUtils

if __name__ == '__main__':
    origin_file = 'input/format_contributors.json'
    success_file = 'input/report_send_success.json'

    origin_map = json.loads(FileUtils.read(origin_file))
    success_list = json.loads(FileUtils.read(success_file))
    for success_item in success_list:
        origin_map.pop(success_item)
    print(len(origin_map))

    FileUtils.write('input/format_contributors4.json', json.dumps(origin_map, indent=4))
