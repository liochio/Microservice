from pydantic import BaseModel, model_validator
from typing import Optional, Any
from app.constants import SystemConstants
from app.core.exceptions.base_exception import FintechBaseException

class CreateWalletRequest(BaseModel):
    wallet_code: Optional[Any] = None
    name: Optional[Any] = None
    currency: Optional[Any] = "VND"
    wallet_type: Optional[Any] = "CASH"
    color: Optional[Any] = None
    icon: Optional[Any] = None
    description: Optional[Any] = None

    @model_validator(mode="after")
    def sequential_wallet_validation_pipeline(self):
        if not self.wallet_code:
            raise FintechBaseException(error_code=SystemConstants.MISSING_WALLET_CODE, status_code=400)
        if not self.name:
            raise FintechBaseException(error_code=SystemConstants.MISSING_WALLET_NAME, status_code=400)

        self.wallet_code = str(self.wallet_code).strip()
        if len(self.wallet_code) > 50:
            raise FintechBaseException(error_code=SystemConstants.INVALID_WALLET_CODE_LENGTH, status_code=400)

        self.name = str(self.name).strip()
        if len(self.name) > 100:
            raise FintechBaseException(error_code=SystemConstants.INVALID_WALLET_NAME_LENGTH, status_code=400)

        self.currency = str(self.currency).strip().upper()
        if self.currency not in ["VND", "USD", "EUR", "CNY"]:
            raise FintechBaseException(error_code=SystemConstants.INVALID_CURRENCY_ENUM, status_code=400)

        self.wallet_type = str(self.wallet_type).strip().upper()
        return self