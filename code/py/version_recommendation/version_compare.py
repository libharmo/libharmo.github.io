import re
import sys

from version_recommendation import database
from version_recommendation.file_util import read_json, write_json

db = database.connectdb()

def compare(version1, version2):
    # print(version1 + "   " + version2)
    v1_array = version1.split(".")
    v2_array = version2.split(".")
    length = min(len(v1_array), len(v2_array))
    for i in range(0, length):
        # print(v1_array[i] + "  " + v2_array[i])
        if v1_array[i].isdigit() and v2_array[i].isdigit():
            result = compare_two_numstr(v1_array[i], v2_array[i])
            if result != 0:
                return result
        else:
            pattern = '(\d+)(.*)'
            m1 = re.match(pattern, v1_array[i])
            m2 = re.match(pattern, v2_array[i])
            if m1 is not None and m2 is not None:
                # print(m1.group(1) + "  " + m2.group(1))
                result = compare_two_numstr(m1.group(1), m2.group(1))
                if result != 0:
                    return result
                else:
                    if len(m1.group(2)) != 0 and len(m2.group(2)) != 0:
                        if m1.group(2) != m2.group(2):
                            return None
                    elif len(m1.group(2)) == 0:
                        return 1
                    else:
                        return -1
            else:
                return None
    return 0

def compare_two_numstr(str1,str2):
    if str1 == str2:
        return 0
    num1 = int(str1)
    num2 = int(str2)
    if num1 > num2:
        return 1
    elif num1 < num2:
        return -1
    else:
        return 0

def get_max_version(version_list):
    max_version = None
    for ver in version_list:
        if max_version is None:
            max_version = ver
        else:
            com_result = compare(max_version, ver)
            if com_result is None:
                sys.stderr.write("uncomparable : " + max_version + " " + ver + "\n")
                return None
            elif com_result == -1:
                max_version = ver
    return max_version

def jar2version(jar_name):
    pattern = '-\d+'
    if not jar_name.endswith(".jar"):
        sys.stderr.write("not jar : " + jar_name)
        sys.exit(0)
    if jar_name.endswith("-tests.jar"):
        jar_name = jar_name[:-10]
    else:
        jar_name = jar_name[:-4]
    # print(jar_name)
    m = re.search(pattern, jar_name)
    if m is None:
        sys.stderr.write("pattern not match : " + jar_name + "\n")
        sys.exit(0)
    return jar_name[m.start()+1:]

def get_version_of_jar(version):
    lib_jar_pair_reverse = read_json("datas/lib_jar_pair_reverse.txt")
    if version in lib_jar_pair_reverse:
        return lib_jar_pair_reverse[version].split("__fdse__")[0]
    try:
        sql = "SELECT version_id FROM version_types WHERE jar_package_url = '" + version + "'"
        version_id = database.querydb(db, sql)[0][0]
        sql = "SELECT version FROM library_versions WHERE id = " + str(version_id) + ""
        new_version = database.querydb(db, sql)[0][0]
    except:
        new_version = jar2version(version)
        print("jar2version: " + version + " -> " + new_version)
    return new_version

def version2jar(version_lib):
    lib_jar_pair = read_json("datas/lib_jar_pair.txt")
    if version_lib in lib_jar_pair:
        return lib_jar_pair[version_lib]
    else:
        lib_array = version_lib.split("__fdse__")
        version = lib_array[0]
        groupId = lib_array[1]
        artifactId = lib_array[2]
        _type = lib_array[3]
        try:
            sql = "SELECT id FROM library_versions WHERE group_str = '" + groupId + "' and name_str = '" + artifactId + "' and version = '" + version + "'"
            version_id = database.querydb(db, sql)[0][0]
            # print("version id : " + str(version_id))
            if len(lib_array) > 4:
                classifier = lib_array[4]
                sql = "SELECT jar_package_url FROM version_types WHERE (version_id = " + str(version_id) + " or version_id2 = " +str(version_id)+ ") and type = '" +_type+ "' and classifier = '" +classifier+ "'"
            else:
                sql = "SELECT jar_package_url FROM version_types WHERE (version_id = " + str(version_id) + " or version_id2 = " +str(version_id)+ ") and type = '" +_type+ "' and (classifier is null or classifier='')"
            jar_package_url = database.querydb(db, sql)[0][0]
            lib_jar_pair[version_lib] = jar_package_url
            print("jar2version: " + version_lib + " -> " + jar_package_url)
            write_json("datas/lib_jar_pair.txt", lib_jar_pair)
            return jar_package_url
        except:
            sys.stderr.write("Can't find jar name for : " + version_lib + "\n")
            sys.exit(0)

