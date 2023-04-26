from enum import Enum


class StreetType(Enum):
    TRCK = "Track"
    CALM2 = "Traffic calming zone bi-directional"
    CALM1 = "Traffic calming zone one-way"
    RES2 = "Residential area bi-directional"
    RES1 = "Residential area one-way"
    URBN2 = "Urban bi-directional"
    URBN1 = "Urban one-way"
    URBNPHDIV = "Urban physical divider"
    URBNMW = "Urban motorway"
    URBNMWPHDIV = "Urban motorway physical divider"
    RURAL = "Rural"
    RURALPHDIV = "Rural physical divider"
    HIWAY = "Highway"
    HIWAYPHDIV = "Highway physical divider"
    HIWAYENTRY = "Highway entry"
    HIWAYEXIT = "Highway exit"
    HIWAYENEX = "Highway entry-exit"
    MOWAY = "Motorway"
    MOWAYEN = "Motorway entry"
    MOWAYEX = "Motorway exit"
    MOWAYENEX = "Motorway entry-exit"
    FERRY = "Ferry"
    RACE = "Racetrack"
    NA = "Not available"
    INV = "Invalid"

    @classmethod
    def from_integer(cls, x):
        if x in cls.__members__.values():
            return cls(x)
        return None
