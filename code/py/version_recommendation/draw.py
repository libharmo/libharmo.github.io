import matplotlib
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

def draw_twinx(x,y1,y2,x_label,y_label1,y_label2):

    # plt.bar(index, y, width=0.4, color='lightblue')
    #
    #
    #
    # x_num = range(len(x))
    # plt.xticks(x_num, x, rotation=75, fontproperties=Times_New_Roman, fontsize=21)
    # plt.yticks(fontproperties=Times_New_Roman, fontsize=21)

    Times_New_Roman = matplotlib.font_manager.FontProperties(fname='C:\\Windows\\Fonts\\times.ttf')
    # print(y2)
    df = pd.DataFrame({'bar': y1, 'line': y2}, index=x)
    fig = plt.figure()
    ax1 = df['bar'].plot(kind='bar', label='Number of Projects', color='lightblue')
    plt.xticks(rotation=75, fontproperties=Times_New_Roman, fontsize=21)
    plt.yticks(fontproperties=Times_New_Roman, fontsize=21)
    plt.xlabel(x_label, fontproperties=Times_New_Roman, fontsize=21)
    plt.ylabel(y_label1, fontproperties=Times_New_Roman, fontsize=21)
    index = np.arange(len(y1))
    for a, b in zip(index, y1):
        plt.text(a, b + 0.01, '%.0f' % b, ha='center', va='bottom', rotation=0, fontproperties=Times_New_Roman, fontsize=17)
    ax2 = ax1.twinx()
    # lns1 = df['line'].plot(kind='line', label='Median Size of Projects', ax=ax2, color='green', style='-o')
    lns1 = df['line'].plot(kind='line', label='Median Number of Libraries', ax=ax2, color='green', style='-o')
    # ax2.grid(color="red", axis="x")
    plt.yticks(fontproperties=Times_New_Roman, fontsize=21)
    plt.ylabel(y_label2, fontproperties=Times_New_Roman, fontsize=21)

    font1 ={'family' : 'Times New Roman', 'size'   : 15}


    fig.legend(loc=1, bbox_to_anchor=(0.85,1), bbox_transform=ax1.transAxes, prop=font1)
    # lns = ax1 + lns1
    # labs = [l.get_label() for l in lns]
    # ax1.legend(lns, labs, loc=0)

    plt.rcParams['font.sans-serif'] = ['SimHei']
    plt.rcParams['axes.unicode_minus'] = False
    plt.show()

def draw_bar(x,y,x_label,y_label):
    index = np.arange(len(y))
    plt.bar(index, y, width=0.4, color='lightblue')

    Times_New_Roman = matplotlib.font_manager.FontProperties(fname='C:\\Windows\\Fonts\\times.ttf')

    x_num = range(len(x))
    plt.xticks(x_num, x, rotation=75, fontproperties=Times_New_Roman, fontsize=21)
    # plt.xticks(x_num, x, fontproperties=Times_New_Roman, fontsize=11)
    plt.yticks(fontproperties=Times_New_Roman, fontsize=21)

    # plt.xlabel("Java项目数量")
    # plt.ylabel("第三方库数量（个）")

    plt.xlabel(x_label, fontproperties=Times_New_Roman, fontsize=21)
    plt.ylabel(y_label, fontproperties=Times_New_Roman, fontsize=21)

    # plt.legend()
    for a, b in zip(index, y):
        plt.text(a, b + 0.01, '%.0f' % b, ha='center', va='bottom', rotation=0, fontproperties=Times_New_Roman, fontsize=17)
    plt.rcParams['font.sans-serif'] = ['SimHei']
    plt.rcParams['axes.unicode_minus'] = False
    plt.show()

def draw_barh(labels, value, xlabel, ylabel):
    index = np.arange(len(value))
    # b=plt.barh(index, value, height=0.5, color='lightblue')
    b = plt.barh(index, value, height=0.5)

    Times_New_Roman = matplotlib.font_manager.FontProperties(fname='C:\\Windows\\Fonts\\times.ttf')

    y_ = range(len(labels))
    plt.yticks(y_, labels, fontproperties=Times_New_Roman, fontsize=17)
    plt.xticks(fontproperties=Times_New_Roman, fontsize=20)

    plt.xlabel(xlabel, fontproperties=Times_New_Roman, fontsize=21)
    plt.ylabel(ylabel, fontproperties=Times_New_Roman, fontsize=21)

    for rect in b:
        w = rect.get_width()
        plt.text(w, rect.get_y()+rect.get_height()/2, '%d' % int(w), ha='left', va='center', fontproperties=Times_New_Roman, fontsize=17)
    plt.rcParams['font.sans-serif'] = ['SimHei']
    plt.rcParams['axes.unicode_minus'] = False
    plt.show()

