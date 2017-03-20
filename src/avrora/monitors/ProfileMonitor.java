/**
 * Copyright (c) 2004-2005, Regents of the University of California
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * Neither the name of the University of California, Los Angeles nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package avrora.monitors;

import avrora.arch.AbstractInstr;
import avrora.core.Program;
import avrora.sim.Simulator;
import avrora.sim.State;
import avrora.arch.legacy.LegacyInstr;
import avrora.arch.legacy.*;
import cck.stat.StatUtil;
import cck.text.*;
import cck.util.Option;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The <code>ProfileMonitor</code> class represents a monitor that can collect profiling information such as
 * counts and branchcounts about the program as it executes.
 *
 * @author Ben L. Titzer
 */
public class ProfileMonitor extends MonitorFactory {

    public final Option.Bool CYCLES = newOption("record-cycles", true,
            "This option controls whether this monitor will record " +
            "the cycles consumed by each instruction or basic block. ");
    public final Option.Long PERIOD = newOption("period", 0,
            "This option specifies whether the profiling will be exact or periodic. When " +
            "this option is set to non-zero, then a sample of the program counter is taken at " +
            "the specified period in clock cycles, rather than through probes at each instruction.");
    public final Option.Bool CLASSES = newOption("instr-classes", false,
            "This option selects whether the profiling monitor will generate a report of the " +
            "types of instructions that were executed most frequently by the program.");
    public final Option.Str FILENAME = newOption("profile-data-filename", "",
            "This option specifies the name of the file to write the profile data to. If not " +
            "specifed, the output will be printed to the terminal.");

    /**
     * The <code>Monitor</code> inner class contains the probes and formatting code that
     * can report the profile for the program after it has finished executing.
     */
    public class Mon implements Monitor {
        public final Simulator simulator;
        public final Program program;

        public final long[] icount;
        public final long[] itime;
        public final long[] isubroutinetime;
        public class CallStackRecord {
            public int sourcePC;
            public int returnAddress;
            public long time;
        }
        public final Stack<CallStackRecord> callstack;
        private int expectedNextPc = 0;
        private int returnInstructionPc = 0;
        private int returnInstructionSp = 0;

        Mon(Simulator s) {
            simulator = s;
            program = s.getProgram();

            // allocate a global array for the count of each instruction
            icount = new long[program.program_end];
            // allocate a global array for the cycles of each instruction
            itime = new long[program.program_end];
            // allocate a global array for the cycles consumed in a subroutine for each CALL instruction
            isubroutinetime = new long[program.program_end];
            // and a stack to keep track of CALLs/RETs
            callstack = new Stack<CallStackRecord>();

            long period = PERIOD.get();
            if ( period > 0 ) {
                // insert the periodic probe
                s.insertEvent(new PeriodicProfile(period), period);
            } else if ( CYCLES.get() ) {
                // insert the count and cycles probe
                s.insertProbe(new CCProbe());
            } else {
                // insert just the count probe
                s.insertProbe(new CProbe());
            }
        }

        /**
         * The <code>PeriodicProfile</code> class can be used as a simulator event to periodically
         * sample the program counter value. This can be used to get an approximation of
         * the execution profile.
         */
        public class PeriodicProfile implements Simulator.Event {
            private final long period;

            PeriodicProfile(long p) {
                period = p;
            }

            public void fire() {
                icount[simulator.getState().getPC()]++;
                simulator.insertEvent(this, period);
            }
        }

        /**
         * The <code>CCProbe</code> class implements a probe that keeps track of the
         * execution count of each instruction as well as the number of cycles that
         * it has consumed.
         */
        public class CCProbe implements Simulator.Probe {
            protected long timeBegan;

