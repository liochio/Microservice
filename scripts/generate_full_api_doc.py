# -*- coding: utf-8 -*-
import json
import sys

sys.stdout.reconfigure(encoding='utf-8')

with open('scripts/all_apis.json', 'r', encoding='utf-8') as f:
    data = json.load(f)

def clean_sb_apis(apis):
    unique = {}
    for a in apis:
        p = a['path'].strip().strip('"\'{}')
        norm_p = p.replace('/api/', '/api/v1/') if not p.startswith('/api/v1/') and p.startswith('/api/') else p
        norm_p = norm_p.replace('/api/v1/v1/', '/api/v1/')
        key = (a['service'], a['method'], norm_p)
        if key not in unique or len(a['summary']) > len(unique[key]['summary']):
            unique[key] = {
                'service': a['service'],
                'method': a['method'],
                'path': norm_p,
                'summary': a['summary'] or a['handler'],
                'file': a['file']
            }
    return sorted(unique.values(), key=lambda x: (x['service'], x['path']))

clean_sb = clean_sb_apis(data['spring_boot'])
py_apis = data['python']

md = []
md.append("# 📚 DANH MỤC TOÀN BỘ API HỆ THỐNG LIOCHIO (FULL API SPECIFICATION)")
md.append("\n> **Tổng cộng**: " + str(len(clean_sb)) + " API Spring Boot Core & " + str(len(py_apis)) + " API Python FinTech & AI Core")
md.append("\n---\n")

# Spring Boot by Service
services = sorted(list(set(a['service'] for a in clean_sb)))
for s in services:
    items = [a for a in clean_sb if a['service'] == s]
    md.append(f"## 🏛️ Dịch Vụ: `{s.upper()}` ({len(items)} APIs)\n")
    md.append("| Phương Thức | Đường Dẫn API (Endpoint) | Chức Năng & Vai Trò | Controller |")
    md.append("| :---: | :--- | :--- | :--- |")
    for it in items:
        summary = it['summary'].replace('|', '/')
        md.append(f"| `{it['method']}` | `{it['path']}` | {summary} | `{it['file']}` |")
    md.append("\n---\n")

# Python by Module
md.append("## 🐍 DỊCH VỤ: `PYTHON FINTECH & AI CORE (:8089)` (" + str(len(py_apis)) + " APIs)\n")
py_modules = sorted(list(set(a['module'] for a in py_apis)))
for m in py_modules:
    items = [a for a in py_apis if a['module'] == m]
    md.append(f"### 📦 Phân Hệ Python: `{m.upper()}` ({len(items)} APIs)\n")
    md.append("| Phương Thức | Đường Dẫn API (Endpoint) | Chức Năng & Vai Trò | File Handler |")
    md.append("| :---: | :--- | :--- | :--- |")
    for it in items:
        summary = it['summary'].replace('|', '/')
        md.append(f"| `{it['method']}` | `/api/v1/{m}{it['path']}` | {summary} | `{it['file']}` |")
    md.append("\n")

full_content = "\n".join(md)

with open('API_CATALOG_FULL.md', 'w', encoding='utf-8') as f:
    f.write(full_content)

print(f"Generated API_CATALOG_FULL.md with {len(clean_sb)} Spring Boot APIs and {len(py_apis)} Python APIs!")
