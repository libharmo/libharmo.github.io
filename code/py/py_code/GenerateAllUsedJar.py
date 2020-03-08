import codecs
import json
import operator
import os

if __name__ == '__main__':

    result_dict = {}

    # 读取所有文件
    file_dir = 'input/real_multi_version_method_calls_with_module'
    files = os.listdir(file_dir)
    for file in files:
        file_path = file_dir + '/' + file
        f = codecs.open(file_path, 'r', encoding='utf-8')
        content = f.read()
        f.close()
        content_json = json.loads(content)

        for java_file in content_json.keys():
            java_file_info = content_json[java_file]
            for jar in java_file_info.keys():
                if jar == 'module':
                    continue
                # jar:xxx-xxx-1.0.0-1323123.3411324123.jar
                # jar:xxx-xxx-1.0.0-1323123.3411324123
                jar = jar.split('.jar')[0]
                jars = jar.split('-')
                artifact_name = ''
                for jar_item in jars:
                    if operator.ge(jar_item[0].lower(), 'a') and operator.le(jar_item[0].lower(), 'z'):
                        artifact_name += '-' + jar_item
                    else:
                        break
                artifact_name = artifact_name[1:]
                version = jar[len(artifact_name) + 1:]

                if artifact_name not in result_dict:
                    result_dict[artifact_name] = []
                if version in result_dict[artifact_name]:
                    continue
                result_dict[artifact_name].append(version)

    f = codecs.open('output/all_jar.txt','w',encoding='utf-8')
    f.write(json.dumps(result_dict,indent=4))
    f.close()
