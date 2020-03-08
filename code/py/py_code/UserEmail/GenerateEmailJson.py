import codecs
import json

if __name__ == '__main__':
    survey = 'Dear %s<p></p>,We are a <a href="https://chenbihuan.github.io/">research team</a> at Fudan University, Shanghai, China, working on automatic third-party library analysis methods/tools to ease the management of third-party libraries for developers.<p></p>We are currently investigating the usage of different versions of the same third-party library in different modules of a project (i.e., the library version inconsistency problem). Due to this problem, it is a non-trivial and time-consuming task to maintain the evolving third-party libraries. In that sense, it seems necessary to harmonize the different versions of the same third-party library in different modules into one single version. However, developers may intentionally use a different version, e.g., due to backward compatibility issues.<p></p>Therefore, we designed a survey on library version inconsistency to better understand the root causes of library version inconsistency and the requirements of an automatic library version harmonization tool. We hope that you could take around 10 minutes to complete the survey. Your participation in the survey will remain strictly confidential, and all of the analysis and reporting will be based on the aggregate responses. Your input is very valuable to guide us on designing automatic tools that could benefit the community.\n<a href="https://www.surveymonkey.com/r/9HSF3T3">Please click here to take the survey</a>, or copy and paste the URL below into your internet browser: <a href="https://www.surveymonkey.com/r/9HSF3T3">https://www.surveymonkey.com/r/9HSF3T3</a>.'
    report = '<p></p>Furthermore, we developed a prototype tool to detect library version inconsistencies and suggest harmonized version with detailed maintenance efforts. We applied our tool on your GitHub project, and attached the generated report. We hope that the report could be useful for you to be aware of the library version inconsistencies in your project and to decide whether to harmonize inconsistent library versions. <a href="https://www.surveymonkey.com/r/BGQ6TPL">Please click here to give us feedbacks</a>, or copy and paste the URL below into your internet browser: <a href="https://www.surveymonkey.com/r/BGQ6TPL">https://www.surveymonkey.com/r/BGQ6TPL</a>.'
    regards = '<p></p>We appreciate your comments and thank you for your time.<p></p>Best regards,</br>Kaifeng Huang, Ying Wang, Bowen Shi, and Bihuan Chen'

    survey_map = dict()
    survey_map['survey'] = survey
    survey_map['report'] = report
    survey_map['regards'] = regards

    f = codecs.open('../input/survey.json', 'w', encoding='utf-8')
    f.write(json.dumps(survey_map, indent=4))
    f.close()