def add_lib_pair():
    lib_jar_pair = read_json("datas/lib_jar_pair.txt")
    print(len(lib_jar_pair))
    lib_jar_pair["3.0.20100224__fdse__javax.servlet__fdse__servlet-api__fdse__jar"] = "servlet-api-3.0.20100224.jar"
    print("jar2version: " + "3.11.0-SNAPSHOT__fdse__com.squareup.okhttp__fdse__mockwebserver__fdse__jar" + " -> " + "mockwebserver-3.11.0-20180713.034253-175.jar")
    print(len(lib_jar_pair))
    write_json("datas/lib_jar_pair.txt", lib_jar_pair)

# uncomparable : 2.6.0-cdh5.12.2 2.6.0-cdh5.13.1uncomparable : 4600 : org.apache.hadoop__fdse__hadoop-aws__fdse__jar : 0
# uncomparable : 2.6.0-cdh5.10.1 2.6.0-cdh5.12.2uncomparable : 4600 : org.apache.hadoop__fdse__hadoop-client__fdse__jar : 0
# uncomparable : 2.6.0-cdh5.10.1 2.6.0-cdh5.12.2uncomparable : 4600 : org.apache.hadoop__fdse__hadoop-minicluster__fdse__jar : 0
# uncomparable : 4.10.3-cdh5.8.5 4.10.3-cdh5.5.6uncomparable : 4600 : org.apache.solr__fdse__solr-test-framework__fdse__jar : 0
# uncomparable : 3.4.5-cdh5.9.2 3.4.5-mapr-1503uncomparable : 4600 : org.apache.zookeeper__fdse__zookeeper__fdse__jar : 0
# uncomparable : 1.2.0-cdh5.14.0 1.2.0-cdh5.15.0uncomparable : 4600 : org.apache.hbase__fdse__hbase-testing-util__fdse__jar : 0
# uncomparable : 1.1.0-cdh5.4.11 1.1.0-cdh5.5.6uncomparable : 4600 : org.apache.hive.hcatalog__fdse__hive-hcatalog-streaming__fdse__jar : 0
# uncomparable : 1.7.6-cdh5.15.0 1.7.6-cdh5.14.0uncomparable : 4600 : org.apache.avro__fdse__avro__fdse__jar : 0
# uncomparable : 1.6.0-cdh5.12.2 1.6.0-cdh5.10.1uncomparable : 4600 : org.apache.flume__fdse__flume-ng-configuration__fdse__jar : 0
# uncomparable : 1.7.6-cdh5.15.0 1.7.6-cdh5.14.0uncomparable : 4600 : org.apache.avro__fdse__avro-mapred__fdse__jar__fdse__hadoop2 : 0

# hadoop-common-3.0.0-tests.jar  3.0.0  type=test-jar
# hadoop-common-3.1.0.jar   3.1.0
# httpclient-4.2.1-atlassian-2.jar   4.2.1-atlassian-2
# hadoop-common-3.0.0-beta1.jar 3.0.0-beta1
# hadoop-common-3.2.0-20180809.000209-1000.jar  3.2.0-SNAPSHOT
# hadoop-common-2.6.0-cdh5.7.0.jar   2.6.0-cdh5.7.0
# hadoop-common-2.7.3.2.6.1.0-129.jar   2.7.3.2.6.1.0-129
# spark-core_2.10-2.2.0.jar   spark-core_2.10   2.2.0
# javax.ws.rs-api-2.0-m15.jar  2.0-m15
# guava-23.1-jre.jar   23.1-jre
# hbase-server-2.0.0-beta-1.jar   2.0.0-beta-1
# hbase-client-1.1.2.2.6.2.1-1.jar  1.1.2.2.6.2.1-1
# guava-23.0-android.jar   23.0-android

# print(compare("3.4.5-cdh5.9.2", "3.4.5-mapr-1503"))
# total_efforts = {"test1":131, "test4":43,"test3":531,"test2":43}
# min = min(total_efforts.values())
# print(min)
# min_list = [k for k, v in total_efforts.items() if v == min]
# print(min_list)
# print(jar2version("guava-23.0-android.jar"))
# add_lib_pair()