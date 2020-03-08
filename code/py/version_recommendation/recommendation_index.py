import os
import sys

from version_recommendation import database
from version_recommendation.file_util import read_json, write_json, write_json_format
from version_recommendation.version_compare import get_max_version, jar2version, get_version_of_jar

def get_pair():
    lib_jar_pair = {}
    db = database.connectdb()
    libs = read_json("datas/tongji_libs.json")

    for proj in libs:
        proj_obj = libs[proj]
        for _module in proj_obj:
            module_obj = proj_obj[_module]
            for jar in module_obj:
                version = module_obj[jar]
                if version + "__fdse__" + jar in lib_jar_pair:
                    continue
                jar_array = jar.split("__fdse__")
                groupId = jar_array[0]
                artifactId = jar_array[1]
                _type = jar_array[2]
                classifier = None
                if len(jar_array) > 3:
                    classifier = jar_array[3]

                if "${" in version:
                    continue
                # print(groupId + " " + artifactId + " " + version)
                sql = "SELECT id FROM library_versions WHERE group_str = '" + groupId + "' and name_str = '" + artifactId + "' and version = '" + version + "'"
                library_info = database.querydb(db, sql)
                library_id = None
                if len(library_info) > 0:
                    library_id = library_info[0][0]
                else:
                    sys.stderr.write(groupId + " " + artifactId + " " + version + " library_info" + "\n")
                    sys.stderr.write(str(library_id) + "\n")
                    continue
                if classifier is None:
                    sql = "SELECT jar_package_url FROM version_types WHERE (version_id = " + str(
                        library_id) + " or version_id2 = " + str(
                        library_id) + ") and type = '" + _type + "' and classifier is null"
                else:
                    sql = "SELECT jar_package_url FROM version_types WHERE (version_id = " + str(
                        library_id) + " or version_id2 = " + str(
                        library_id) + ") and type = '" + _type + "' and classifier = '" + classifier + "'"
                jar_result = database.querydb(db, sql)
                if len(jar_result) > 0:
                    lib_jar_pair[version + "__fdse__" + jar] = jar_result[0][0]
                else:
                    sys.stderr.write(groupId + " " + artifactId + " " + version + " " + str(library_id))
    write_json("lib_jar_pair.txt", lib_jar_pair)


def get_lib_in_tongjiresult():
    count = 0
    final = {}
    json_data = read_json("../tongjiresult-8-5.json")
    for file in json_data:
        print(file)
        new_proj = {}
        file_obj = json_data[file]
        for jar in file_obj:
            trees = file_obj[jar]
            for tree_id in trees:
                subtree = trees[tree_id]
                count += len(subtree)
                for entry in subtree:
                    usePostion = entry["usePostion"]
                    version = entry["resolved_version"]
                    if not usePostion.endswith("/pom.xml"):
                        _module = usePostion.replace("pom.xml", "")
                    else:
                        _module = usePostion.replace("/pom.xml", "")
                    if _module in new_proj:
                        if jar in new_proj[_module]:
                            print(jar + " " + new_proj[_module][jar])
                            sys.exit(0)
                    else:
                        new_proj[_module] = {}
                    new_proj[_module][jar] = version
        final[file] = new_proj
    write_json("datas/tongji_libs.json", final)

    # count = 0
    # json_data = read_json("datas/tongji_libs.json")
    # print(len(json_data))
    # for file in json_data:
    #     file_obj = json_data[file]
    #     for jar in file_obj:
    #         jar_obj = file_obj[jar]
    #         count += len(jar_obj)
    #
    # print(count)

