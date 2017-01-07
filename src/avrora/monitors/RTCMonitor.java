package avrora.monitors;

import avrora.sim.Simulator;
import avrora.sim.util.RTCTrace;
import avrora.sim.util.InfusionHeaderParser;
import avrora.core.SourceMapping;
import cck.util.Option;
import cck.util.Util;
import cck.text.Verbose;
import cck.text.Printer;
import cck.text.Terminal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The <code>PrintMonitor</code> gives apps a simple way to tell the
 * simulator to print a string or int to the screen
 *
 * @author John Regehr
 * @author Rodolfo de Paz
 */
public class RTCMonitor extends MonitorFactory {

    protected final Option.Str VARIABLENAME = newOption("VariableName", "rtcMonitorVariable" ,
            "This option specifies the name of the variable marking the base address of " +
            "the memory region to watch.");
    protected final Option.Str BASEADDR = newOption("rtcMonitorBaseAddr", "",
            "This option specifies the starting address in SRAM of the memory region to " +
            "watch for instructions. (If specified, it takes precedence over VariableName).");
    protected final Option.Str FILENAME = newOption("rtc-data-filename", "",
            "This option specifies the name of the file to write the rtc data to. If not " +
            "specifed, the output will be printed to the terminal.");
    protected final Option.Str GRADLEBUILD = newOption("rtc-gradle-build", "",
            "This option specifies the Gradle build directory where the infusion headers " +
            "can be found.");

    static final Printer verbosePrinter = Verbose.getVerbosePrinter("c-print");

    public class Monitor implements avrora.monitors.Monitor {
        private RTCTrace rtctrace;

        Monitor(Simulator s) {
            int base = -1;

            InfusionHeaderParser.basedir = GRADLEBUILD.get();

            if (!BASEADDR.isBlank()) {
                // The address is given directly, so we do not need to look-up the variable.
                base = Integer.parseInt(BASEADDR.get());
            } else {
                // Look for the label that equals the desired variable name inside the map file.
                final SourceMapping map = s.getProgram().getSourceMapping();
                final SourceMapping.Location location = map.getLocation(VARIABLENAME.get());
                if (location != null) {
                    // Strip any memory-region markers from the address.
                    base = location.vma_addr & 0xffff;
                } else {
                    Util.userError("rtc monitor could not find variable \"" +
                            VARIABLENAME.get() + "\"");
                }
            }

            if (base != -1) {
                verbosePrinter.println("rtc monitor monitoring SRAM at " + base);
                rtctrace = new RTCTrace();
                s.insertWatch(rtctrace, base);
            } else {
                verbosePrinter.println("rtc monitor not monitoring any memory region");
            }


        }

        public void report() {
            if (rtctrace != null) {
                String filename = FILENAME.get();
                if (filename == "") {
                    // Print to screen
                    synchronized (Terminal.class) {
                        Terminal.print(rtctrace.toString());
                    }
                } else {
                    // Export output as python file
                    try {
                        Terminal.println("Writing rtc data to " + filename);
                        Files.write(Paths.get(filename), rtctrace.toXmlString().getBytes());
                        Terminal.println("Done.");
                    } catch (Exception e) {
                        Terminal.println("FAILED!!");
                    }
                }
            } else {
                Terminal.print("No rtctrace.");
            }
        }
    }

    public RTCMonitor() {
        super("The \"rtc\" monitor is used for monitoring the runtime compilation process in " +
                "darjeeling. It watches a dedicated range of SRAM to which Darjeeling will write " +
                "the instructions about to be written to Flash so a trace can be generated. " +
                "AvroraRTC.h contains the helper functions used in Darjeeling.");
    }

    public avrora.monitors.Monitor newMonitor(Simulator s) {
        return new Monitor(s);
    }
}