            private void printCall(int pc, int sp, AbstractInstr inst) {
                String padding = new String(new char[callstack.size()*2]).replace('\0', ' ');
                if (inst instanceof LegacyInstr.CALL) {
                    int target = ((LegacyInstr.CALL)inst).imm1;
                    Terminal.println(String.format("%sCALL at 0x%s to 0x%s (SP 0x%s)", padding, Integer.toHexString(pc), Integer.toHexString(target*2), Integer.toHexString(sp)));
                }
                if (inst instanceof LegacyInstr.RCALL) {
                    int offset = ((LegacyInstr.RCALL)inst).imm1;
                    LegacyInstr.RCALL callInst = (LegacyInstr.RCALL)inst;
                    Terminal.println(String.format("%sRCALL at 0x%s by 0x%s to 0x%s (SP 0x%s)", padding, Integer.toHexString(pc), Integer.toHexString(offset*2), Integer.toHexString(pc+2+offset*2), Integer.toHexString(sp)));
                }
                if (inst instanceof LegacyInstr.ICALL) {
                   Terminal.println(String.format("%sICALL at 0x%s to ??? (todo) (SP 0x%s)", padding, Integer.toHexString(pc), Integer.toHexString(sp)));
                }
                if (inst instanceof LegacyInstr.EICALL) {
                    Terminal.println(String.format("%sEICALL at 0x%s to ??? (todo) (SP 0x%s)", padding, Integer.toHexString(pc), Integer.toHexString(sp)));
                }
            }
            private void printRet(int pc, int returnToPc) {
                String padding = new String(new char[callstack.size()*2]).replace('\0', ' ');
                Terminal.println(String.format("%sRET at 0x%s to 0x%s", padding, Integer.toHexString(pc), Integer.toHexString(returnToPc)));
            }

            public void fireBefore(State state, int pc) {
                icount[pc]++;
                timeBegan = state.getCycles();

                if (expectedNextPc != 0 && pc != expectedNextPc) {
                    if (pc != 0x40) { // We may be at 0x40 because of an interrupt
                        Terminal.println(String.format("UNEXPECTED NEXT PC: 0x%s!!", Integer.toHexString(pc)));
                        Terminal.println(String.format("Expected next PC: 0x%s", Integer.toHexString(expectedNextPc)));
                        Terminal.println(String.format("Return instruction PC: 0x%s ", Integer.toHexString(returnInstructionPc)));
                        Terminal.println(String.format("SP at return instruction: 0x%s ", Integer.toHexString(returnInstructionSp)));                        Terminal.println(String.format("Current SP: 0x%s ", Integer.toHexString(state.getSP())));

                        Terminal.println("R0:" + StringUtil.toHex(((LegacyState)state).getRegisterByte(LegacyRegister.R0),2) + " ");   
                        Terminal.println("R1:" + StringUtil.toHex(((LegacyState)state).getRegisterByte(LegacyRegister.R1),2) + " ");   
                        Terminal.println("R2:" + StringUtil.toHex(((LegacyState)state).getRegisterByte(LegacyRegister.R2),2) + " ");   
                        Terminal.println("R3:" + StringUtil.toHex(((LegacyState)state).getRegisterByte(LegacyRegister.R3),2) + " ");   
                        Terminal.println("R4:" + StringUtil.toHex(((LegacyState)state).getRegisterByte(LegacyRegister.R4),2) + " ");   
                        Terminal.println("R5:" + StringUtil.toHex(((LegacyState)state).getRegisterByte(LegacyRegister.R5),2) + " ");   
                        Terminal.println("R6:" + StringUtil.toHex(((LegacyState)state).getRegisterByte(LegacyRegister.R6),2) + " ");   
                        Terminal.println("R7:" + StringUtil.toHex(((LegacyState)state).getRegisterByte(LegacyRegister.R7),2) + " ");   
                        Terminal.println("R8:" + StringUtil.toHex(((LegacyState)state).getRegisterByte(LegacyRegister.R8),2) + " ");   
                        Terminal.println("R9:" + StringUtil.toHex(((LegacyState)state).getRegisterByte(LegacyRegister.R9),2) + " ");   
                        Terminal.println("R10:" + StringUtil.toHex(((LegacyState)state).getRegisterByte(LegacyRegister.R10),2) + " "); 
                        Terminal.println("R11:" + StringUtil.toHex(((LegacyState)state).getRegisterByte(LegacyRegister.R11),2) + " "); 
                        Terminal.println("R12:" + StringUtil.toHex(((LegacyState)state).getRegisterByte(LegacyRegister.R12),2) + " "); 
                        Terminal.println("R13:" + StringUtil.toHex(((LegacyState)state).getRegisterByte(LegacyRegister.R13),2) + " "); 
                        Terminal.println("R14:" + StringUtil.toHex(((LegacyState)state).getRegisterByte(LegacyRegister.R14),2) + " "); 
                        Terminal.println("R15:" + StringUtil.toHex(((LegacyState)state).getRegisterByte(LegacyRegister.R15),2) + " "); 
                        Terminal.println("R16:" + StringUtil.toHex(((LegacyState)state).getRegisterByte(LegacyRegister.R16),2) + " "); 
                        Terminal.println("R17:" + StringUtil.toHex(((LegacyState)state).getRegisterByte(LegacyRegister.R17),2) + " "); 
                        Terminal.println("R18:" + StringUtil.toHex(((LegacyState)state).getRegisterByte(LegacyRegister.R18),2) + " "); 
                        Terminal.println("R19:" + StringUtil.toHex(((LegacyState)state).getRegisterByte(LegacyRegister.R19),2) + " "); 
                        Terminal.println("R20:" + StringUtil.toHex(((LegacyState)state).getRegisterByte(LegacyRegister.R20),2) + " "); 
                        Terminal.println("R21:" + StringUtil.toHex(((LegacyState)state).getRegisterByte(LegacyRegister.R21),2) + " "); 
                        Terminal.println("R22:" + StringUtil.toHex(((LegacyState)state).getRegisterByte(LegacyRegister.R22),2) + " "); 
                        Terminal.println("R23:" + StringUtil.toHex(((LegacyState)state).getRegisterByte(LegacyRegister.R23),2) + " "); 
                        Terminal.println("R24:" + StringUtil.toHex(((LegacyState)state).getRegisterByte(LegacyRegister.R24),2) + " "); 
                        Terminal.println("R25:" + StringUtil.toHex(((LegacyState)state).getRegisterByte(LegacyRegister.R25),2) + " "); 
                        Terminal.println("R26:" + StringUtil.toHex(((LegacyState)state).getRegisterByte(LegacyRegister.R26),2) + " "); 
                        Terminal.println("R27:" + StringUtil.toHex(((LegacyState)state).getRegisterByte(LegacyRegister.R27),2) + " "); 
                        Terminal.println("R28:" + StringUtil.toHex(((LegacyState)state).getRegisterByte(LegacyRegister.R28),2) + " "); 
                        Terminal.println("R29:" + StringUtil.toHex(((LegacyState)state).getRegisterByte(LegacyRegister.R29),2) + " "); 
                        Terminal.println("R30:" + StringUtil.toHex(((LegacyState)state).getRegisterByte(LegacyRegister.R30),2) + " "); 

                        System.exit(0);
                    }
                    expectedNextPc = 0;
                } else {
                    expectedNextPc = 0;
                }
                AbstractInstr inst = state.getInstr(pc);
                if (inst instanceof LegacyInstr.CALL
                        || (inst instanceof LegacyInstr.RCALL && ((LegacyInstr.RCALL)inst).imm1 != 0) // RCALL +0 is used to reserve stack space, but isn't actually a call.
                        || inst instanceof LegacyInstr.ICALL
                        || inst instanceof LegacyInstr.EICALL) {
                    // printCall(pc, state.getSP(), inst);
                    CallStackRecord r = new CallStackRecord();
                    r.sourcePC = pc;
                    r.returnAddress = inst instanceof LegacyInstr.CALL ? pc+4 : pc+2;
                    r.time = state.getCycles();
                    callstack.push(r);
                }
            }