def filter_lib_in_callgraph():
    # 去除掉call_graph中存在的一个pom中同一个库存在不同版本的情况，同时去除找不到module的java文件
    tongji_libs = read_json("datas/tongji_libs.json")

    db = database.connectdb()

    dir = "../buggyCallgraph-20190613-93/buggyCallgraph4"
    files = os.listdir(dir)
    for file in files:
        if os.path.exists("call_graph/" + file):
            continue
        print(file)
        proj_id = file.replace(".txt", "")
        # if file == "2735.txt":
        #     continue
        if proj_id not in tongji_libs:
            sys.stderr.write(proj_id + " not in tongji\n")
            continue
        new_file = {}

        content = read_json(os.path.join(dir, file))
        for path in content:
            # print(path)
            _module = match_module(path, list(tongji_libs[proj_id].keys()))
            if _module is None:
                continue
            # print(_module)
            obj = content[path]
            temp_obj = {}
            for jar_name in obj:
                sql = "SELECT version_id,type,classifier FROM version_types WHERE jar_package_url = '" + jar_name + "'"
                type_info = database.querydb(db, sql)
                version_id = type_info[0][0]
                _type = type_info[0][1]
                classifier = type_info[0][2]
                sql = "SELECT group_str,name_str,version FROM library_versions WHERE id = " + str(version_id)
                query_result = database.querydb(db, sql)
                version = query_result[0][2]
                lib_key = query_result[0][0] + "__fdse__" + query_result[0][1] + "__fdse__" + _type
                if classifier is not None:
                    lib_key += "__fdse__" + classifier
                if lib_key in temp_obj:
                    if version not in temp_obj[lib_key]:
                        temp_obj[lib_key][version] = jar_name
                else:
                    temp_obj[lib_key] = {}
                    temp_obj[lib_key][version] = jar_name
            # print(temp_obj)
            for key in temp_obj:
                value = temp_obj[key]
                if len(value) > 1:
                    count = 0
                    for version in value:
                        # if key not in tongji[file][_module]:
                        #     count += 1
                        #     obj.pop(value[version])
                        if key in tongji_libs[proj_id][_module] and version != tongji_libs[proj_id][_module][key]:
                            count += 1
                            # print(value[version])
                            # print(obj)
                            obj.pop(value[version])
                    if count != 0 and count != len(value) and count != len(value) - 1:
                        print("@!!!!!!!!!!!!!!!!!!!!!!!")
            obj["module"] = _module
            new_file[path] = obj
        write_json("call_graph/" + file, new_file)



def match_module(path, modules):
    temp_module = None
    for _module in modules:
        if _module == "":
            if "/" not in path:
                return _module
        elif path.startswith(_module):
                if temp_module is None:
                    temp_module = _module
                elif len(_module) > len(temp_module):
                    temp_module = _module
    return temp_module

def get_all_modules():
    tongji = read_json("datas/tongji_libs.json")
    final = {}
    for file in tongji:
        print(file)
        file_obj = tongji[file]
        modules = []
        for pom in file_obj.keys():
            if not pom.endswith("/pom.xml"):
                _module = pom.replace("pom.xml", "")
                # print(_module)
            else:
                _module = pom.replace("/pom.xml", "")
            modules.append(_module)
        final[file] = modules
    write_json("datas/tongji_modules.json", final)

def call_graph_preproess():
    dir = "call_graph"
    files = os.listdir(dir)
    for file in files:
        # if file != "2590.txt":
        #     continue
        print(file)
        call_graph = read_json(os.path.join(dir, file))
        for java_file in call_graph:
            java_file_obj = call_graph[java_file]
            for jar in java_file_obj:
                if jar == "module":
                    continue
                jar_obj = java_file_obj[jar]
                for new_jar in jar_obj:
                    new_jar_obj = jar_obj[new_jar]
                    for api in new_jar_obj:
                        api_obj = new_jar_obj[api]
                        new_obj = {}
                        for method in api_obj:
                            if "<init>" in method:
                                new_method = parse_init(method)
                                new_obj[new_method] = api_obj[method]
                            else:
                                new_obj[method.replace("$", ".")] = api_obj[method]
                        new_jar_obj[api] = new_obj
        write_json("call_graph_preprocessed/" + file, call_graph)


def parse_init(method):
    array = method.replace("$", ".").split("<init>")
    if len(array) != 2:
        sys.stderr.write("len(array) != 2")
        sys.stderr.write("method" + method)
        sys.exit(0)
    prefix = array[0]
    if prefix.endswith("."):
        prefix = prefix[:-1]
    dot_index = prefix.rfind(".")
    if dot_index < 0:
        new_method = array[0] + prefix + array[1]
    else:
        new_method = array[0] + prefix[dot_index + 1:] + array[1]
    return new_method

