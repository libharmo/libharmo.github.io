import codecs


def read(file_path):
    f = codecs.open(file_path, 'r', encoding='utf-8')
    content = f.read()
    f.close()

    return content


def write(file_path, content):
    f = codecs.open(file_path, 'w', encoding='utf-8')
    f.write(content)
    f.close()
