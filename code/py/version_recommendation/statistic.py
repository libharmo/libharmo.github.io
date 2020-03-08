from version_recommendation.draw import draw_barh, draw_mulitbar, draw_dots
from version_recommendation.file_util import read_json, write_json_format

output_dir = "datas/figure/"

def proj_modules():
    # pom_module_count = read_json("datas/pom_module_count.json")
    # result = {}
    # for proj_id in pom_module_count:
    #     module_count = pom_module_count[proj_id][1]
    #     if module_count == 0:
    #         continue
    #     if module_count not in result:
    #         result[module_count] = []
    #     if proj_id not in result[module_count]:
    #         result[module_count].append(proj_id)
    # print(len(result))
    # write_json_format(output_dir + "proj_modules.json", result)

    # inconsistent_data = read_json("../action-8-6.json")
    # total_ununified = read_json("../action-ununified-8-6.json")
    # data = read_json(output_dir + "proj_modules.json")
    # new_data = {}
    # for module_cnt in data:
    #     projs = data[module_cnt]
    #     total = len(projs)
    #     inconsistent_cnt = 0
    #     false_consistent_cnt = 0
    #     for proj in projs:
    #         if proj in inconsistent_data:
    #             inconsistent_cnt += 1
    #         if proj in total_ununified:
    #             false_consistent_cnt += 1
    #     new_data[module_cnt] = [total, inconsistent_cnt, false_consistent_cnt]
    # write_json_format(output_dir + "module_proj_type.json", new_data)

    module_proj_type = read_json(output_dir + "module_proj_type.json")
    module_proj_type = sorted(module_proj_type.items(), key=lambda d: int(d[0]))
    # keys = []
    # inconsistent_data = []
    # false_consistent_data = []
    # total_data = []
    # for entry in module_proj_type:
    #     module_cnt = entry[0]
    #     proj_type_cnt = entry[1]
    #     keys.append(module_cnt)
    #     inconsistent_data.append(proj_type_cnt[1])
    #     false_consistent_data.append(proj_type_cnt[2]-proj_type_cnt[1])
    #     total_data.append(proj_type_cnt[0]-proj_type_cnt[2])

    keys = [''] * 21
    inconsistent_data = [0] * 21
    false_consistent_data = [0] * 21
    total_data = [0] * 21
    for i in range(0, 20):
        start = i * 5
        end = i * 5 + 5
        keys[i] = str(start+1) + "-" + str(end)
    keys[20] = '>100'
    for entry in module_proj_type:
        module_cnt = int(entry[0])
        proj_type_cnt = entry[1]
        index = None
        if module_cnt > 100:
            index = 20
        # elif module_cnt > 90 and module_cnt <= 100:
        #     index = 54
        # elif module_cnt > 80 and module_cnt <= 90:
        #     index = 53
        # elif module_cnt > 70 and module_cnt <= 80:
        #     index = 52
        # elif module_cnt > 60 and module_cnt <= 70:
        #     index = 51
        # elif module_cnt > 50 and module_cnt <= 60:
        #     index = 50
        else:
            index = module_cnt // 5
            index = int(round(index, 0))
            if module_cnt % 5 == 0:
                index -= 1
            if index < 0:
                index = 0
        # inconsistent_data[index] += proj_type_cnt[1]
        # false_consistent_data[index] += proj_type_cnt[2]-proj_type_cnt[1]
        # total_data[index] += proj_type_cnt[0]-proj_type_cnt[2]
        inconsistent_data[index] += proj_type_cnt[1]
        false_consistent_data[index] += proj_type_cnt[2]
        total_data[index] += proj_type_cnt[0]

    pop_indices = []
    for i in range(0,len(total_data)):
        if total_data[i] == 0 and inconsistent_data[i] == 0 and false_consistent_data[i] == 0:
            pop_indices.append(i)
    tag = 0
    for e in pop_indices:
        keys.pop(e-tag)
        inconsistent_data.pop(e-tag)
        false_consistent_data.pop(e-tag)
        total_data.pop(e-tag)
        tag += 1
    draw_mulitbar(keys, inconsistent_data, false_consistent_data, total_data,
                  'The Number of Modules in a Project (#)', 'The Number of Projects (#)')


