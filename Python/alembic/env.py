# ============================================
# FILE: alembic/env.py
# ============================================

import sys
import importlib

from pathlib import Path
from logging.config import fileConfig

from sqlalchemy import engine_from_config
from sqlalchemy import pool
from sqlalchemy import text

from alembic import context

# ============================================
# PROJECT ROOT
# ============================================

project_root = Path(__file__).resolve().parent.parent

sys.path.append(str(project_root))

# ============================================
# IMPORT SETTINGS + BASE
# ============================================

from app.core.config.settings import settings
from app.models.common.base_entity import BaseEntity

# ============================================
# LOAD ALL MODELS
# ============================================

models_dir = project_root / "app" / "models"

for py_file in models_dir.rglob("*.py"):

    if py_file.name == "__init__.py":
        continue

    module_path = py_file.relative_to(
        project_root
    ).with_suffix("").parts

    module_name = ".".join(module_path)

    try:

        importlib.import_module(module_name)

    except Exception as import_error:

        print(
            f"[MODEL_IMPORT_ERROR] "
            f"{module_name}: {import_error}"
        )

# ============================================
# ALEMBIC CONFIG
# ============================================

config = context.config

if config.config_file_name is not None:

    fileConfig(config.config_file_name)

target_metadata = BaseEntity.metadata

# ============================================
# REMOVE EMPTY MIGRATIONS
# ============================================

def process_revision_directives(
    _context,
    _revision,
    directives
):

    if not directives:
        return

    script = directives[0]

    if script.upgrade_ops.is_empty():

        print(
            "🟡 No schema changes detected."
        )

        directives[:] = []

        return

    print(
        "🟢 Schema changes detected."
    )

# ============================================
# IGNORE TABLES
# ============================================

IGNORE_TABLES = {
    "alembic_version"
}

PROTECTED_TABLES = {
    "users",
    "roles",
    "permissions",
    "modules",
    "refresh_tokens",
    "user_roles",
    "user_sessions"
}

# ============================================
# INCLUDE OBJECT
# ============================================

def include_object(
    _obj,
    name,
    type_,
    reflected,
    compare_to
):

    # Ignore alembic table
    if (
        type_ == "table"
        and name in IGNORE_TABLES
    ):
        return False

    # Protect important tables
    if (
        type_ == "table"
        and name in PROTECTED_TABLES
    ):
        return False

    # Prevent auto drop column
    if type_ == "column":

        # Column exists in DB
        # but not in model
        if reflected and compare_to is None:
            return False

    return True

# ============================================
# AUTO BUILD MODELS STRUCTURE
# ============================================

def auto_sync_build_models_script():

    try:

        build_file_path = (
            project_root
            / "build_models_structure.py"
        )

        script_content = """
# ============================================
# AUTO GENERATED FILE
# ============================================

import os


def create_file(path, content):

    os.makedirs(
        os.path.dirname(path),
        exist_ok=True
    )

    with open(
        path,
        "w",
        encoding="utf-8"
    ) as writer:

        writer.write(content)

    print(
        f"[SUCCESS] Created: {path}"
    )


def main():

    print(
        "🚀 Building models structure..."
    )
"""

        for model_file in models_dir.rglob("*.py"):

            if "__pycache__" in str(model_file):
                continue

            relative_path = (
                model_file.relative_to(project_root)
                .as_posix()
            )

            with open(
                model_file,
                "r",
                encoding="utf-8"
            ) as reader:

                content = reader.read()

            content = content.replace(
                "\\",
                "\\\\"
            )

            content = content.replace(
                '"""',
                '\\"\\"\\"'
            )

            script_content += f'''

    create_file(
        "{relative_path}",
        """
{content}
"""
    )

'''

        script_content += """

if __name__ == "__main__":
    main()
"""

        # ====================================
        # ONLY WRITE WHEN CONTENT CHANGED
        # ====================================

        old_content = ""

        if build_file_path.exists():

            with open(
                build_file_path,
                "r",
                encoding="utf-8"
            ) as old_reader:

                old_content = old_reader.read()

        # No changes -> skip rewrite
        if old_content == script_content:

            print(
                "🟡 build_models_structure.py unchanged."
            )

            return

        # Write only when changed
        with open(
            build_file_path,
            "w",
            encoding="utf-8"
        ) as writer:

            writer.write(script_content)

        print(
            "✅ build_models_structure.py synchronized."
        )

    except Exception as build_error:

        print(
            f"❌ BUILD_SYNC_ERROR: "
            f"{build_error}"
        )

# ============================================
# ONLINE MIGRATIONS
# ============================================

def run_migrations_online():

    configuration = config.get_section(
        config.config_ini_section
    )

    if configuration is None:
        configuration = {}

    configuration[
        "sqlalchemy.url"
    ] = settings.DATABASE_URL

    connectable = engine_from_config(
        configuration,
        prefix="sqlalchemy.",
        poolclass=pool.NullPool
    )

    with connectable.connect() as connection:

        context.configure(
            connection=connection,

            target_metadata=target_metadata,

            include_object=include_object,

            compare_type=True,

            compare_server_default=False,

            process_revision_directives=(
                process_revision_directives
            ),

            render_as_batch=True
        )

        with context.begin_transaction():

            try:

                connection.execute(
                    text(
                        "SET FOREIGN_KEY_CHECKS = 0;"
                    )
                )

                context.run_migrations()

                connection.execute(
                    text(
                        "SET FOREIGN_KEY_CHECKS = 1;"
                    )
                )

            except Exception as migration_error:

                connection.execute(
                    text(
                        "SET FOREIGN_KEY_CHECKS = 1;"
                    )
                )

                print(
                    f"❌ MIGRATION_ERROR: "
                    f"{migration_error}"
                )

                raise migration_error

        auto_sync_build_models_script()

# ============================================
# OFFLINE MIGRATIONS
# ============================================

def run_migrations_offline():

    context.configure(
        url=settings.DATABASE_URL,

        target_metadata=target_metadata,

        literal_binds=True
    )

    with context.begin_transaction():

        context.run_migrations()

# ============================================
# ENTRY
# ============================================

if context.is_offline_mode():

    run_migrations_offline()

else:

    run_migrations_online()