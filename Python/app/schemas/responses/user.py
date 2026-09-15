
from pydantic import BaseModel
from typing import Optional, List, Any


class UserProfileData(BaseModel):
    user_id: str
    username: str
    email: str
    phone_number: Optional[str] = None
    full_name: Optional[str] = None
    date_of_birth: Optional[str] = None
    gender: Optional[str] = None
    status: str
    is_active: bool
    roles: List[str] = []


class UserProfileResponse(BaseModel):
    success: bool = True
    error_code: str = "USER_PROFILE_FETCH_SUCCESS"
    message: str
    data: UserProfileData
    trace_id: Optional[str] = None


class UserActionResponse(BaseModel):
    success: bool = True
    error_code: str = "SUCCESS"
    message: str
    data: Optional[Any] = None
    trace_id: Optional[str] = None
