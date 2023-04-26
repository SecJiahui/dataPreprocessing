import datetime
import os
import traceback
from AscParser import AscParser
from KmlWriter import KmlWriter
from RingBufferHistory import RingbufferHistory

if "__main__" == __name__:

    output_kml_filename = "C:/Users/q622469/OneDrive - BMW Group/80_Masterarbeit/Program/Output/GPS_map_test_" \
                        + datetime.datetime.now().strftime("%Y%m%d")

    # all files ( .asc) in this directory will be processed
    #absolute_pathname = 'C:/Users/q622469/OneDrive - BMW Group/80_Masterarbeit/Program/Input/ASC_test'
    absolute_pathname = 'C:/Users/q622469/OneDrive - BMW Group/80_Masterarbeit/Program/Input/ASC'
    files_in_path = os.listdir(absolute_pathname)
    for file in files_in_path:
        print(file)

    ascii_q = RingbufferHistory(4)
    for i in range(65, 72):
        ascii_q.push(chr(i))
    print(ascii_q.list())
    ascii_q.init()
    print(ascii_q.list())
    for i in range(69, 78):
        ascii_q.push(chr(i))
    print(ascii_q.list())

    kml_writer = KmlWriter(output_kml_filename)
    kml_writer.write_kml_header()
    kml_writer.write_kml_name_description("DEMO", "Show GPS positions and ADAS segments")
    kml_writer.write_style_ids()

    asc_parser = AscParser(kml_writer)
    for filename in files_in_path:
        filepath = os.path.join(absolute_pathname, filename)
        if os.path.isfile(filepath):
            try:
                # ascprsr.parseAscFile(outputKmlFilename + ".asc", "1");
                # ascprsr.parseZipAscFile(outputKmlFilename + ".zip", "1");
                index = filename.rfind('.')
                if index > 0:
                    extension = filename[index + 1:].lower()
                    if extension == "zip":
                        print("processing " + filename)
                        asc_parser.parseZipAscFile(filepath)
                    elif extension == "asc":
                        print("processing " + filename)
                        asc_parser.parseAscFile(filepath)
            except Exception as e:
                traceback.print_exc()

    asc_parser.write_con_veh("PSWF", f"{len(asc_parser.pswf_state_changes)} PWF states")
    #asc_parser.write_gps("GPS", str(len(asc_parser.gps_coordinates)) + " GPS coordinates")
    asc_parser.write_entry_exit("Entry/Exit", "n.a. Potential vehicle entries/exits")
    asc_parser.write_exit_history("Exit History", f"{len(asc_parser.exit_triggers)} Potential vehicle exits")
    asc_parser.write_pres_seg("ADAS", f"{len(asc_parser.adas_segments)} ADAS segments")
    asc_parser.write_segment_path("PATH", f"{len(asc_parser.adas_segments)} ADAS segments path")
    asc_parser.write_lids_flaps("LID(s)-FLAP(s)", f"{len(asc_parser.vehicle_properties)} LID-FLAP interactions")
    kml_writer.write_kml_footer()