            public void fireAfter(State state, int pc) {
                itime[pc] += state.getCycles() - timeBegan;

                AbstractInstr inst = state.getInstr(pc);
                if (inst instanceof LegacyInstr.RET) {
                    CallStackRecord r = callstack.pop();
                    isubroutinetime[r.sourcePC] += state.getCycles() - r.time;
                    expectedNextPc = r.returnAddress;
                    returnInstructionPc = pc;
                    returnInstructionSp = state.getSP();
                    // printRet(pc, r.sourcePC);
                }
            }
        }

        /**
         * The <code>CProbe</code> class implements a simple probe that keeps a count
         * of how many times each instruction in the program has been executed.
         */
        public class CProbe extends Simulator.Probe.Empty {

            public void fireBefore(State state, int pc) {
                icount[pc]++;
            }
        }

        public void report() {

            computeTotals();
            if ( FILENAME.get() != "" ) {
                reportProfileToFile(FILENAME.get());
            } else {
                reportProfile();
            }

            if ( CLASSES.get() ) {
                reportInstrProfile();
            }
            Terminal.nextln();
        }

        long totalcount;
        long totalcycles;

        private void reportProfile() {
            int imax = icount.length;

            TermUtil.printSeparator("Profiling results for node "+simulator.getID());
            Terminal.printGreen("       Address     Count  Run     Cycles     Cumulative");
            Terminal.nextln();
            TermUtil.printThinSeparator(Terminal.MAXLINE);

            // report the profile for each instruction in the program
            for (int cntr = 0; cntr < imax; cntr = program.getNextPC(cntr)) {
                int start = cntr;
                int runlength = 1;
                long curcount = icount[cntr];
                long cumulcycles = itime[cntr];

                // collapse long runs of equivalent counts (e.g. basic blocks)
                int nextpc;
                for (; cntr < imax - 2; cntr = nextpc) {
                    nextpc = program.getNextPC(cntr);
                    if (nextpc >= icount.length || icount[nextpc] != curcount) break;
                    runlength++;
                    cumulcycles += itime[nextpc];
                }

                // format the results appropriately (columnar)
                String cnt = StringUtil.rightJustify(curcount, 8);
                float pcnt = computePercent(runlength*curcount, cumulcycles);
                String percent = "";
                String addr;
                if (runlength > 1) {
                    // if there is a run, adjust the count and address strings appropriately
                    addr = StringUtil.addrToString(start) + '-' + StringUtil.addrToString(cntr);
                    percent = " x" + runlength;
                } else {
                    addr = "       " + StringUtil.addrToString(start);
                }

                percent = StringUtil.leftJustify(percent, 7);

                // compute the percentage of total execution time
                if (curcount != 0) {
                    percent += StringUtil.rightJustify(cumulcycles, 8);
                    percent += " = " + StringUtil.rightJustify(StringUtil.toFixedFloat(pcnt, 4),8) + " %";
                }

                TermUtil.reportQuantity(' ' + addr, cnt, percent);
            }
        }

