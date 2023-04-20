import datetime
import zipfile
from pathlib import Path
import logging

from AscParser import AscParser
from KmlWriter import KmlWriter


def process_directory(input_directory):
    for filepath in input_directory.glob('*.asc'):
        logging.info(f"Processing {filepath.name}")
        asc_parser.parseAscFile(filepath)
    for filepath in input_directory.glob('*.zip'):
        logging.info(f"Processing {filepath.name}")
        asc_parser.parseZipAscFile(filepath)


if __name__ == "__main__":
    input_directory = Path('C:/Users/q622469/OneDrive - BMW Group/80_Masterarbeit/Program/Input/ASC')
    output_directory = Path('C:/Users/q622469/OneDrive - BMW Group/80_Masterarbeit/Program/Output')
    output_filename = output_directory / f"GPS_map_test_{datetime.datetime.now().strftime('%Y%m%d')}.kml"

    kml_writer = KmlWriter(output_filename)
    kml_writer.write_kml_header()
    kml_writer.write_kml_name_description("DEMO", "Show GPS positions and ADAS segments")
    kml_writer.write_style_ids()

    asc_parser = AscParser(kml_writer)
    process_directory(input_directory)

    asc_parser.writeCON_VEH("PSWF", f"{len(asc_parser.pswfStateChanges)} PWF states")
    asc_parser.writeGPS("GPS", f"{len(asc_parser.gpsCoordinates)} GPS coordinates")
    asc_parser.writePRES_SEG("ADAS", f"{len(asc_parser.adasSegments)} ADAS segments")
    asc_parser.writeSegmentPath("PATH", f"{len(asc_parser.adasSegments)} ADAS segments path")
    kml_writer.write_kml_footer()
