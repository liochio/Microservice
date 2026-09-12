# -*- coding: utf-8 -*-
import os
import re
import sys
import json
from collections import defaultdict

sys.stdout.reconfigure(encoding='utf-8')

def extract_spring_apis():
    apis = []
    sb_dir = 'SpringBoot'
    for root, dirs, files in os.walk(sb_dir):
        for f in files:
            if f.endswith('Controller.java'):
                path = os.path.join(root, f)
                rel = os.path.relpath(path, 'SpringBoot')
                service = rel.split(os.sep)[0]
                
                with open(path, 'r', encoding='utf-8', errors='ignore') as fp:
                    content = fp.read()
                
                # Base request mapping
                base_mappings = []
                bm = re.findall(r'@RequestMapping\s*\(\s*(?:(?:value|path)\s*=\s*)?\{?([^})]+)\}?', content)
                if bm:
                    raw_bases = bm[0].split(',')
                    base_mappings = [b.strip().strip('"\'') for b in raw_bases if b.strip().strip('"\'')]
                if not base_mappings:
                    base_mappings = ['']
                
                matches = re.finditer(r'@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping)\s*(?:\(\s*(?:(?:value|path)\s*=\s*)?\{?([^)]*)\}?\s*\))?', content)
                for m in matches:
                    http_method = m.group(1).replace('Mapping', '').upper()
                    raw_sub = m.group(2) or '""'
                    sub_paths = [p.strip().strip('"\'') for p in raw_sub.split(',') if p.strip().strip('"\'')]
                    if not sub_paths:
                        sub_paths = ['']
                    
                    start_pos = m.end()
                    chunk = content[start_pos:start_pos+500]
                    op_m = re.search(r'@Operation\s*\(\s*summary\s*=\s*"([^"]+)"', chunk)
                    summary = op_m.group(1) if op_m else ''
                    
                    fn_m = re.search(r'public\s+[\w\<\>\[\],\s]+\s+(\w+)\s*\(', chunk)
                    func_name = fn_m.group(1) if fn_m else ''
                    
                    for bp in base_mappings:
                        for sp in sub_paths:
                            full_path = (bp + ('/' + sp if sp and not sp.startswith('/') else sp)).replace('//', '/')
                            if not full_path.startswith('/'):
                                full_path = '/' + full_path
                            apis.append({
                                'service': service,
                                'file': f,
                                'method': http_method,
                                'path': full_path,
                                'summary': summary or func_name,
                                'handler': func_name
                            })
    return apis

def extract_python_apis():
    apis = []
    py_dir = 'Python/app/api'
    for root, dirs, files in os.walk(py_dir):
        for f in files:
            if f.endswith('.py') and not f.startswith('__'):
                path = os.path.join(root, f)
                module = f.replace('.py', '')
                with open(path, 'r', encoding='utf-8', errors='ignore') as fp:
                    content = fp.read()
                
                matches = re.finditer(r'@router\.(get|post|put|delete|patch)\s*\(\s*"([^"]+)"(?:[\s\S]*?(?:summary\s*=\s*"([^"]+)"|description\s*=\s*"([^"]+)"))?', content)
                for m in matches:
                    method = m.group(1).upper()
                    sub_path = m.group(2)
                    summary = m.group(3) or m.group(4) or ''
                    
                    chunk = content[m.end():m.end()+250]
                    fn_m = re.search(r'def\s+(\w+)\s*\(', chunk)
                    func_name = fn_m.group(1) if fn_m else ''
                    
                    apis.append({
                        'service': 'Python FinTech & AI Core (:8089)',
                        'module': module,
                        'file': f,
                        'method': method,
                        'path': sub_path,
                        'summary': summary or func_name,
                        'handler': func_name
                    })
    return apis

if __name__ == '__main__':
    sb_apis = extract_spring_apis()
    py_apis = extract_python_apis()
    
    with open('scripts/all_apis.json', 'w', encoding='utf-8') as f:
        json.dump({'spring_boot': sb_apis, 'python': py_apis}, f, ensure_ascii=False, indent=2)
        
    print(f"✅ Extracted {len(sb_apis)} Spring Boot APIs and {len(py_apis)} Python APIs!")
