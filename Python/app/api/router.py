import importlib
from pathlib import Path
from fastapi import APIRouter

api_router = APIRouter()


def auto_discover_v1_routers():
    current_dir = Path(__file__).resolve().parent
    v1_dir = current_dir / "v1"

    if not v1_dir.exists():
        return

    for py_file in v1_dir.rglob("*.py"):
        if py_file.name == "__init__.py":
            continue
        try:
            relative_path = py_file.relative_to(current_dir).with_suffix('')
            module_suffix = ".".join(relative_path.parts)
            module_name = f"app.api.{module_suffix}"
            module = importlib.import_module(module_name)
            if hasattr(module, "router"):
                sub_router = getattr(module, "router")
                api_router.include_router(sub_router)

                # If smart-piggy, also mount with /smart_piggy for backward compatibility
                if getattr(sub_router, "prefix", "") == "/smart-piggy":
                    api_router.include_router(sub_router, prefix="", tags=getattr(sub_router, "tags", []))
                    
        except Exception as e:
            print(f"[ROUTER_DISCOVERY_ERROR] Thất bại khi nạp tự động module {py_file.name}: {str(e)}")

auto_discover_v1_routers()
