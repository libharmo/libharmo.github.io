import codecs
import json

from bs4 import BeautifulSoup

if __name__ == '__main__':
    doc_path = 'input/doc.txt'
    f = codecs.open(doc_path, 'r', encoding='utf-8')
    content = f.read()
    f.close()
    docs = json.loads(content)

    result_path = 'output/recommend_docs.txt'
    f = codecs.open(result_path, 'r', encoding='utf-8')
    content = f.read()
    f.close()

    result = json.loads('{}' if content == '' else content)
    count = len(docs)
    index = len(result)
    for doc in docs:

        if doc in result.keys():
            continue
        print(str(index) + '/' + str(count))
        print(doc)

        bs = BeautifulSoup(doc, 'html.parser')
        links = bs.findAll('a')
        for link in links:
            print(link)

        recommends = list()
        while True:
            recommend = input()
            if len(recommend) <= 2:
                break
            recommends.append(recommend)
        result[doc] = recommends
        f = codecs.open(result_path, 'w', encoding='utf-8')
        content = f.write(json.dumps(result, indent=4))
        f.close()