def draw_mulitbar(x,y1,y2,y3,x_label,y_label):
    index = np.arange(len(y1))
    y1 = np.array(y1)
    y2 = np.array(y2)
    y3 = np.array(y3)

    Times_New_Roman = matplotlib.font_manager.FontProperties(fname='C:\\Windows\\Fonts\\times.ttf')

    # plt.bar(index, y1, width=0.4, color='lightgreen', label='y1')
    # plt.bar(index, y2, bottom=y1, width=0.4, color='gold', label='y2')
    # plt.bar(index, y3, bottom=y1+y2, width=0.4, color='lightblue', label='y3')

    total_width, n = 0.8, 3
    width = total_width / n
    index = index - (total_width - width) / 2

    plt.bar(index, y1, width=width, label='projects having inconsistent libs')
    plt.bar(index + width, y2, width=width, label='projects having false inconsistent libs')
    plt.bar(index + 2 * width, y3, width=width, label='total projects')

    x_num = range(len(x))
    plt.xticks(x_num, x, rotation=60, fontproperties=Times_New_Roman, fontsize=21)
    plt.yticks(fontproperties=Times_New_Roman, fontsize=21)

    # plt.xlabel("Java项目数量")
    # plt.ylabel("第三方库数量（个）")

    plt.xlabel(x_label, fontproperties=Times_New_Roman, fontsize=21)
    plt.ylabel(y_label, fontproperties=Times_New_Roman, fontsize=21)

    plt.legend()
    # for a, b in zip(index, y1):
    #     plt.text(a, b + 0.01, '%.0f' % b, ha='center', va='bottom', rotation=0, fontproperties=Times_New_Roman, fontsize=12)
    # for a, b in zip(index, y2):
    #     plt.text(a + width, b + 0.01, '%.0f' % b, ha='center', va='bottom', rotation=0, fontproperties=Times_New_Roman,fontsize=12)
    # for a, b in zip(index, y3):
    #     plt.text(a + 2 * width, b + 0.01, '%.0f' % b, ha='center', va='bottom', rotation=0, fontproperties=Times_New_Roman,fontsize=12)
    plt.rcParams['font.sans-serif'] = ['SimHei']
    plt.rcParams['axes.unicode_minus'] = False
    plt.show()

def draw_line(x1,y1,x2,y2,x_label,y_label):
    Times_New_Roman = matplotlib.font_manager.FontProperties(fname='C:\\Windows\\Fonts\\times.ttf')

    l1, = plt.plot(x1, y1, color='b', linestyle=':', marker='o', markerfacecolor='b', markersize=5)
    l2, = plt.plot(x2, y2, color='r', linestyle=':', marker='o', markerfacecolor='r', markersize=5)

    plt.legend([l1, l2], ['twelve months', 'nine months'], loc='upper right')
    # plt.legend()

    plt.xlabel(x_label, fontproperties=Times_New_Roman, fontsize=15)
    plt.ylabel(y_label, fontproperties=Times_New_Roman, fontsize=15)
    plt.xticks(rotation=60)

    plt.rcParams['font.sans-serif'] = ['SimHei']
    plt.rcParams['axes.unicode_minus'] = False
    plt.show()

