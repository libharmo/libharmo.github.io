import codecs
import json


def readAllJarInput():
    f = codecs.open('output/all_jar.txt', 'r', encoding='utf-8')
    content_json = json.loads(f.read())
    f.close()
    return content_json


if __name__ == '__main__':
    jar_dict = readAllJarInput()
    for artifact in jar_dict.keys():
        versions = jar_dict[artifact]
        versions.sort(key=version)
        jar_dict[artifact] = versions
    f = codecs.open('output/candidate.txt','w',encoding='utf-8')
    f.write(json.dumps(jar_dict,indent=4))
    f.close()