# public new version is none: 638 : org.immutables__fdse__value__fdse__jar : 0
# public new version is none: 2594 : org.jenkins-ci__fdse__trilead-ssh2__fdse__jar : 0
# public new version is none: 2871 : org.hibernate__fdse__hibernate-core__fdse__jar : 0
# public new version is none: 1806 : org.springframework__fdse__spring-test__fdse__jar : 0
# public new version is none: 678 : org.mybatis__fdse__mybatis__fdse__jar : 0
# public new version is none: 1356 : org.apache.maven.plugin-tools__fdse__maven-plugin-annotations__fdse__jar : 0
# public new version is none: 2123 : org.apache.hive__fdse__hive-common__fdse__jar : 0


# no recommend_version: 2871 org.hibernate__fdse__hibernate-core__fdse__jar  0
# no recommend_version: 638 org.immutables__fdse__value__fdse__jar  0
# no recommend_version: 678 org.mybatis__fdse__mybatis__fdse__jar  0
# no recommend_version: 2123 org.apache.hive__fdse__hive-common__fdse__jar  0
# no recommend_version: 2594 org.jenkins-ci__fdse__trilead-ssh2__fdse__jar  0
# no recommend_version: 1356 org.apache.maven.plugin-tools__fdse__maven-plugin-annotations__fdse__jar  0
# no recommend_version: 2081 org.apache.tomcat__fdse__tomcat-coyote__fdse__jar  0
# no recommend_version: 2081 org.apache.tomcat__fdse__tomcat-dbcp__fdse__jar  0
# no recommend_version: 2081 org.apache.tomcat__fdse__tomcat-catalina__fdse__jar  0
# no recommend_version: 2081 org.apache.tomcat__fdse__tomcat-jasper__fdse__jar  0
# no recommend_version: 1517 org.slf4j__fdse__slf4j-log4j12__fdse__jar  0
# no recommend_version: 1521 junit__fdse__junit__fdse__jar  0
# no recommend_version: 1806 org.springframework__fdse__spring-test__fdse__jar  0
# 1517 1521 2081