def false_consistent_lib():
    # total_ununified = read_json("../action-ununified-8-6.json")
    # result = {}
    # for proj_id in total_ununified:
    #     proj_data = total_ununified[proj_id]
    #     for jar in proj_data:
    #         jar = "__fdse__".join(jar.split("__fdse__")[0:2])
    #         if jar not in result:
    #             result[jar] = []
    #         if proj_id not in result[jar]:
    #             result[jar].append(proj_id)
    # print(len(result))
    # write_json_format(output_dir + "false_consistent.json", result)

    datas =read_json(output_dir + "false_consistent.json")
    new_data = {}
    for lib in datas:
        new_data[lib] = len(datas[lib])
    # print(new_data)
    sorted_usage = sorted(new_data.items(), key=lambda d: d[1], reverse=True)
    sorted_usage = sorted_usage[:20]
    print(sorted_usage)

    sorted_usage = sorted_usage[::-1]
    print(sorted_usage)
    values = [value for key, value in sorted_usage]
    keys = [key for key, value in sorted_usage]

    draw_barh(keys, values, "The Number of Projects (#)")


def inconsistent_lib():
    inconsistent_data = read_json("../action-8-6.json")
    result = {}
    for proj_id in inconsistent_data:
        proj_data = inconsistent_data[proj_id]
        for jar in proj_data:
            jar = "__fdse__".join(jar.split("__fdse__")[0:2])
            if jar not in result:
                result[jar] = []
            if proj_id not in result[jar]:
                result[jar].append(proj_id)
    print(len(result))
    write_json_format(output_dir + "inconsistent.json", result)

    datas = read_json(output_dir + "inconsistent.json")
    new_data = {}
    for lib in datas:
        new_data[lib] = len(datas[lib])
    sorted_usage = sorted(new_data.items(), key=lambda d: d[1], reverse=True)
    sorted_usage = sorted_usage[:20]
    print(sorted_usage)

    sorted_usage = sorted_usage[::-1]
    print(sorted_usage)
    values = [value for key, value in sorted_usage]
    keys = [key for key, value in sorted_usage]

    draw_barh(keys, values, "The Number of Projects (#)")

def ic_fc():
    fc_ic = read_json(output_dir + "meta-popular-fc-ic-lib.json")
    data = fc_ic["ic"]
    xlabel = "Usage Number"
    ylabel = "Most Pervasive Inconsistent Libraries"
    # data = fc_ic["fc"]
    # xlabel = "Usage Number"
    # ylabel = "Most Pervasive False Consistent Libraries"
    data = dict(data)
    sorted_usage = sorted(data.items(), key=lambda d: d[1], reverse=True)
    sorted_usage = sorted_usage[:20]
    sorted_usage = sorted_usage[::-1]
    values = [value for key, value in sorted_usage]
    keys = [":".join(key.split("__fdse__")[0:2]) for key, value in sorted_usage]
    draw_barh(keys, values, xlabel, ylabel)

def ic_fc_dot():
    fc_ic = read_json(output_dir + "rq1-scatterdot.json")
    fc_keys = []
    fc_values = []
    ic_keys = []
    ic_values = []
    for entry in fc_ic:
        if entry[1] == "fc":
            if entry[2] <= 20 and entry[3] <= 200:
                fc_values.append(entry[2])
                fc_keys.append(entry[3])
        else:
            if entry[2] <= 20 and entry[3] <= 200:
                ic_values.append(entry[2])
                ic_keys.append(entry[3])

    xlabel = "The Number of Modules (#)"
    ylabel = "The Number of Libraries (#)"
    draw_dots(fc_keys,fc_values,ic_keys,ic_values,xlabel,ylabel)

# false_consistent_lib()
# inconsistent_lib()
# proj_modules()
# ic_fc()
ic_fc_dot()