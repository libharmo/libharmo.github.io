from collections import OrderedDict
import os
import sys

import numpy as np
from reportlab.lib import colors, utils
from reportlab.lib.styles import getSampleStyleSheet
from reportlab.lib.units import inch
from reportlab.platypus import Paragraph, SimpleDocTemplate, Table, Image, ListFlowable

from version_recommendation.file_util import read_json, write_json, write_json_format
from version_recommendation.version_compare import version2jar

stylesheet = getSampleStyleSheet()
normalStyle = stylesheet['Normal']

large_images = {}

def get_proj_dict():
    json_data = read_json("E:/data/200_plus.txt")
    # print(len(json_data))
    proj_dict = {}
    for entry in json_data:
        id = entry["id"]
        name = entry["name"].replace("__fdse__", "/")
        proj_dict[str(id)] = name
    return proj_dict

def generate_report_pdf():
    doc = SimpleDocTemplate('E:/lab/ICSE/report/test.pdf')
    # frame = doc.getFrame('FirstL')
    # width = frame._aW

    border_style = [
        ('BOX', (0, 0), (-1, -1), 0.5, colors.brown)]
    overview_style = [
        ('GRID', (0, 0), (-1, -1), 0.3, colors.grey),
        ('VALIGN', (0, 0), (-1, -1), 'MIDDLE')]
    table_style = [
        ('GRID', (0, 0), (-1, -1), 0.3, colors.grey),
        ('BACKGROUND', (0, 0), (-1, 0), colors.lightgrey),
        ('VALIGN', (0, 0), (-1, -1), 'MIDDLE')]

    story = []

    stylesheet = getSampleStyleSheet()
    normalStyle = stylesheet['Normal']

    # border_box
    one_box = []
    count = 1
    # jar box
    # color : beige 米白 bisque 橘黄 blueviolet 蓝紫色 brown 实木  cadetblue军蓝 chocolate darkcyan深绿 darksalmon 深肉桂 darkseagreen深海藻绿  darkturquoise石英灰
    jar_box = Table([[Paragraph('<para leading=18 fontSize=14 align=center><b><font color = "darksalmon">io.netty:netty-transport:jar</font></b></para>', normalStyle)]],
                    style=[('BOX',(0,0),(-1,-1),0.8,colors.darksalmon)])
    doc.build(story)

def get_module(path):
    if not path.endswith("/pom.xml"):
        _module = path.replace("pom.xml", "")
    else:
        _module = path.replace("/pom.xml", "")
    return _module

