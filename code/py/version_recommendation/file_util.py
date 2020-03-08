import json

def read_json(path):
    with open(path, 'r') as json_file:
        return json.load(json_file)

def write_json(path, json_data):
    with open(path, 'w') as json_file:
        json.dump(json_data, json_file)

def write_json_format(path, json_data):
    with open(path, 'w') as json_file:
        json.dump(json_data, json_file, indent=4)