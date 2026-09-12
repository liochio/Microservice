import os
import shutil
from pathlib import Path

src_dir = Path("d:/Learning/JavaSpring/SpringBoot/src/main/java/com/learning/springboot")
dest_dir = Path("d:/Learning/Microservice/SpringBoot/src/main/java/com/liochio/auth")

items_to_copy = [
    ("aspect", "aspect"),
    ("core", "core"),
    ("modules/category", "modules/category"),
    ("modules/product", "modules/product"),
    ("common/response/PageResponse.java", "dto/response/PageResponse.java"),
]

for src_rel, dest_rel in items_to_copy:
    src_path = src_dir / src_rel
    dest_path = dest_dir / dest_rel

    if src_path.is_file():
        dest_path.parent.mkdir(parents=True, exist_ok=True)
        content = src_path.read_text(encoding="utf-8")
        # Replace packages & imports
        content = content.replace("package com.learning.springboot.common.response;", "package com.liochio.auth.dto.response;")
        content = content.replace("com.learning.springboot.", "com.liochio.auth.")
        dest_path.write_text(content, encoding="utf-8")
        print(f"Copied file: {src_rel} -> {dest_rel}")
    elif src_path.is_dir():
        for py_file in src_path.rglob("*.java"):
            rel_to_src = py_file.relative_to(src_dir)
            target_file = dest_dir / rel_to_src
            target_file.parent.mkdir(parents=True, exist_ok=True)
            content = py_file.read_text(encoding="utf-8")
            content = content.replace("com.learning.springboot.", "com.liochio.auth.")
            content = content.replace("com.learning.springboot;", "com.liochio.auth;")
            target_file.write_text(content, encoding="utf-8")
            print(f"Copied: {rel_to_src}")

print("Done migrating Java modules!")