def read_actions():
    good = [3584, 3590, 1548, 526, 3600, 3088, 532, 2580, 21, 1556, 534, 3606, 2075, 2590, 30, 32, 3105, 2594, 2087, 40, 1582, 572, 60, 4672, 3648, 4161, 1602, 68, 73, 1098, 2123, 2635, 3068, 79, 1618, 83, 1107, 3070, 1633, 610, 102, 2663, 4202, 3186, 1139, 118, 119, 633, 123, 1659, 125, 638, 1665, 1156, 133, 136, 649, 4746, 5261, 3214, 3220, 660, 148, 4247, 1691, 155, 672, 2209, 678, 3238, 3244, 3757, 5295, 1202, 1716, 692, 1722, 700, 190, 4288, 707, 709, 3784, 1738, 4811, 3277, 209, 5330, 5333, 1238, 217, 221, 1758, 1765, 2792, 1770, 236, 3822, 4846, 751, 2808, 1784, 3323, 1792, 2307, 3333, 2826, 1806, 1809, 2834, 1819, 5412, 3367, 3370, 2871, 1852, 1342, 2880, 1344, 2886, 1866, 1356, 5456, 338, 3411, 853, 2902, 1879, 1878, 2405, 1383, 361, 2923, 369, 1403, 383, 1416, 1419, 2450, 1430, 407, 2973, 1439, 2975, 5538, 2980, 5033, 1964, 941, 1452, 1971, 1468, 3005, 1992, 4041, 4049, 2004, 477, 3042, 486, 3568, 1520, 3062, 1528, 508, 2558]
    total_index = read_json("datas/tongji_with_index.json")
    total_action = read_json("../action-8-6.json")
    total_ununified = read_json("../action-ununified-8-6.json")

    for proj_id in total_action:
        # if not os.path.exists("call_graph_preprocessed/" + proj_id + ".txt"):
        #     continue
        # # jar-lib problem
        if proj_id == "1120":
            continue
        # # bug
        # if proj_id == "1618":
        #     continue
        # if proj_id != "1286":
        #     continue
        print(proj_id)
        # call_graph = read_json("call_graph_preprocessed/" + proj_id + ".txt")

        proj_output = {}
        proj_multi_output = {}
        inconsistent_lib = 0
        modules_related = 0
        multilib_versions = 0

        for jar in total_action[proj_id]:
            # if jar != "org.easymock__fdse__easymock__fdse__jar":
            #     continue
            inconsistent_lib += 1
            # module_versions = collections.OrderedDict()
            # module_properties = collections.OrderedDict()
            tree_actions = OrderedDict()
            tree_indices = OrderedDict()
            module_versions = []
            module_properties = []
            # tree_actions = []
            # tree_indices = []
            # tree_files = collections.OrderedDict()
            desc_dict = None

            tree_numbers = {}
            index = 0
            for tree_id in total_action[proj_id][jar]:
                if tree_id == "desc_dict":
                    desc_dict = total_action[proj_id][jar][tree_id]
                    continue
                subtree = total_action[proj_id][jar][tree_id]
                temp = subtree[-1]
                no_action = False
                if "action_update_define_version_value" not in temp:
                    sys.stderr.write(proj_id + " " + jar + " " + tree_id + "\n")
                    tree_actions[tree_id] = "no action"
                    no_action = True
                action_jar = None
                if not no_action:
                    action_pos = temp["action_update_define_pos"]
                    action_version = temp["action_update_define_version_value"].split("=")[1]
                    action_jar = version2jar(action_version + "__fdse__" + jar)

                # subtree_index = collections.OrderedDict()
                subtree_index = []
                # subtree_file = collections.OrderedDict()
                subtree_number = None
                for i in range(0,len(subtree)):
                    if "action_update_define_version_value" in subtree[i] or "skip" in subtree[i]:
                        continue
                    usePostion = subtree[i]["usePostion"]
                    _module = get_module(usePostion)
                    # module_versions[_module] = subtree[i]["resolved_version"]
                    # module_properties[_module] = subtree[i]["propertyName"]
                    module_versions.append([_module,subtree[i]["resolved_version"]])
                    module_properties.append([_module,subtree[i]["propertyName"]])
                    index += 1
                    if subtree_number is None:
                        subtree_number = [index]

                    subindex = total_index[proj_id][jar][tree_id][i]
                    if get_module(subindex["usePostion"]) != _module:
                        sys.stderr.write("module conflict: " + proj_id + " " + jar + " " + tree_id  + " " + str(i) + "\n")
                        sys.exit(0)
                    if action_jar is None:
                        # subtree_index[_module] = []
                        subtree_index.append([_module, []])
                    elif "type" in subindex["index"] and subindex["index"]["type"] == "no_api_use":
                        # subtree_index[_module] = [0, 0, 0, 0]
                        subtree_index.append([_module, [0, 0, 0, 0]])
                    else:
                        api_count = subindex["index"][action_jar]["api_count"]
                        modify_api_count = subindex["index"][action_jar]["modify_api_count"]
                        delete_api_count = subindex["index"][action_jar]["delete_api_count"]
                        if type(api_count) == list:
                            api_count = [len(api_count),sum(api_count)]
                        if type(modify_api_count) == list:
                            modify_api_count = [len(modify_api_count),sum(modify_api_count)]
                        if type(delete_api_count) == list:
                            delete_api_count = [len(delete_api_count),sum(delete_api_count)]
                        # api_count = [len(subindex["index"][action_jar]["api_count"]),sum(subindex["index"][action_jar]["api_count"])] if type(subindex["index"][action_jar]["api_count"]) == list else subindex["index"][action_jar]["api_count"]
                        # modify_api_count = [len(subindex["index"][action_jar]["modify_api_count"]),sum(subindex["index"][action_jar]["modify_api_count"])]
                        # delete_api_count = [len(subindex["index"][action_jar]["delete_api_count"]),sum(subindex["index"][action_jar]["delete_api_count"])]
                        stability = api_count[1] - subindex["index"][action_jar]["index_value"] if type(api_count) == list else api_count - subindex["index"][action_jar]["index_value"]
                        # subtree_index[_module] = [api_count, delete_api_count, modify_api_count, stability]
                        subtree_index.append([_module, [api_count, delete_api_count, modify_api_count, stability]])

                    if no_action:
                        continue

                    # files related
                    # jar_name = version2jar(subtree[i]["resolved_version"] + "__fdse__" + jar)
                    # for java_file in call_graph:
                    #     java_file_obj = call_graph[java_file]
                    #     # module对应，jar包对应
                    #     if _module == java_file_obj["module"] and jar_name in java_file_obj:
                    #         if _module not in subtree_file:
                    #             subtree_file[_module] = []
                    #         subtree_file[_module].append(java_file)

                if subtree_number is not None:
                    subtree_number.append(index)
                tree_numbers[tree_id] = subtree_number
                tree_indices[tree_id] = subtree_index
                # tree_files[tree_id] = subtree_file
                if not no_action:
                    tree_actions[tree_id] = {"action_pos": action_pos, "action_version": action_version}

            modules_related += len(module_versions)
            multilib_versions += len(set(dict(module_versions).values()))
            jar_output = {}
            jar_output["tree_numbers"] = tree_numbers
            jar_output["module_versions"] = module_versions
            jar_output["module_properties"] = module_properties
            jar_output["tree_actions"] = tree_actions
            jar_output["tree_indices"] = tree_indices
            # jar_output["tree_files"] = tree_files
            jar_output["desc_dict"] = desc_dict
            proj_multi_output[jar] = jar_output
        # print(json.dumps(proj_multi_output))

        proj_ununified_output = {}
        if proj_id in total_ununified:
            proj_ununified_output = read_ununified_proj(total_ununified[proj_id])

        proj_output["inconsistent_lib"] = inconsistent_lib
        proj_output["modules_related"] = modules_related
        proj_output["multilib_versions"] = multilib_versions
        proj_output["multilib_output"] = proj_multi_output
        proj_output["ununified_output"] = proj_ununified_output
        write_json("datas/pdf_data/" + proj_id + ".json", proj_output)

    for proj_id in total_ununified:
        # if proj_id == "1286" or proj_id == "1423" or proj_id == "816" or proj_id == "85":
        #     continue
        # # jar-lib problem
        # if proj_id == "349" or proj_id == "1544" or proj_id == "1120" or proj_id == "602" or proj_id == "1464" or proj_id == "4600":
        #     continue
        # # bug
        # if proj_id == "1618":
        #     continue
        if not os.path.exists("datas/pdf_data/" + proj_id + ".json"):
            proj_output = {}
            proj_ununified_output = read_ununified_proj(total_ununified[proj_id])
            proj_output["ununified_output"] = proj_ununified_output
            write_json("datas/pdf_data_unify/" + proj_id + ".json", proj_output)


