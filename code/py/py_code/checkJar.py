import codecs
import json
import os

if __name__ == '__main__':
    f = codecs.open('output/all_jar.txt', 'r', encoding='utf-8')
    content = f.read()
    f.close()

    content_json = json.loads(content)
    all_count = 0
    find_count = 0
    for jar in content_json.keys():
        versions = content_json[jar]

        for version in versions:
            all_count += 1
            if os.path.exists('h:/wangying/lib_all/' + jar + '-' + version + '.jar'):
                find_count += 1
            else:
                print(jar+'-'+version)
    print(all_count)
    print(find_count)
