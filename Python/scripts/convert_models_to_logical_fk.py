import os
import re
from pathlib import Path

models_dir = Path(__file__).resolve().parent.parent / "app" / "models"
modified_files = []

for py_file in models_dir.rglob("*.py"):
    content = py_file.read_text(encoding="utf-8")
    if "users.id" in content:
        lines = content.splitlines()
        new_lines = []
        for line in lines:
            if 'ForeignKey("users.id"' in line or "ForeignKey('users.id'" in line:
                is_nullable = "nullable=True" in line
                indent = line[:len(line) - len(line.lstrip())]
                var_name = line.strip().split("=")[0].strip()
                if is_nullable:
                    new_line = f"{indent}{var_name} = Column(String(64), nullable=True, index=True, comment=\"Logical FK lien ket voi liochio-core\")"
                else:
                    new_line = f"{indent}{var_name} = Column(String(64), nullable=False, index=True, comment=\"Logical FK lien ket voi liochio-core\")"
                new_lines.append(new_line)
            else:
                new_lines.append(line)
        
        new_content = "\n".join(new_lines) + "\n"
        if new_content != content:
            py_file.write_text(new_content, encoding="utf-8")
            modified_files.append(py_file.name)

print(f"Successfully converted {len(modified_files)} files to Logical FK:")
for f in modified_files:
    print(f"  - {f}")