def read_ununified_proj(proj_data):
    proj_ununified_output = {}
    for jar in proj_data:
        # module_versions = OrderedDict()
        # module_properties = OrderedDict()
        tree_actions = OrderedDict()
        module_versions = []
        module_properties = []
        tree_ids = []
        desc_dict = None

        tree_numbers = {}
        index = 0
        for tree_id in proj_data[jar]:
            if tree_id == "desc_dict":
                desc_dict = proj_data[jar][tree_id]
                continue
            tree_ids.append(tree_id)
            subtree = proj_data[jar][tree_id]
            temp = subtree[-1]
            if "action_update_define_version_value" not in temp:
                # sys.stderr.write(proj_id + " " + jar + " " + tree_id + "\n")
                tree_actions[tree_id] = "no action"
            subtree_number = None
            for i in range(0, len(subtree)):
                if "action_update_define_version_value" in subtree[i] or "skip" in subtree[i]:
                    continue
                usePostion = subtree[i]["usePostion"]
                _module = get_module(usePostion)
                # module_versions[_module] = subtree[i]["resolved_version"]
                # module_properties[_module] = subtree[i]["propertyName"]
                module_versions.append([_module,subtree[i]["resolved_version"]])
                module_properties.append([_module,subtree[i]["propertyName"]])
                index += 1
                if subtree_number is None:
                    subtree_number = [index]
            if subtree_number is not None:
                subtree_number.append(index)
            tree_numbers[tree_id] = subtree_number
        jar_output = {}
        jar_output["module_versions"] = module_versions
        jar_output["module_properties"] = module_properties
        jar_output["desc_dict"] = desc_dict
        jar_output["tree_numbers"] = tree_numbers
        jar_output["tree_actions"] = tree_actions
        proj_ununified_output[jar] = jar_output
    return proj_ununified_output

