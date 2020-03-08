from bs4 import BeautifulSoup


class BeautifulUtil(object):
    def format(response):
        bs = BeautifulSoup(response, 'html.parser')
        return bs

    def has_issues(bs):
        issues_items = bs.findAll('span', attrs={'itemprop': 'name'})
        for issues_item in issues_items:
            if issues_item.text == 'Issues':
                return True
        return False