def get_one_to_recommend():
    well_proj = [369,941,3186,85,2834,136,1602,2808,3370,4672,2004,4746,633,102,707,602,2635,217,2450,4041,1120,79,5295,1419,3600,5456,1156,1416,692,1556,21,2405,83,3005,1633,2871,125,190,678,3568,1879,3784,3323,3070,1430,2663,526,486,2980,4049,1618,1964,3105,40,1792,2123,3244,2590,32,1992,3062,1521,5330,1139,4811,2594,1784,3606,1342,3220,508,1866,361,119,4161,1356,118,1722,2826,1971,1852,3042,1098,649,123,1520,221,2307,1738,209,660,383,4202,3367,2580,2087,5033,5261,1548,2973,1439,338,532,236,1544,1468,1202,1665,2923,1423,3238,73,2880,3648,1659,534,3757,2081,610,1758,1383,1691,5538,2975,1403,2886,1238,4288,407,3822,2075,1452,60,477,349,816,2558,572,5333,1809,1582,4846,1716,700,3411,4247,853,3214,638,1806,1819,68,2792,2902,148,751,3590,3277,1878,133,3333,155,1344,30,1765,5412,3584,3088,672,1517,1770,1464,1286,1528,709,2209,1107,3068]
    # print(len(well_proj))
    # noindex = []
    json_data = read_json("datas/tongji_with_index.json")
    for project in json_data:
        # if int(project) in well_proj:
        #     continue
        # if project != "4600":
        #     continue
        print(project)
        project_obj = json_data[project]
        for lib in project_obj:
            trees = project_obj[lib]
            for tree_id in trees:
                recommend_version = None
                shared_new_jars = []
                old_jars = set()
                subtree = trees[tree_id]

                temp = subtree[-1]
                if "recommend_version" not in temp:
                    sys.stderr.write("no recommend_version: " + project + " " + lib + "  " + tree_id + "\n")
                continue

                no_index = False
                have_random = False
                total_efforts = None
                for entry in subtree:
                    if "index" in entry: # 存在index则计算
                        # have_index = True
                        resolved_version = entry["resolved_version"]
                        old_jars.add(resolved_version)
                        new_jars = set(entry["index"].keys())
                        if "type" in new_jars:
                            new_jars.remove("type")
                            if entry["index"]["type"] == "no_api_use":
                                have_random = True
                        if len(new_jars) > 0:
                            shared_new_jars.append(new_jars)
                    else:
                        no_index = True
                if no_index:
                    # # todo:
                    # if project not in noindex:
                    #     noindex.append(project)
                    #     print(project)
                    # sys.stderr.write("no index : " + project + " : " + lib + " : " + tree_id)
                    continue
                if len(shared_new_jars) > 0:
                    total = set.intersection(*shared_new_jars)
                    if len(total) > 0:
                        total_efforts = {}
                        for entry in subtree:
                            for one_new_jar in total:
                                one_new_jar_version = get_version_of_jar(one_new_jar)
                                if one_new_jar_version is None:
                                    sys.stderr.write("parse to version error : " + one_new_jar)
                                    sys.exit(0)
                                if one_new_jar_version not in total_efforts:
                                    total_efforts[one_new_jar_version] = 0
                                if "type" in entry["index"] and entry["index"]["type"] == "no_api_use":
                                    continue
                                total_efforts[one_new_jar_version] += entry["index"][one_new_jar]["index_value"]
                        min_effort = min(total_efforts.values())
                        min_effort_versions = [k for k, v in total_efforts.items() if v == min_effort]
                        if len(min_effort_versions) > 1:
                            recommend_version = get_max_version(min_effort_versions)
                            if recommend_version is None:
                                sys.stderr.write("uncomparable : " + project + " : " + lib + " : " + tree_id + "\n")
                                # sys.exit(0)
                        else:
                            recommend_version = min_effort_versions[0]
                    # todo: 没有一个公共的可推荐版本
                    else:
                        sys.stderr.write("public new version is none: " + project + " : " + lib + " : " + tree_id + "\n")
                        # sys.exit(0)
                else:
                    # todo: 任意版本可推荐 从原版本中选择
                    if have_random:
                        shared_new_jars = list(old_jars)
                        recommend_version = get_max_version(shared_new_jars)
                        if recommend_version is None:
                            sys.stderr.write("uncomparable : " + project + " : " + lib + " : " + tree_id + "\n")
                            # sys.exit(0)
                    else:
                        sys.stderr.write("no new jar : " + project + " : " + lib + " : " + tree_id + "\n")
                        sys.exit(0)
                if recommend_version is not None:
                    # if recommend_version.endswith(".jar"):
                    #     print(recommend_version)
                    #     recommend_version = get_version_of_jar(recommend_version)
                    #     if recommend_version is None:
                    #         sys.stderr.write("parse to version error")
                    #         sys.exit(0)
                    recommend_obj = {"recommend_version": recommend_version}
                    if total_efforts is not None:
                        recommend_obj["total_efforts"] = total_efforts
                    subtree.append(recommend_obj)
    # write_json_format("datas/tongji_with_index.json", json_data)


