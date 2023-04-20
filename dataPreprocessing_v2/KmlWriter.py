import logging
import GPSCoordinate
from PSWF import PSWF


class KmlWriter:
    def __init__(self, bfn):
        self.base_filename = bfn
        self.kml_filename = bfn + ".kml"

        self.t2 = "  "
        self.t4 = "    "
        self.t6 = "      "
        self.t8 = "        "
        self.t10 = "          "

        self.pal2 = "http://maps.google.com/mapfiles/kml/pal2/"
        self.pal3 = "http://maps.google.com/mapfiles/kml/pal3/"
        self.pal4 = "http://maps.google.com/mapfiles/kml/pal4/"
        self.pal5 = "http://maps.google.com/mapfiles/kml/pal5/"

        self.header_written = False
        self.footer_written = False

        self.buffered_writer = open(self.kml_filename, "w")

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.buffered_writer.close()

    def write_kml_name_description(self, n, d):
        if self.buffered_writer:
            self.buffered_writer.write(f"{self.t4}<name>{n}</name>\n")
            self.buffered_writer.write(f"{self.t4}<open>1</open>\n")
            self.buffered_writer.write(f"{self.t4}<description>{d}</description>\n")
            return True
        return False

    def write_folder_header(self, name, descr):
        if self.buffered_writer:
            self.buffered_writer.write(f"{self.t4}<Folder>\n")
            self.buffered_writer.write(f"{self.t6}<name>{name}</name>\n")
            self.buffered_writer.write(f"{self.t6}<description>{descr}</description>\n")
            return True
        return False

    def write_folder_footer(self):
        if self.buffered_writer:
            self.buffered_writer.write(f"{self.t4}</Folder>\n")
            return True
        return False

    def write_kml_header(self):
        if self.buffered_writer and not self.header_written:
            self.buffered_writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            self.buffered_writer.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n")
            self.buffered_writer.write(f"{self.t2}<Document>\n")
            self.header_written = True
            return True
        return False

    def write_kml_footer(self):
        if self.buffered_writer and self.header_written and not self.footer_written:
            self.buffered_writer.write(f"{self.t2}</Document>\n")
            self.buffered_writer.write("</kml>\n")
            self.buffered_writer.close()
            self.footer_written = True
            return True
        return False

    def write_style_ids(self) -> bool:
        return (
                self.write_gps_sid()
                and self.write_pswf_sid()
                and self.write_pres_seg_sid()
                and self.write_veh_gps_path_sid()
        )

    def write_line_style(self, name: str, color: str, width: str) -> bool:
        if self.buffered_writer:
            try:
                self.buffered_writer.write(self.t4 + f'<Style id="{name}">\n')
                self.buffered_writer.write(self.t6 + "<LineStyle>\n")
                self.buffered_writer.write(self.t8 + f"<color>{color}</color>\n")
                self.buffered_writer.write(self.t8 + f"<width>{width}</width>\n")
                self.buffered_writer.write(self.t6 + "</LineStyle>\n")
                self.buffered_writer.write(self.t4 + "</Style>\n")
                return True
            except IOError as ex:
                logging.exception("Exception in KmlWriter.write_line_style", exc_info=ex)
        return False

    def write_icon_style(self, name: str, url: str) -> bool:
        if self.buffered_writer:
            try:
                self.buffered_writer.write(self.t4 + f'<Style id="{name}">\n')
                self.buffered_writer.write(self.t6 + "<IconStyle>\n")
                self.buffered_writer.write(self.t8 + "<Icon>\n")
                self.buffered_writer.write(self.t10 + f"<href>{url}</href>\n")
                self.buffered_writer.write(self.t8 + "</Icon>\n")
                self.buffered_writer.write(self.t6 + "</IconStyle>\n")
                self.buffered_writer.write(self.t4 + "</Style>\n")
                return True
            except IOError as ex:
                logging.exception("Exception in KmlWriter.write_icon_style", exc_info=ex)
        return False

    def write_line_string_header(self, extrude: str, altitude: str, tessellate: str) -> bool:
        if self.buffered_writer:
            try:
                self.buffered_writer.write(self.t4 + "<Placemark>\n")
                self.buffered_writer.write(self.t4 + "<styleUrl>#VehGpsPathStyle</styleUrl>\n")
                self.buffered_writer.write(self.t4 + "<LineString>\n")
                self.buffered_writer.write(self.t6 + "<extrude>" + extrude + "</extrude>\n")
                self.buffered_writer.write(self.t6 + "<altitudeMode>" + altitude + "</altitudeMode>\n")
                self.buffered_writer.write(self.t6 + "<tessellate>" + tessellate + "</tessellate>\n")
                self.buffered_writer.write(self.t8 + "<coordinates>\n")

                return True
            except IOError as ex:
                logging.getLogger('KmlWriter').exception(ex)
                return False
        return False

    def write_line_string_coordinate(self, lon: float, lat: float) -> bool:
        if self.buffered_writer:
            try:
                self.buffered_writer.write(self.t8 + str(lon) + "," + str(lat) + ",1\n")

                return True
            except IOError as ex:
                logging.getLogger('KmlWriter').exception(ex)
                return False
        return False

    def write_line_string_footer(self) -> bool:
        if self.buffered_writer:
            try:
                self.buffered_writer.write(self.t8 + "</coordinates>\n")
                self.buffered_writer.write(self.t6 + "</LineString>\n")
                self.buffered_writer.write(self.t6 + "</Placemark>\n")

                return True
            except IOError as ex:
                logging.getLogger('KmlWriter').exception(ex)
                return False
        return False

    def write_placemark(self, name: str, descr: str, icon_style: str, gps: GPSCoordinate) -> bool:
        if self.buffered_writer:
            try:
                self.buffered_writer.write(self.t4 + "<Placemark>\n")
                self.buffered_writer.write(self.t6 + "<name>" + name + "</name>\n")
                self.buffered_writer.write(self.t6 + "<description>" + descr + "</description>\n")
                self.buffered_writer.write(self.t6 + "<styleUrl>#" + icon_style + "</styleUrl>\n")
                self.buffered_writer.write(self.t6 + "<Point>\n")
                self.buffered_writer.write(
                    self.t8 + "<coordinates>" + str(gps.longitude) + "," + str(gps.latitude) + ",0</coordinates>\n")
                self.buffered_writer.write(self.t6 + "</Point>\n")
                self.buffered_writer.write(self.t4 + "</Placemark>\n")

                return True
            except IOError as ex:
                logging.getLogger('KmlWriter').exception(ex)
                return False
        return False

    def write_gps_placemark(self, name: str, descr: str, gps: GPSCoordinate) -> bool:
        return self.write_placemark(name, descr, "GPSIconStyle", gps)

    def write_pres_seg_placemark(self, name: str, descr: str, gps: GPSCoordinate) -> bool:
        return self.write_placemark(name, descr, "PresentSegmentIconStyle", gps)

    def write_pswf_placemark(self, pswf: PSWF, name: str, descr: str, gps: GPSCoordinate) -> bool:
        return self.write_placemark(name, descr, pswf.name + "IconStyle", gps)

    def write_gps_sid(self) -> bool:
        return self.write_icon_style("GPSIconStyle", self.pal4 + "icon57.png")

    def write_pres_seg_sid(self) -> bool:
        return self.write_icon_style("PresentSegmentIconStyle", self.pal4 + "icon56.png")

    def write_pswf_sid(self) -> bool:
        return (
                self.write_icon_style("ParkenIconStyle", self.pal5 + "icon47.png")
                and self.write_icon_style("StandfunktionenIconStyle", self.pal5 + "icon26.png")
                and self.write_icon_style("WohnenIconStyle", self.pal5 + "icon30.png")
                and self.write_icon_style("PruefenAnDiIconStyle", self.pal5 + "icon47.png")
                and self.write_icon_style("FahrenIconStyle", self.pal5 + "icon61.png")
                and self.write_icon_style("InvalidIconStyle", self.pal5 + "icon40.png"))

    def write_veh_gps_path_sid(self) -> bool:
        return self.write_line_style("VehGpsPathStyle", "ffffffff", "2")