def data2pdf():
    proj_dict = get_proj_dict()
    table_style = [('GRID', (0, 0), (-1, -1), 0.3, colors.grey),('BACKGROUND', (0, 0), (-1, 0), colors.lightgrey), ('VALIGN', (0, 0), (-1, -1), 'MIDDLE')]
    jar_box_style = [('BOX', (0, 0), (-1, -1), 0.8, colors.darksalmon)]

    dir = "datas/pdf_data_unify"
    output_dir = "datas/pdf_unify/"
    large_images = read_json("datas/large_images.json")
    # dir = "datas/pdf_data"
    # output_dir = "datas/pdf/"
    files = os.listdir(dir)
    # error_proj = ['1342', '1238', '1107', '1344', '1356', '136', '1383', '1419', '1468', '148', '1520', '1582', '1716', '1758', '1770', '1784', '1866', '1878', '2004', '2075', '217', '2209', '236', '2635', '2834', '2886', '2973', '2975', '3105', '3186', '32', '3214', '3220', '3277', '3568', '3606', '3648', '3822', '383', '4049', '407', '4288', '5033', '508', '5538', '572', '60', '692', '709', '751', '83']
    # print(len(error_proj))
    error_proj = []

    for file in files:
        proj_name = proj_dict[file.replace(".json", "")].replace("/", " ")
        print(proj_name)
        # if os.path.exists(output_dir + file.replace(".json", ".pdf")):
        #     continue
        if os.path.exists(output_dir + proj_name + ".pdf"):
            continue
        print(file)
        proj_data = read_json(os.path.join(dir, file))
        story = []
        # title
        rpt_title = title("Project : " + proj_dict[file.replace(".json", "")], 1)
        story.append(Paragraph(rpt_title, normalStyle))

        # definition
        add_definition(story)

        # overview
        inconsistent_lib_cnt = 0
        if "inconsistent_lib" in proj_data:
            inconsistent_lib_cnt = proj_data["inconsistent_lib"]
        add_overview(story, normalStyle, inconsistent_lib_cnt, len(proj_data["ununified_output"]))

        if "multilib_output" in proj_data:
            story.append(Paragraph(title('II.  Inconsistent Libraries', 2), normalStyle))

            multi_data = proj_data["multilib_output"]
            multi_jar_cnt = 0
            for jar in multi_data:
                multi_jar_cnt += 1
                # if jar != "org.easymock__fdse__easymock__fdse__jar":
                #     continue
                one_box = []
                # jar box
                one_box.append([Paragraph(title(str(multi_jar_cnt) + '. ' + ":".join(jar.split("__fdse__")[:2]), 3), normalStyle)])

                # Summery
                desc_dict = multi_data[jar]["desc_dict"]
                # add_summary(one_box,desc_dict,None,None,":".join(desc_dict["libraryname"].split("__fdse__")[:2]),str(len(set(multi_data[jar]["module_versions"].keys()))),True)
                add_summary(one_box,desc_dict,None,None,":".join(desc_dict["libraryname"].split("__fdse__")[:2]),str(len(set(dict(multi_data[jar]["module_versions"]).values()))),True)

                # Multiple versions
                multi_versions(one_box, table_style, multi_data[jar])

                # Unify Recommendation
                # title2 = '<para autoLeading="off" fontSize=14 align=left leading=16 textColor="black"><b><font>Unify Recommendation</font></b></para>'
                # one_box.append([Paragraph(title2, normalStyle)])
                # one_box.append([Paragraph(title("Harmonization Recommendation", 4), normalStyle)])
                one_box.append([Paragraph('<para fontSize=13 face="Times" leading=17><b> Harmonization Recommendation</b></para>', normalStyle)])


                # subtrees
                for tree_id in multi_data[jar]["tree_actions"]:
                    action_version = None if multi_data[jar]["tree_actions"][tree_id] == "no action" else multi_data[jar]["tree_actions"][tree_id]["action_version"]
                    action_pos = None if multi_data[jar]["tree_actions"][tree_id] == "no action" else multi_data[jar]["tree_actions"][tree_id]["action_pos"]

                    # one tree
                    tree_indices = multi_data[jar]["tree_indices"][tree_id]
                    subtree_numbers = multi_data[jar]["tree_numbers"][tree_id]
                    # modules_list = list(OrderedDict(tree_indices).keys())
                    # modules_str = ', '.join(modules_list[:50]) + '<br/> ···<br/>' + modules_list[-1] + "<br/>" if len(modules_list) > 50 else ', '.join(modules_list)
                    modules_str = "The inconsistent library versions in Modules " + str(subtree_numbers[0]) + "-" + str(subtree_numbers[1])
                    # content = modules_str if multi_data[jar]["tree_actions"][tree_id] == "no action" else modules_str + ' → ' + action_version
                    content = modules_str + ' can not be harmonized into a suitable version.' if multi_data[jar]["tree_actions"][
                                                 tree_id] == "no action" else modules_str + ' are harmonized into version ' + action_version + '.'
                    text = '<para fontSize=12 align=left textColor = "darkblue" leading=13>' + content + '</para>'
                    one_box.append([Paragraph(text, normalStyle)])
                    one_box.append([Paragraph('<para leading=6> &nbsp;<br/></para>', normalStyle)])

                    if multi_data[jar]["tree_actions"][tree_id] == "no action":
                        # text = '<para autoLeading="off" fontSize=9 align=left>There is no new version to recommend.</para>'
                        # one_box.append([Paragraph(text, normalStyle)])
                        continue

                    # add_image()
                    add_image(one_box, file, jar, tree_id)

                    # efforts
                    add_efforts(one_box, table_style, tree_indices,subtree_numbers)
                    # # files related
                    # add_files_related(one_box, table_style, multi_data[jar]["tree_files"][tree_id])

                story = story + list(np.transpose(one_box)[0])
                # one_box.append([Paragraph('<para align=center leading=2><br/><br/></para>', normalStyle)])
                # border_table = Table(one_box)
                # border_table.setStyle(border_style)
                # story.append(border_table)
                story.append(Paragraph('<para><br/></para>', normalStyle))

        ununified_data = proj_data["ununified_output"]
        if len(ununified_data) > 0:
            if "multilib_output" in proj_data:
                story.append(Paragraph(title('III.  False Consistent Libraries', 2), normalStyle))
            else:
                story.append(Paragraph(title('II.  False Consistent Libraries', 2), normalStyle))
        ununified_jar_cnt = 0
        for jar in ununified_data:
            ununified_jar_cnt += 1
            # border_box
            one_box = []
            # jar box
            one_box.append([Paragraph(title(str(ununified_jar_cnt) + '. ' + ":".join(jar.split("__fdse__")[:2]), 3), normalStyle)])

            # Summery
            unify_version = ununified_data[jar]["module_versions"][0][1]
            # unify_version = None
            # for _module in ununified_data[jar]["module_versions"]:
            #     unify_version = ununified_data[jar]["module_versions"][_module]
            #     break
            desc_dict = ununified_data[jar]["desc_dict"]
            # add_summary(one_box,desc_dict, unify_version, ":".join(desc_dict["libraryname"].split("__fdse__")[:2]),str(len(set(ununified_data[jar]["module_versions"].keys()))),None,False)
            add_summary(one_box, desc_dict, str(len(ununified_data[jar]["module_versions"])),unify_version, ":".join(desc_dict["libraryname"].split("__fdse__")[:2]), None, False)

            # Module versions
            add_ununified_module_version(one_box, table_style, ununified_data[jar])

            # trees
            tree_numbers = ununified_data[jar]["tree_numbers"]
            for tree_id in tree_numbers:
                if tree_id in ununified_data[jar]["tree_actions"] and ununified_data[jar]["tree_actions"][tree_id] == "no action":
                    text = '<para fontSize=12 align=left textColor = "darkblue" leading=12>The false consistent library versions in Modules ' + str(tree_numbers[tree_id][0]) + "-" + str(tree_numbers[tree_id][1]) + ' can not reference a common property on a local POM file.</para>'
                    one_box.append([Paragraph(text, normalStyle)])
                    one_box.append([Paragraph('<para leading=6> &nbsp;<br/></para>', normalStyle)])
                    continue
                content = "The false consistent library versions in Modules " + str(tree_numbers[tree_id][0]) + "-" + str(tree_numbers[tree_id][1])
                text = '<para fontSize=12 align=left textColor = "darkblue" leading=10>' + content + '</para>'
                one_box.append([Paragraph(text, normalStyle)])
                one_box.append([Paragraph('<para leading=6> &nbsp;<br/></para>', normalStyle)])
                # image
                add_image(one_box, file, jar, tree_id)

            story = story + list(np.transpose(one_box)[0])
            # border_table = Table(one_box)
            # border_table.setStyle(border_style)
            # story.append(border_table)
            story.append(Paragraph('<para><br/><br/></para>', normalStyle))

        doc = SimpleDocTemplate(output_dir + proj_name + ".pdf")
        # doc = SimpleDocTemplate(output_dir + file.replace(".json", ".pdf"),pagesize=A0)
        # try:
        doc.build(story)
        # except:
        #     error_proj.append(file.replace(".json", ""))
        #     sys.stderr.write(file +" error\n")
    print(error_proj)
    write_json_format("datas/large_images.json", large_images)