        private String profileToPyString() {
            int imax = icount.length;

            StringBuilder sb_single = new StringBuilder();
            StringBuilder sb_cumulative = new StringBuilder();
            sb_single.append("executionCountPerInstruction={\n");
            sb_cumulative.append("executionCountPerBasicblock={\n");

            // report the profile for each instruction in the program
            for (int cntr = 0; cntr < imax; cntr = program.getNextPC(cntr)) {
                sb_single.append(String.format("AddressTraceEntry(address=%s, executions=%d, cycles=%d),\n",
                    StringUtil.addrToString(cntr), icount[cntr], itime[cntr]));
            }

            for (int cntr = 0; cntr < imax; cntr = program.getNextPC(cntr)) {
                int start = cntr;
                int runlength = 1;
                long curcount = icount[cntr];
                long cumulcycles = itime[cntr];

                // collapse long runs of equivalent counts (e.g. basic blocks)
                int nextpc;
                for (; cntr < imax - 2; cntr = nextpc) {
                    nextpc = program.getNextPC(cntr);
                    if (nextpc >= icount.length || icount[nextpc] != curcount)
                        break;
                    runlength++;
                    cumulcycles += itime[nextpc];
                }
                sb_cumulative.append(String.format("BasicBlockTraceEntry(start=%s, end=%s, length=%6s, executions=%10d, cycles=%10d),\n",
                    StringUtil.addrToString(start),
                    StringUtil.addrToString(cntr),
                    "0x" + Integer.toHexString(runlength),
                    curcount,
                    cumulcycles));
            }

            sb_single.append("}\n\n");
            sb_cumulative.append("}\n\n");

            String pyString = "";
            pyString += "from collections import namedtuple\n\r";
            pyString += "AddressTraceEntry = namedtuple(\"AddressTraceEntry\", \"address executions cycles\")\n\r";
            pyString += "BasicBlockTraceEntry = namedtuple(\"AddressTraceEntry\", \"start end length executions cycles\")\n\r";
            pyString += sb_single.toString();
            pyString += sb_cumulative.toString();
            return pyString;
        }

