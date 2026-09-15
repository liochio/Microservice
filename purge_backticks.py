import os
import re

EXTENSIONS = ('.md', '.java', '.py', '.yml', '.yaml', '.json', '.properties', '.bat', '.sh', '.ps1')
SKIP_DIRS = {'.git', 'venv', '.venv', 'node_modules', 'target', '__pycache__', '.gemini', 'brain'}

total_files_scanned = 0
total_files_modified = 0
total_backticks_purged = 0
modified_files = []

for root, dirs, files in os.walk('.'):
    # Filter out skip directories
    dirs[:] = [d for d in dirs if d not in SKIP_DIRS and not any(s in d for s in ['.git', 'node_modules', 'target', '__pycache__', 'venv'])]
    
    for file in files:
        if file.endswith(EXTENSIONS) and file != 'purge_backticks.py':
            total_files_scanned += 1
            file_path = os.path.join(root, file)
            
            try:
                with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
                    content = f.read()
                
                b = chr(96)
                count = content.count(b)
                if count > 0:
                    # 1. Replace triple backticks with triple single quotes
                    new_content = content.replace(b * 3, "'''")
                    # 2. Replace paired inline backticks with single quotes
                    new_content = re.sub(re.escape(b) + r'([^' + re.escape(b) + r'\n]+)' + re.escape(b), r"'\1'", new_content)
                    # 3. Replace any remaining backtick with '
                    new_content = new_content.replace(b, "'")
                    
                    with open(file_path, 'w', encoding='utf-8') as f:
                        f.write(new_content)
                    
                    total_files_modified += 1
                    total_backticks_purged += count
                    modified_files.append((file_path, count))
            except Exception as e:
                print(f"[ERROR] Could not process {file_path}: {e}")

print("=" * 60)
print("BAO CAO CHI TIET CHIEN DICH PURGE BACKTICKS:")
print(f"- Tong so files da quet: {total_files_scanned}")
print(f"- Tong so files da sua doi: {total_files_modified}")
print(f"- Tong so dau backtick da bi tieu diet: {total_backticks_purged}")
print("=" * 60)
print("DANH SACH CAC FILE DA DUOC LAM SACH:")
for path, cnt in modified_files:
    safe_path = path.encode('ascii', errors='replace').decode('ascii')
    print(f"  + {safe_path}: da xoa {cnt} dau")