def add_definition(story):
    story.append(Paragraph('<para><br/><br/>We introduce some terms for avoiding confusions.</para>', normalStyle))
    # title2 = title('Definition:', 2)
    # story.append(Paragraph(title2, normalStyle))
    delist = ListFlowable(
        [
            Paragraph('<b>Explicit version declaration in Maven: </b>a library version is declared as a hard-coded value in version tag, e.g., &lt;version&gt;2.5&lt;/version&gt;.', normalStyle),
            Paragraph('<b>Implicit version declaration in Maven: </b>a library version is declared as a referenced value in version tag through a declared property, e.g., &lt;version&gt;${guava.version}&lt;/version&gt;.', normalStyle),
            Paragraph('<b>Inconsistent library: </b>a library whose multiple versions are used in multiple modules of a project; e.g., module A declares guava 2.5 but module B declares guava 2.6.',normalStyle),
            Paragraph('<b>False consistent library: </b> a library that is used in multiple modules of a project with the same version declared separately; e.g., module A declares guava 2.5 explicitly and module B declares guava 2.5 explicitly. Therefore, such a consistency is likely to turn into inconsistency when there is an incomplete library update.',normalStyle),
            # Paragraph('<b>Property: </b>property tag in <font face="Times-Italic">pom</font>, <font face="Times-Italic">maven\'s</font> term for <font face="Times-Italic">variable</font>.',normalStyle),
        ],
        bulletType='bullet',
    )
    story.append(delist)
    story.append(Paragraph('<para><br/><br/></para>', normalStyle))

def title(data, level):
    font_size = 20 - 2 * level
    align = 'left' if level > 1 else 'center'
    return '<para leading=' + str(font_size+12 - 2*level) + ' face="Times" align=' + align + ' textColor="black" fontSize=' + str(font_size) + '><b><font>' + data + '</font></b></para>'

def multi_versions(one_box,table_style,jar_obj):
    table_content_style = [('GRID', (0, 0), (-1, -1), 0.3, colors.grey), ('VALIGN', (0, 0), (-1, -1), 'MIDDLE')]
    # title2 = '<para fontSize=14 align=left leading=16 textColor="black"><b><font>Multiple versions</font></b></para>'
    # story.append(Paragraph(title(":".join(jar.split("__fdse__")[:2]), 3), normalStyle))
    # title2 = title("Multiple versions", 4)
    # one_box.append([Paragraph(title2, normalStyle)])
    table_data = [[Paragraph('<para fontSize=9><b>Index</b></para>', normalStyle),Paragraph('<para fontSize=9><b>Module</b></para>', normalStyle),Paragraph('<para fontSize=9><b>Type</b></para>', normalStyle), Paragraph('<para fontSize=9><b>Property</b></para>', normalStyle), Paragraph('<para fontSize=9><b>Version</b></para>', normalStyle)]]
    # component_table = Table(table_data, colWidths=4 * [1.5 * inch], style=table_style)
    component_table = Table(table_data, colWidths=[0.5*inch,1.7 * inch,1.3 * inch,1.3 * inch,1.3 * inch], style=table_style)
    one_box.append([component_table])
    cnt = 0
    # for _module in jar_obj["module_versions"]:
    for i in range(0,len(jar_obj["module_versions"])):
        cnt += 1
        # property_data = "NA" if jar_obj["module_properties"][_module] is None else jar_obj["module_properties"][_module]
        # type_data = "explicit" if jar_obj["module_properties"][_module] is None else "implicit"
        property_data = "NA" if jar_obj["module_properties"][i][1] is None else jar_obj["module_properties"][i][1]
        type_data = "explicit" if jar_obj["module_properties"][i][1] is None else "implicit"
        table_data = [[Paragraph(str(cnt), normalStyle), Paragraph(jar_obj["module_versions"][i][0], normalStyle), Paragraph(type_data, normalStyle), Paragraph(property_data, normalStyle), Paragraph(jar_obj["module_versions"][i][1], normalStyle)]]
        # table_data = [[Paragraph(str(cnt), normalStyle), Paragraph(_module, normalStyle), Paragraph(type_data, normalStyle), Paragraph(property_data, normalStyle), Paragraph(jar_obj["module_versions"][_module], normalStyle)]]
        component_table = Table(table_data, colWidths=[0.5*inch,1.7 * inch,1.3 * inch,1.3 * inch,1.3 * inch], style=table_content_style)
        one_box.append([component_table])
    one_box.append([Paragraph('<para leading=6> &nbsp;<br/></para>', normalStyle)])