        private String profileToXmlString() {
            int imax = icount.length;

            StringBuilder sb_single = new StringBuilder();
            StringBuilder sb_cumulative = new StringBuilder();
            sb_single.append("<ExecutionCountPerInstruction>\n");
            // sb_cumulative.append("<ExecutionCountPerBasicblock>\n");

            // report the profile for each instruction in the program
            for (int cntr = 0; cntr < imax; cntr = program.getNextPC(cntr)) {
                sb_single.append(String.format("    <Instruction address=\"%s\" executions=\"%d\" cycles=\"%d\" cyclesSubroutine=\"%d\"/>\n",
                    StringUtil.addrToString(cntr), icount[cntr], itime[cntr], isubroutinetime[cntr]));
            }

            // for (int cntr = 0; cntr < imax; cntr = program.getNextPC(cntr)) {
            //     int start = cntr;
            //     int runlength = 1;
            //     long curcount = icount[cntr];
            //     long cumulcycles = itime[cntr];

            //     // collapse long runs of equivalent counts (e.g. basic blocks)
            //     int nextpc;
            //     for (; cntr < imax - 2; cntr = nextpc) {
            //         nextpc = program.getNextPC(cntr);
            //         if (nextpc >= icount.length || icount[nextpc] != curcount)
            //             break;
            //         runlength++;
            //         cumulcycles += itime[nextpc];
            //     }
            //     sb_cumulative.append(String.format("    <Basicblock start=\"%s\" end=\"%s\" length=\"%6s\" executions=\"%10d\" cycles=\"%10d\" />\n",
            //         StringUtil.addrToString(start),
            //         StringUtil.addrToString(cntr),
            //         "0x" + Integer.toHexString(runlength),
            //         curcount,
            //         cumulcycles));
            // }

            sb_single.append("</ExecutionCountPerInstruction>\n\n");
            // sb_cumulative.append("</ExecutionCountPerBasicblock>\n\n");

            // return sb_single.toString() + sb_cumulative.toString();
            return sb_single.toString();
        }
        private void reportProfileToFile(String filename) {
            try {
                Terminal.println("Writing performance trace to " + filename);
                Files.write(Paths.get(filename), profileToXmlString().getBytes());
                Terminal.println("Done.");
            } catch (Exception e) {
                Terminal.println("FAILED!!");
            }
        }

        private void computeTotals() {
            // compute the total cycle count
            totalcycles = StatUtil.sum(itime);
            totalcount = StatUtil.sum(icount);
        }

        private float computePercent(long count, long cycles) {
            if ( CYCLES.get() )
                return 100.0f * cycles / totalcycles;
            else
                return 100.0f * count / totalcount;
        }

        class InstrProfileEntry implements Comparable<InstrProfileEntry> {
            String name;
            long count;
            long cycles;

            public int compareTo(InstrProfileEntry other) {
                if ( this.cycles > 0 ) {
                    if ( other.cycles > this.cycles ) return 1;
                    if ( other.cycles < this.cycles ) return -1;
                } else {
                    if ( other.count > this.count ) return 1;
                    if ( other.count < this.count ) return -1;
                }

                return 0;
            }
        }

        private void reportInstrProfile() {
            List<InstrProfileEntry> profile = computeInstrProfile();

            TermUtil.printSeparator(Terminal.MAXLINE, "Profiling Results by Instruction Type");
            Terminal.printGreen(" Instruction      Count    Cycles   Percent");
            Terminal.nextln();
            TermUtil.printThinSeparator(Terminal.MAXLINE);

            for (InstrProfileEntry ipe : profile) {
                float pcnt = computePercent(ipe.count, ipe.cycles);
                String p = StringUtil.toFixedFloat(pcnt, 4) + " %";
                Terminal.printGreen("   "+StringUtil.rightJustify(ipe.name, 9));
                Terminal.print(": ");
                Terminal.printBrightCyan(StringUtil.rightJustify(ipe.count, 9));
                Terminal.print("  "+StringUtil.rightJustify(ipe.cycles, 8));
                Terminal.print("  "+StringUtil.rightJustify(p, 10));
                Terminal.nextln();
            }
        }

        private List<InstrProfileEntry> computeInstrProfile() {
            HashMap<String,InstrProfileEntry> cmap = new HashMap<String,InstrProfileEntry>();

            for ( int cntr = 0; cntr < icount.length; cntr++ ) {
                if ( icount[cntr] == 0 ) continue;
                AbstractInstr i = program.readInstr(cntr);
                if ( i == null ) continue;
                String variant = i.getName();
                InstrProfileEntry entry = (InstrProfileEntry)cmap.get(variant);
                if  ( entry == null ) {
                    entry = new InstrProfileEntry();
                    entry.name = variant;
                    cmap.put(variant, entry);
                }
                entry.count += icount[cntr];
                entry.cycles += itime[cntr];
            }

            Enumeration<InstrProfileEntry> e = Collections.enumeration(cmap.values());
            List<InstrProfileEntry> l = Collections.list(e);
            Collections.sort(l);
            return l;
        }

    }

    public ProfileMonitor() {
        super("The \"profile\" monitor profiles the execution history " +
                "of every instruction in the program and generates a textual report " +
                "of the execution frequency for all instructions.");
    }

    public Monitor newMonitor(Simulator s) {
        return new Mon(s);
    }
}