def get_new_version_index():
    lib_jar_pair = read_json("datas/lib_jar_pair.txt")

    json_data = read_json("../tongjiresult-8-5.json")
    # print(len(json_data))
    # return

    for project in json_data:
        # if project != "508":
        #     continue
        # if project != "751":
        #     continue
        if not os.path.exists("call_graph_preprocessed/" + project + ".txt"):
            # sys.stderr.write(project + "\n")
            continue
        # continue
        print(project)
        call_graph = read_json("call_graph_preprocessed/" + project + ".txt")
        proj_call_count = read_json("E:/data/RQ1/api_call/total_with_count_preprocessed/" + project + ".txt")

        project_obj = json_data[project]
        for lib in project_obj:
            # print(lib)
            trees = project_obj[lib]
            # jar_result = {}
            for tree_id in trees:
                subtree = trees[tree_id]
                for entry in subtree:
                    usePostion = entry["usePostion"]
                    version = entry["resolved_version"]
                    if not usePostion.endswith("/pom.xml"):
                        _module = usePostion.replace("pom.xml", "")
                    else:
                        _module = usePostion.replace("/pom.xml", "")
                    lib_key = version + "__fdse__" + lib
                    # if lib_key in lib_jar_pair:
                        # lib转换成jar名
                    jar_name = lib_jar_pair[lib_key]
                    index_obj = None
                    # 从call graph中寻找
                    find_in_call_graph = False
                    for java_file in call_graph:
                        # print(java_file)
                        java_file_obj = call_graph[java_file]
                        # module对应，jar包对应
                        if _module == java_file_obj["module"] and jar_name in java_file_obj:
                            jar_obj = java_file_obj[jar_name]
                            # todo: jar包下面的api为空，没有候选可推荐的jar包
                            if len(jar_obj) == 0:
                                continue
                            if not find_in_call_graph:
                                index_obj = {}  # 初始化
                            find_in_call_graph = True
                            # # todo: jar包下面的api为空，没有候选可推荐的jar包
                            # if len(jar_obj) == 0:
                            #     if len(index_obj) == 0:
                            #         index_obj["type"] = 'no_new_jar'
                            #         new_dict = {"api_count": 0, "index_value": 0, "methods_in_modify_api": [],
                            #                     "delete_api_count": 0, "modify_api_count": 0}
                            #         index_obj[jar_name] = new_dict
                            for api in jar_obj:
                                api_obj = jar_obj[api]
                                # todo: 推荐的new jar为空，说明没有新版本可推
                                if len(api_obj) == 0:
                                    if len(index_obj) == 0:
                                        index_obj["type"] = 'no_new_jar'
                                        new_dict = {"api_count": 0,"index_value": 0,"methods_in_modify_api": [],"delete_api_count": 0,"modify_api_count": 0}
                                        index_obj[jar_name] = new_dict
                                        # index_obj[jar_name] = {"api_count": 0,"index_value": 0,"methods_in_modify_api": [],"delete_api_count": 0,"modify_api_count": 0},
                                    elif index_obj["type"] != 'no_new_jar':
                                        sys.stderr.write('no_new_jar type conflict : ' + java_file + "(" + jar_name + ")")
                                        sys.exit(0)
                                for new_jar in api_obj:
                                    if new_jar not in index_obj:
                                        index_obj[new_jar] = {}
                                        # index_obj[new_jar]["api_count"] = 1
                                        # index_obj[new_jar]["api_count"] = proj_call_count[java_file][api]
                                        index_obj[new_jar]["api_count"] = [proj_call_count[java_file][api]]
                                        index_obj[new_jar]["delete_api_count"] = []
                                        index_obj[new_jar]["modify_api_count"] = []
                                        index_obj[new_jar]["methods_in_modify_api"] = []
                                        index_obj[new_jar]["index_value"] = 0
                                    else:
                                        # index_obj[new_jar]["api_count"] += 1
                                        # index_obj[new_jar]["api_count"] += proj_call_count[java_file][api]
                                        index_obj[new_jar]["api_count"].append(proj_call_count[java_file][api])

                                    new_jar_obj = api_obj[new_jar]
                                    # todo: api can't find
                                    if api not in new_jar_obj:
                                        if len(new_jar_obj) == 0:
                                            # index_obj[new_jar]["delete_api_count"] += 1
                                            # index_obj[new_jar]["index_value"] += 1
                                            # index_obj[new_jar]["delete_api_count"] += proj_call_count[java_file][api]
                                            index_obj[new_jar]["delete_api_count"].append(proj_call_count[java_file][api])
                                            index_obj[new_jar]["index_value"] += proj_call_count[java_file][api]
                                        else:
                                            sys.stderr.write('KeyError : ' + java_file + "(" + jar_name + ":" + api + ")")
                                            sys.exit(0)
                                    elif new_jar_obj[api] == "jar not found" or new_jar_obj[api] == "class not found" or \
                                            new_jar_obj[api] == "method not found" or new_jar_obj[api] == "jdk method":
                                        # index_obj[new_jar]["delete_api_count"] += 1
                                        # index_obj[new_jar]["index_value"] += 1
                                        # index_obj[new_jar]["delete_api_count"] += proj_call_count[java_file][api]
                                        index_obj[new_jar]["delete_api_count"].append(proj_call_count[java_file][api])
                                        index_obj[new_jar]["index_value"] += proj_call_count[java_file][api]
                                    else:
                                        total = 0
                                        delete = 0
                                        modify = 0
                                        add = 0
                                        for method in new_jar_obj:
                                            # if method != api:
                                            total += 1
                                            if new_jar_obj[method] == "jar not found" or new_jar_obj[
                                                method] == "class not found" or new_jar_obj[
                                                method] == "method not found" or new_jar_obj[method] == "jdk method":
                                                delete += 1
                                            elif new_jar_obj[method] == "modify":
                                                modify += 1
                                            elif new_jar_obj[method] == "add":
                                                add += 1
                                        if delete != 0 or modify != 0 or add != 0:
                                            # index_obj[new_jar]["modify_api_count"] += 1
                                            # index_obj[new_jar]["methods_in_modify_api"].append([total, delete, modify, add])
                                            # index_obj[new_jar]["modify_api_count"] += proj_call_count[java_file][api]
                                            index_obj[new_jar]["modify_api_count"].append(proj_call_count[java_file][api])
                                            for i in range(0,proj_call_count[java_file][api]):
                                                index_obj[new_jar]["methods_in_modify_api"].append([total, delete, modify, add])
                                            if total > 0:
                                                # index_obj[new_jar]["index_value"] += (delete + modify + add) / total
                                                index_obj[new_jar]["index_value"] += (delete + modify + add) / total * proj_call_count[java_file][api]
                    # todo : module 和 jar包对应不上，说明call graph中没有用到该第三方库的API，则index={}
                    if not find_in_call_graph:
                        index_obj = {}
                        index_obj["type"] = 'no_api_use'
                    entry["index"] = index_obj

    write_json_format("datas/tongji_with_index.json", json_data)

