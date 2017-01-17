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

import avrora.arch.legacy.LegacyInstr;
import avrora.core.Program;
import avrora.sim.Simulator;
import avrora.sim.State;
import avrora.sim.mcu.MCUProperties;
import cck.text.*;
import cck.util.Option;
import cck.util.Util;
import cck.elf.ELFSectionHeaderTable;

/**
 * The <code>StackMonitor</code> class is a monitor that tracks the height of the program's stack over the
 * execution of the simulation and reports the maximum stack of the program.
 *
 * @author Ben L. Titzer
 */
public class StackMonitor extends MonitorFactory {

    protected final Option.Long SPGUARDADDRESS = newOption("spGuardAddress", 0xFFFFFF ,
            "This option specifies the name of a variable that must be the last in memory and " +
            "marks the boundary to where SP may grow.");
    protected final Option.Long ABORTRANGE = newOption("spGuardAbortRange", 0,
            "Specifies the minimum distance in bytes between SP and the location of spGuardVariable. " +
            "Whenever SP comes closer, the simulation is aborted.");
    protected final Option.Bool PRINTEACHMODIFICATION = newOption("spGuardPrintAll", false ,
            "If this option is true, the SP will be printed to the terminal on each modification.");

    /**
     * The <code>Monitor</code> class implements a monitor for the stack height that inserts a probe after
     * every instruction in the program and checks the stack height after each instruction is executed.
     */
    public class Mon implements Monitor {
        private final SPWatch SPH_watch;
        private final SPWatch SPL_watch;

        private boolean SPinit;
        private int minSP = 0;
        private int maxSP = 0;
        int guardAddress;
        int abortRange;
        boolean printEachModification;
        protected final Simulator simulator;

        Mon(Simulator sim) {
            guardAddress = (int)SPGUARDADDRESS.get();
            if (guardAddress == 0xFFFFFF) {
                ELFSectionHeaderTable sht = sim.getProgram().getELFSectionHeaderTable();
                ELFSectionHeaderTable.Entry32[] sections = sht.getEntries();
                for (ELFSectionHeaderTable.Entry32 section : sections) {
                    String sectionName = section.getName();
                    System.err.println("section " + sectionName);
                    if (sectionName.equals(".bss")) {
                        guardAddress = section.sh_addr + section.sh_size - 0x800000;
                        break;
                    }
                }
            }
            if (guardAddress == 0xFFFFFF) {
                Util.userError("sp guard address not specified and bss section not found");
                System.exit(1);
            }

            abortRange = (int)ABORTRANGE.get();
            printEachModification = PRINTEACHMODIFICATION.get();

            SPH_watch = new SPWatch();
            SPL_watch = new SPWatch();

            SPProbe probe = new SPProbe();

            // insert watches for SP registers
            MCUProperties props = sim.getMicrocontroller().getProperties();
            simulator = sim;
            simulator.insertWatch(SPH_watch, props.getIOReg("SPH"));
            sim.insertWatch(SPL_watch, props.getIOReg("SPL"));
            sim.getInterpreter().getInterruptTable().insertProbe(new IntProbe());

            // insert probes to catch "implicit" updates to SP for certain instructions
            Program program = sim.getProgram();
            for ( int pc = 0; pc < program.program_end; pc = program.getNextPC(pc)) {
                LegacyInstr i = (LegacyInstr)program.readInstr(pc);
                if ( i != null ) {
                    if ( i instanceof LegacyInstr.CALL ) sim.insertProbe(probe, pc);
                    else if ( i instanceof LegacyInstr.ICALL ) sim.insertProbe(probe, pc);
                    else if ( i instanceof LegacyInstr.RCALL) sim.insertProbe(probe, pc);
                    else if ( i instanceof LegacyInstr.RET ) sim.insertProbe(probe, pc);
                    else if ( i instanceof LegacyInstr.RETI ) sim.insertProbe(probe, pc);
                    else if ( i instanceof LegacyInstr.PUSH ) sim.insertProbe(probe, pc);
                    else if ( i instanceof LegacyInstr.POP ) sim.insertProbe(probe, pc);
                }
            }
        }

        /**
         * The <code>report()</code> method generates a textual report after the simulation is complete. The
         * text report contains the 3 smallest stack pointers encountered (tracking all three is necessary
         * because the stack pointer begins at 0 and then is initialized one byte at a time).
         */
        public void report() {
            if ( SPinit ) {
                TermUtil.printSeparator("Stack results for node "+simulator.getID());
                TermUtil.reportQuantity("Maximum stack pointer", StringUtil.addrToString(maxSP), "");
                TermUtil.reportQuantity("Minimum stack pointer", StringUtil.addrToString(minSP), "");
                TermUtil.reportQuantity("Maximum stack size", (maxSP - minSP), "bytes");
                Terminal.nextln();
            } else {
                Terminal.println("No stack pointer information for node "+simulator.getID()+".");
                Terminal.nextln();
            }
        }

        class SPWatch extends Simulator.Watch.Empty {
            boolean written;
            // fire when either SPH or SPL is written
            public void fireAfterWrite(State state, int data_addr, byte value) {
                written = true;
                checkSPWrite(state);
            }
        }

        class IntProbe extends Simulator.InterruptProbe.Empty {
            // fire when any interrupt is invoked
            public void fireAfterInvoke(State s, int inum) {
                newSP(s);
            }
        }

        class SPProbe extends Simulator.Probe.Empty {
            // fire after a call, push, pop, or ret instruction
            public void fireAfter(State state, int pc) {
                newSP(state);
            }
        }

        void checkSPWrite(State state) {
            // only record SP values after both SPH and SPL written
            if ( SPH_watch.written && SPL_watch.written ) {
                newSP(state);
                SPH_watch.written = false;
                SPL_watch.written = false;
            }
        }

        void newSP(State state) {
            int sp = state.getSP();
            int pc = state.getPC();

            if (printEachModification) {
                Terminal.printRed("[avrora.StackMonitor] SP = 0x" + StringUtil.toHex(sp,4) + " PC = 0x" + StringUtil.toHex(pc,6) + "\n");
            }

            if (sp < guardAddress + abortRange) {
                Terminal.printRed("[avrora.StackMonitor] SP too close to or below guard address @ PC=0x" + StringUtil.toHex(pc,6) + ": SP = 0x" + StringUtil.toHex(sp,4) + ", guard address = 0x" + StringUtil.toHex(guardAddress,4) + ", abort range = 0x" + StringUtil.toHex(abortRange,4) + ".\n");
                System.exit(1);
            }

            // record a new stack pointer value
            if ( !SPinit ) {
                maxSP = sp;
                minSP = sp;
                SPinit = true;
            }
            else if ( sp > maxSP ) maxSP = sp;
            else if ( sp < minSP ) minSP = sp;
        }
    }

    /**
     * The constructor for the <code>StackMonitor</code> class builds a new <code>MonitorFactory</code>
     * capable of creating monitors for each <code>Simulator</code> instance passed to the
     * <code>newMonitor()</code> method.
     */
    public StackMonitor() {
        super("The \"stack\" monitor tracks the height of the stack while " +
                "the program executes, reporting the maximum stack height seen.");
    }

    /**
     * The <code>newMonitor()</code> method creates a new monitor that is capable of monitoring the stack
     * height of the program over its execution.
     *
     * @param s the simulator to create a monitor for
     * @return an instance of the <code>Monitor</code> interface for the specified simulator
     */
    public Monitor newMonitor(Simulator s) {
        return new Mon(s);
    }
}
