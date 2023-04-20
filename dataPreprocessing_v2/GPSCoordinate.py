import math


class GPSCoordinate:
    def __init__(self, lon, lat):
        self.Longitude = lon
        self.Latitude = lat
        self.is_valid = not (self.Longitude == 2147483647 or self.Latitude == 2147483647)

        self.longitude = float(self.Longitude * 180.0 / 2147483647)
        self.longitude = self.longitude if self.longitude <= 180 else self.longitude - 360

        self.latitude = float(self.Latitude * 180.0 / 2147483647)
        self.latitude = self.latitude if self.latitude <= 180 else self.latitude - 360

    @staticmethod
    def distance(gps_1, gps_2):
        if gps_1.is_valid and gps_2.is_valid:
            return math.sqrt(
                math.pow((gps_1.latitude - gps_2.latitude), 2) + math.pow((gps_1.longitude - gps_2.longitude), 2))
        return 0.0