def add_ununified_module_version(one_box,table_style,jar_obj):
    table_content_style = [('GRID', (0, 0), (-2, -1), 0.3, colors.grey), ('LINEAFTER', (-1, -1), (-1, -1), 0.3, colors.grey),('VALIGN', (0, 0), (-1, -1), 'MIDDLE')]
    table_data = [[Paragraph('<para fontSize=9><b>Index</b></para>', normalStyle),Paragraph('<b>Module</b>', normalStyle), Paragraph('<b>Type</b>', normalStyle), Paragraph('<b>Property</b>', normalStyle),
                   Paragraph('<b>Version</b>', normalStyle)]]
    component_table = Table(table_data, colWidths=[0.5 * inch,1.6 * inch, 1.3 * inch, 1.3 * inch, 1.4 * inch], style=table_style)
    one_box.append([component_table])

    unify_version = None
    length = len(jar_obj["module_versions"])
    middle = get_middle(length)
    # cnt = 0
    # for _module in jar_obj["module_versions"]:
    for cnt in range(0, len(jar_obj["module_versions"])):
        # cnt += 1
        # property_data = "NA" if jar_obj["module_properties"][_module] is None else jar_obj["module_properties"][_module]
        # type_data = "explicit" if jar_obj["module_properties"][_module] is None else "implicit"
        # version_data = jar_obj["module_versions"][_module] if cnt == middle else ''
        # unify_version = jar_obj["module_versions"][_module]
        property_data = "NA" if jar_obj["module_properties"][cnt][1] is None else jar_obj["module_properties"][cnt][1]
        type_data = "explicit" if jar_obj["module_properties"][cnt][1] is None else "implicit"
        version_data = jar_obj["module_versions"][cnt][1] if cnt+1 == middle else ''
        unify_version = jar_obj["module_versions"][cnt][1]
        table_data = [[Paragraph(str(cnt+1), normalStyle),Paragraph(jar_obj["module_versions"][cnt][0], normalStyle),Paragraph(type_data, normalStyle), Paragraph(property_data, normalStyle),Paragraph(version_data, normalStyle)]]
        component_table = Table(table_data, colWidths=[0.5 * inch,1.6 * inch, 1.3 * inch, 1.3 * inch, 1.4 * inch], style=table_content_style)
        if cnt+1 == length:
            temp_style = table_content_style.copy()
            temp_style.append(('LINEBELOW', (-1, -1), (-1, -1), 0.3, colors.grey))
            component_table.setStyle(temp_style)
        one_box.append([component_table])
    one_box.append([Paragraph('<para leading=6> &nbsp;<br/></para>', normalStyle)])
    return unify_version

def add_efforts(one_box,table_style,tree_indices,subtree_numbers):
    # table_span_style = [('GRID', (0, 0), (-1, -1), 0.3, colors.grey),('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),('SPAN', (1, 0), (4, 0))]
    table_content_style = [('GRID', (0, 0), (-1, -1), 0.3, colors.grey),('VALIGN', (0, 0), (-1, -1), 'MIDDLE')]

    text = '<para fontSize=11 align=left leading=18 textColor="black"><b>Harmonization Effort</b></para>'
    one_box.append([Paragraph(text, normalStyle)])
    table_data = [[Paragraph('<para fontSize=9><b>Index</b><para fontSize=9></para>', normalStyle),Paragraph('<b>Module</b>', normalStyle), Paragraph('<b>NA (NAC)</b>', normalStyle),
                   Paragraph('<b>NDA (NDAC)</b>', normalStyle),
                   Paragraph('<b>NMA (NMAC)</b>', normalStyle)],
                  ]
    component_table = Table(table_data, colWidths=[0.5 * inch, 2.3 * inch, 1.1 * inch, 1.1 * inch, 1.1 * inch], style=table_style)
    one_box.append([component_table])

    number = subtree_numbers[0]
    for entry in tree_indices:
        cell_values = []
        for cell in entry[1][0:3]:
            if type(cell) == list:
                cell_values.append(Paragraph(str(cell[0]) + '(' + str(cell[1]) + ')', normalStyle))
            else:
                cell_values.append(Paragraph(str(cell), normalStyle))
        table_data = [[Paragraph(str(number), normalStyle), Paragraph(entry[0], normalStyle)] + cell_values]
        component_table = Table(table_data, colWidths=[0.5 * inch, 2.3 * inch, 1.1 * inch, 1.1 * inch, 1.1 * inch],style=table_content_style)
        one_box.append([component_table])
        number += 1

    content = '''<b>NA (Number of APIs): </b>the number of called library APIs in one module.<br/>
        <b>NAC (Number of API Calls): </b>the number of library API calls in one module.<br/>
        <b>NDA (Number of Deleted APIs): </b>the number of called library APIs that are deleted in the recommended library version.<br/>
        <b>NDAC (Number of Deleted API Calls): </b>the number of library API calls that are deleted in the recommended library version.<br/>
        <b>NMA (Number of Modifiefd APIs): </b>the number of called library APIs whose call graphs (A control flow graph which represents calling relationships between subroutines in the library) are modified in the recommended library version.<br/>
        <b>NMAC (Number of Modifiefd API Calls): </b>the number of library API calls to modified library APIs.'''
    one_box.append([Paragraph('<para autoLeading="off" fontSize=7 align=left face="Times-Italic" textColor="black">' + content + '<br/><br/></para>', normalStyle)])

def add_files_related(one_box,table_style,files_data):
    table_content_style = [('GRID', (-1, -1), (-1, -1), 0.3, colors.grey),('LINEBEFORE', (0, -1), (-2, -1), 0.3, colors.grey),('VALIGN', (0, 0), (-1, -1), 'MIDDLE')]

    if len(files_data) == 0:
        return
    text = '<para fontSize=9 align=left face="Times-Italic" leading=15>Files related:</para>'
    one_box.append([Paragraph(text, normalStyle)])
    table_data = [[Paragraph('<b>Module</b>', normalStyle), Paragraph('<b>Package</b>', normalStyle),
                   Paragraph('<b>File</b>', normalStyle)]]
    component_table = Table(table_data, colWidths=3 * [2 * inch], style=table_style)
    one_box.append([component_table])

    for _module in files_data:
        java_files = files_data[_module]
        module_length = len(java_files)
        module_middle = get_middle(module_length)
        new_files = get_pkg_files(java_files, _module)
        module_cnt = 0
        for pkg in new_files:
            pkg_files = new_files[pkg]
            pkg_length = len(pkg_files)
            pkg_middle = get_middle(pkg_length)
            pkg_cnt = 0
            for f in pkg_files:
                pkg_cnt += 1
                module_cnt += 1
                package_data = pkg if pkg_cnt == pkg_middle else ''
                module_data = _module if module_cnt == module_middle else ''
                table_data = [[Paragraph(module_data, normalStyle)] + [Paragraph(package_data, normalStyle)] + [Paragraph(f, normalStyle)]]
                component_table = Table(table_data, colWidths=3 * [2 * inch], style=table_content_style)
                if pkg_cnt == pkg_length:
                    temp_style = table_content_style.copy()
                    temp_style.append(('LINEBELOW', (1, -1), (-2, -1), 0.3, colors.grey))
                    if module_cnt == module_length:
                        temp_style.append(('LINEBELOW', (0, -1), (1, -1), 0.3, colors.grey))
                    component_table.setStyle(temp_style)
                one_box.append([component_table])

