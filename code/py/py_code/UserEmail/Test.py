import requests

if __name__ == '__main__':
    r = requests.get("https://www.youtube.com")
    print(r.text)