def json_to_format():
    dir = "call_graph_preprocessed"
    files = os.listdir(dir)
    for file in files:
        # if file != "1383.txt":
        #     continue
        data = read_json(os.path.join(dir, file))
        write_json_format("format/" + file.replace(".txt", ".json"), data)

def lib_jar_pair_reverse():
    lib_jar_pair = read_json("datas/lib_jar_pair.txt")
    new_dict = {v: k for k, v in lib_jar_pair.items()}
    write_json("datas/lib_jar_pair_reverse.txt", new_dict)

def add_lib_pair():
    lib_jar_pair = read_json("datas/lib_jar_pair.txt")
    print(len(lib_jar_pair))

    lib_jar_pair["2.0.0-DP.2__fdse__com.netflix.turbine__fdse__turbine-core__fdse__jar"] = "turbine-core-2.0.0-DP.2.jar"
    print(len(lib_jar_pair))
    write_json("datas/lib_jar_pair.txt", lib_jar_pair)
    # 1.7.6-cdh5.15.0__fdse__org.apache.avro__fdse__avro__fdse__jar

# get_lib_in_tongjiresult()
# get_pair()
# add_lib_pair()
# get_all_modules()
# lib_jar_pair_reverse()

# filter_lib_in_callgraph()
# call_graph_preproess()
# get_new_version_index()
get_one_to_recommend()
# json_to_format()

# data = read_json("../actionresult.json")
# write_json("E:/data/multiversion/unify/actionresult.json", data)
# print(parse_init("org.springframework.core.io.DefaultResourceLoader$ClassPathContextResource.<init>(java.lang.String,java.lang.ClassLoader)"))