def get_pkg_files(java_files,_module):
    new_files = {}
    for java_file in java_files:
        java_file = java_file.replace(_module, "")
        if java_file.startswith("/"):
            java_file = java_file[1:]
        r = java_file.rfind("/")
        package = java_file[:r]
        java_file = java_file[r + 1:]
        if package not in new_files:
            new_files[package] = []
        new_files[package].append(java_file)
    return new_files

def add_overview(story,normalStyle,inconsistent_lib,false_consistent_lib):
    overview_style = [('GRID', (0, 0), (-1, -1), 0.3, colors.grey), ('VALIGN', (0, 0), (-1, -1), 'MIDDLE')]
    title1 = title('I.  Overview', 2)
    story.append(Paragraph(title1, normalStyle))
    content = 'Your project have '
    if inconsistent_lib > 0:
        content += '<font color="red">' + str(inconsistent_lib) + '</font> inconsistent libraries' if inconsistent_lib > 1 else '<font color="red">' + str(inconsistent_lib) + '</font> inconsistent library'
    if false_consistent_lib > 0:
        if inconsistent_lib > 0:
            content += ' and '
        content += '<font color="red">' + str(false_consistent_lib) + '</font> false consistent libraries' if false_consistent_lib > 1 else '<font color="red">' + str(false_consistent_lib) + '</font> false consistent library'
    content += '.'
    # modules_text = 'modules' if modules > 1 else 'module'
    # libraries_text = 'libraries' if inconsistent_lib > 1 else 'library'
    # versions_text = 'versions' if multiversion > 1 else 'version'
    # content += 'with <font color="red">' + str(modules) + '</font> related ' + modules_text + ' in total. And <font color="red">' + str(inconsistent_lib) + '</font> ' + libraries_text + ' imported <font color="red">' + str(multiversion) + '</font> distinct ' + versions_text + '.'
    story.append(Paragraph('<para autoLeading="off" fontSize=9 align=left>' + content + '</para>', normalStyle))
    # table_data = [['Inconsistent libraries', inconsistent_lib],['False consistent libraries', false_consistent_lib],['Modules related', modules],['Distinct versions', multiversion]]
    # overview_table = Table(table_data, colWidths=2 * [3.05 * inch])
    # overview_table.setStyle(overview_style)
    # story.append(overview_table)
    story.append(Paragraph('<para><br/><br/></para>', normalStyle))

def add_summary(one_box,desc_dict,module_num, unify_version,lib,version_num,is_multiversion):
    # title2 = title('Summary', 4)
    # one_box.append([Paragraph(title2, normalStyle)])

    if is_multiversion:
        sentences = 'Your project uses <font color="red">%s</font> distinct versions of %s in <font color="red">%s</font> modules. There are ' % (version_num, lib, str(desc_dict["S"]))
    else:
        sentences = 'Multiple modules share the same version (%s) of %s in <font color="red">%s</font> modules in your project. There are ' % (
    unify_version, lib, module_num)
    if desc_dict["EX"] > 0:
        sentences += '<font color="red">%s</font> modules in which the library version is declared explicitly' % (str(desc_dict["EX"])) if desc_dict["EX"] > 1 else '<font color="red">%s</font> module in which the library version is declared explicitly' % (
            str(desc_dict["EX"]))
    if desc_dict["IM"] > 0:
        if desc_dict["EX"] > 0:
            sentences += ' and '
        sentences += '<font color="red">%s</font> modules in which the library version is declared implicitly' % (str(desc_dict["IM"])) if desc_dict[
                                                                               "IM"] > 1 else '<font color="red">%s</font> module in which the library version is declared implicitly' % (
            str(desc_dict["IM"]))
    # if desc_dict["X"] > 0:
    #     sentences += '<font color="red">%s</font> properties are' % (str(desc_dict["X"])) if desc_dict["X"] > 1 else '<font color="red">%s</font> property is' % (
    #         str(desc_dict["X"]))
    #     sentences += ' declared and referenced in '
    #     sentences += '<font color="red">%s</font> modules' % (str(desc_dict["Y"])) if desc_dict["Y"] > 1 else '<font color="red">%s</font> module' % (
    #         str(desc_dict["Y"]))
    # if desc_dict["M"] > 0:
    #     if desc_dict["X"] > 0:
    #         sentences += ', '
    #     sentences += '<font color="red">%s</font> explicit versions are' % (str(desc_dict["M"])) if desc_dict[
    #                                                                            "M"] > 1 else '<font color="red">%s</font> explicit version is' % (
    #         str(desc_dict["M"]))
    #     sentences += ' declared in '
    #     sentences += '<font color="red">%s</font> modules' % (str(desc_dict["N"])) if desc_dict["N"] > 1 else '<font color="red">%s</font> module' % (
    #         str(desc_dict["N"]))
    sentences += '.'

    text = '<para autoLeading="off" fontSize=9 align=left>' + sentences + '<br/></para>'
    one_box.append([Paragraph(text, normalStyle)])
    one_box.append([Paragraph('<para leading=6> &nbsp;<br/></para>', normalStyle)])
    # return sentences
