from enum import Enum


class LidState(Enum):
    OPEN = "Open"
    CLOSED = "Closed"
    INTERMEDIATE = "Intermediate"
    UNKNOWN = "Unknown"