def draw_pie(labels,sizes,title):
    Times_New_Roman = matplotlib.font_manager.FontProperties(fname='C:\\Windows\\Fonts\\times.ttf')
    plt.figure(figsize=(6, 6))
    # 定义饼状图的标签，标签是列表
    # labels = [u'第一部分', u'第二部分', u'第三部分']
    # 每个标签占多大，会自动去算百分比
    # sizes = [60, 30, 10]
    # colors = ['lightskyblue','royalblue','lightblue','lightseagreen']
    colors = ['lightskyblue', 'royalblue', 'lightblue']
    # 将某部分爆炸出来， 使用括号，将第一块分割出来，数值的大小是分割出来的与其他两块的间隙
    # explode = (0, 0, 0, 0)
    explode = (0, 0, 0)
    patches, l_text, p_text = plt.pie(sizes, explode=explode, colors=colors,
                                      labeldistance=0.6, autopct='', shadow=False,
                                      startangle=90, pctdistance=0.6,textprops={'fontsize': 20})
    # patches, l_text, p_text = plt.pie(sizes, explode=explode, colors=colors, labels=labels,
    #                                   labeldistance=0.6, autopct='%3.1f%%', shadow=False,
    #                                   startangle=90, pctdistance=0.6, textprops={'fontsize': 20})
    # labeldistance，文本的位置离远点有多远，1.1指1.1倍半径的位置
    # autopct，圆里面的文本格式，%3.1f%%表示小数有三位，整数有一位的浮点数
    # shadow，饼是否有阴影
    # startangle，起始角度，0，表示从0开始逆时针转，为第一块。一般选择从90度开始比较好看
    # pctdistance，百分比的text离圆心的距离
    # patches, l_texts, p_texts，为了得到饼图的返回值，p_texts饼图内部文本的，l_texts饼图外label的文本

    for i in range(0,len(p_text)):
        # for key in p_text[i]:
        #     print(key)
        print(p_text[i])
        # p_text[i] = plt.text(4, 1, '12321sadad')

    # plt.text(-0.484564,0.488227,'15.7%\nMajor',fontdict={'size':20})
    # plt.text(-0.53042,-0.418018,'43.1%\nMinor',fontdict={'size':20})
    # plt.text(0.390537,-0.206141,'26.7%\nPatch',fontdict={'size':20})
    # plt.text(0.063397,0.539094,'14.5%\nSnapshot',fontdict={'size':20})

    # plt.text(-0.303183,0.587219,'6.6%\nMajor',fontdict={'size':20})
    # plt.text(-0.698219,-0.146193,'39.3%\nMinor',fontdict={'size':20})
    # plt.text(0.323219,-0.455307,'33.2%\nPatch',fontdict={'size':20})
    # plt.text(0.166992,0.474676,'20.9%\nSnapshot',fontdict={'size':20})

    plt.text(-0.328626,-0.502001,'81.5%\nUpgrade',fontdict={'size':20})
    plt.text(0.35567,0.199905,'3.6%\nDowngrade',fontdict={'size':20})
    plt.text(0.07053,0.60555,'14.9%\nUnknown',fontdict={'size':20})

    plt.axis('equal')
    # plt.legend(prop=Times_New_Roman,fontsize=17)
    # plt.rcParams['font.sans-serif'] = ['SimHei']
    # plt.rcParams['axes.unicode_minus'] = False
    matplotlib.rcParams['font.family'] = 'sans-serif'
    matplotlib.rcParams['font.sans-serif'] = 'Times New Roman'
    # plt.title(title, fontproperties=Times_New_Roman,fontsize=21)
    # plt.title('第三方库在Java项目中是否存在更新情况的比例')
    # plt.title('Java项目中是否存在第三方库更新的比例')
    plt.show()

def draw_dots(x1,y1,x2,y2,x_label,y_label):
    Times_New_Roman = matplotlib.font_manager.FontProperties(fname='C:\\Windows\\Fonts\\times.ttf')

    x1 = np.array(x1)
    y1 = np.array(y1)
    x2 = np.array(x2)
    y2 = np.array(y2)

    plt.figure()
    ax = plt.subplot()
    ax.scatter(x1, y1, alpha=0.5, label='false consistent libraries')
    ax.scatter(x2, y2, c='green', alpha=0.5, label='inconsistent libraries')

    plt.xticks(fontsize=17)
    plt.yticks(fontsize=17)

    plt.xlabel(x_label, fontproperties=Times_New_Roman, fontsize=21)
    plt.ylabel(y_label, fontproperties=Times_New_Roman, fontsize=21)
    # font1 = matplotlib.font_manager.FontProperties(fname='C:\\Windows\\Fonts\\times.ttf',)
    plt.legend(fontsize=15)

    plt.rcParams['font.sans-serif'] = ['SimHei']
    plt.rcParams['axes.unicode_minus'] = False
    plt.show()