def add_image(one_box, file, jar, tree_id):
    border_style = [('ALIGN', (1, 1), (-1, -1), 'CENTER'), ('BOX', (0, 0), (-1, -1), 1, colors.black),
                    ('TOPPADDING', (0, 0), (-1, -1), 0), ('BOTTOMPADDING', (0, 0), (-1, -1), 0)]
    # image
    one_box.append([Paragraph(
        '<para fontSize=11 align=left leading=11><b>Inheritance Relationship of POMs in All Related Modules</b></para>',
        normalStyle)])
    one_box.append([Paragraph('<para leading=6> &nbsp;<br/></para>', normalStyle)])
    show, image = get_image(file.replace(".json", ""), file.replace(".json", "") + "_" + jar.replace("__fdse__", "____") + "_" + tree_id + ".png")
    if show:
        new_box = []
        new_box.append([get_legend()])
        new_box.append([image])
        border_table = Table(new_box)
        border_table.setStyle(border_style)
        one_box.append([border_table])
        one_box.append([Paragraph('<para>The pom in red references the property value declared in the pom in green.</para>', normalStyle)])
    else:
        # one_box.append([image])
        # text = '<para fontSize=6 align=center>image too large to present</para>'
        text = '<para fontSize=8>See ' + file.replace(".json", "") + "_" + jar.replace("__fdse__", "____") + "_" + tree_id + ".png" + ' for details.</para>'
        one_box.append([Paragraph(text, normalStyle)])
    one_box.append([Paragraph('<para leading=6> &nbsp;<br/></para>', normalStyle)])

def get_image(project_id,picture_path):
    img = utils.ImageReader("datas/pngs/" + picture_path)
    iw, ih = img.getSize()
    # print(iw)
    # if iw > 15000:
    #     return False, Image("datas/not_show.png", width=50, height=40)
    if iw > 4200:
        if project_id not in large_images:
            large_images[project_id] = []
        large_images[project_id].append(picture_path)
        return False, None
        # return False, Image("datas/not_show.png", width=50, height=40)
    if iw > 5.9 * inch:
        ih = ih / iw * 5.9 * inch
        iw = 5.9 * inch
    if ih > 2.5 * inch:
        iw = iw / ih * 2.5 * inch
        ih = 2.5 * inch
    return True, Image("datas/pngs/" + picture_path, width=iw, height=ih)

def get_legend():
    img = utils.ImageReader("datas/legend.png")
    iw, ih = img.getSize()
    ih = ih / iw * 5.9 * inch
    iw = 5.9 * inch
    return Image("datas/legend.png", width=iw, height=ih)

def get_middle(length):
    if length % 2 == 0:
        return length // 2
    return length // 2 + 1

def project_id2name():
    proj_dict = get_proj_dict()
    data = read_json("datas/large_images.json")
    new_data = {}
    for id in data:
        proj_name = proj_dict[id].replace("/", " ")
        print(proj_name)
        new_data[proj_name] = data[id]
    write_json_format("datas/large_images.json",new_data)

# generate_report_pdf()
# 148 org.apache.logging.log4j__fdse__log4j-core__fdse__jar 0
# 148 javax.enterprise__fdse__cdi-api__fdse__jar 0
# 148 org.apache.logging.log4j__fdse__log4j-slf4j-impl__fdse__jar 0
# 148 org.apache.maven.plugin-testing__fdse__maven-plugin-testing-harness__fdse__jar 0
# 148 org.apache.maven.plugin-tools__fdse__maven-plugin-annotations__fdse__jar 0
# 148 org.apache.logging.log4j__fdse__log4j-api__fdse__jar 0
# 1765 org.bouncycastle__fdse__bcprov-jdk15on__fdse__jar 0
# 1765 commons-io__fdse__commons-io__fdse__jar 0
# 361 org.springframework.security__fdse__spring-security-jwt__fdse__jar 0
# 361 org.springframework.boot__fdse__spring-boot-starter-jdbc__fdse__jar 0
# 361 org.springframework.boot__fdse__spring-boot-starter-security__fdse__jar 0
# 361 org.springframework.boot__fdse__spring-boot-starter-web__fdse__jar 0
# 361 org.springframework.boot__fdse__spring-boot-starter-test__fdse__jar 0
# 361 org.springframework.boot__fdse__spring-boot-starter-actuator__fdse__jar 0
# 361 org.springframework.boot__fdse__spring-boot-starter__fdse__jar 0
# 361 com.h2database__fdse__h2__fdse__jar 0
# 1286 org.springframework.boot__fdse__spring-boot-starter-test__fdse__jar 1
# 60 org.apache.derby__fdse__derby__fdse__jar 0
# 60 org.apache.hadoop__fdse__hadoop-hdfs__fdse__jar 0
# 60 org.apache.hadoop__fdse__hadoop-mapreduce-client-core__fdse__jar 0
# 60 org.apache.hadoop__fdse__hadoop-hdfs-client__fdse__jar 0
# 60 org.apache.logging.log4j__fdse__log4j-1.2-api__fdse__jar 0
# 60 org.apache.hadoop__fdse__hadoop-distcp__fdse__jar 0
# 60 org.apache.hadoop__fdse__hadoop-common__fdse__jar 0
# 60 org.apache.commons__fdse__commons-lang3__fdse__jar 0
# 60 com.google.guava__fdse__guava__fdse__jar 0
# 60 org.apache.logging.log4j__fdse__log4j-slf4j-impl__fdse__jar 0
# 2594 org.jenkins-ci__fdse__trilead-ssh2__fdse__jar 0
# 407 junit__fdse__junit__fdse__jar 0
# read_actions()
# data2pdf()
project_id2name()
# drawFrame()
# print(get_proj_dict())
# data = read_json("../action-ununified-7-22.json")
# print(len(data))