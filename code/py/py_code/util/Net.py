import requests


class Net(object):

    @staticmethod
    def get_operation(url):
        result = list()
        response = requests.get(url)
        result.append(response.ok)
        response.encoding = 'utf-8'
        result.append(response.text)
        return result
