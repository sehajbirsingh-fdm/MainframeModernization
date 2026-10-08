"""Small standard-library client for the Bank of Z transaction API."""

from __future__ import annotations

import json
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode
from urllib.request import Request, urlopen


DEFAULT_API_URL = "http://localhost:8080"
DEFAULT_TOKEN = "valid-inqacc-inquirer-token"


class BankApi:
    def __init__(self, base_url: str = DEFAULT_API_URL, token: str = DEFAULT_TOKEN) -> None:
        self.base_url = base_url.rstrip("/")
        self.headers = {
            "Authorization": f"Bearer {token}",
            "Accept": "application/json",
        }

    def _request(self, request: Request) -> dict[str, object]:
        try:
            with urlopen(request, timeout=15) as response:
                payload = json.load(response)
        except HTTPError as error:
            detail = error.read().decode("utf-8", errors="replace")
            raise RuntimeError(f"Bank API returned HTTP {error.code}: {detail}") from error
        except URLError as error:
            raise RuntimeError(f"Cannot reach Bank API at {self.base_url}: {error.reason}") from error

        if not isinstance(payload, dict):
            raise RuntimeError("Bank API returned an invalid JSON response")
        return payload

    def transactions(self, sort_code: str, account_number: str) -> list[dict[str, object]]:
        transactions: list[dict[str, object]] = []
        offset = 0
        while True:
            query = urlencode({"limit": 100, "offset": offset})
            url = f"{self.base_url}/api/v1/accounts/{sort_code}/{account_number}/transactions?{query}"
            response = self._request(Request(url, headers=self.headers))
            rows = response.get("transactions", [])
            if not isinstance(rows, list) or not all(isinstance(row, dict) for row in rows):
                raise RuntimeError("Bank API returned an invalid transactions response")
            transactions.extend(rows)

            returned = int(response.get("returnedCount", len(rows)))
            total = int(response.get("totalCount", len(transactions)))
            offset += returned
            if returned == 0 or offset >= total:
                return transactions

    def create(
        self,
        sort_code: str,
        account_number: str,
        payload: dict[str, object],
    ) -> dict[str, object]:
        url = f"{self.base_url}/api/v1/accounts/{sort_code}/{account_number}/transactions"
        headers = {**self.headers, "Content-Type": "application/json"}
        body = json.dumps(payload).encode("utf-8")
        return self._request(Request(url, data=body, headers=headers, method="POST"))
