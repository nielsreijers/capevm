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

package avrora.sim.util;

import avrora.sim.*;
import cck.text.Terminal;


/**
 * <code>MemTimer</code> lets simulated applications start and stop
 * timers
 *
 * @author John Regehr
 */
public class MemTimer extends Simulator.Watch.Empty {
    private static final byte AVRORA_TIMER_START_MAIN      = 1;
    private static final byte AVRORA_TIMER_STOP_MAIN       = 2;
    private static final byte AVRORA_TIMER_SET_MAIN_NUMBER = 3;
    private static final byte AVRORA_TIMER_START_GC        = 4;
    private static final byte AVRORA_TIMER_STOP_GC         = 5;
    private static final byte AVRORA_TIMER_MARK            = 6;

    int base;
    long main_timer_start_time = 0;
    int main_timer_number = 1;
    long gc_timer_start_time = 0;
    long gc_timer_total_time = 0;
    long mark_timer_start_time = 0;

    public MemTimer(int b) {
        base = b;
    }

    public void fireBeforeWrite(State state, int data_addr, byte value) {
        if (data_addr != base) {
            Terminal.printRed("Unexpected interception by printer!");
            System.exit(-1);
        }

        Simulator sim = state.getSimulator();
        AtmelInterpreter a = (AtmelInterpreter) sim.getInterpreter();
        StringBuffer buf = new StringBuffer();
        SimUtil.getIDTimeString(buf, sim);
        buf.append("[avrora.c-timer] ");

        switch(value) {
            case AVRORA_TIMER_START_MAIN:
                if (main_timer_start_time != 0) {
                    Terminal.printRed("multiple starts in a row??\nPROBABLY BECAUSE OF A CRASH: ABORTING\n");
                    System.exit(-1);
                    // buf.append("multiple starts in a row??");
                } else {
                    main_timer_start_time = state.getCycles();
                    buf.append("start");
                }
            break;
            case AVRORA_TIMER_STOP_MAIN:
                if (main_timer_start_time == 0) {
                    buf.append("multiple stops in a row??");
                } else {
                    long stop_time = state.getCycles();
                    long duration = stop_time - main_timer_start_time;
                    buf.append("timer number " + String.valueOf(main_timer_number) + ": " + String.valueOf(duration) + " cycles");
                }
                main_timer_start_time = 0;
            break;
            case AVRORA_TIMER_SET_MAIN_NUMBER:
                this.main_timer_number = a.getDataByte(data_addr+1);
            return;
            case AVRORA_TIMER_START_GC:
                if (main_timer_start_time == 0) { // We only care about garbage collection during benchmarks, so the main timer must be active
                    return;
                }
                if (gc_timer_start_time != 0) {
                    Terminal.printRed("multiple starts in a row??\nPROBABLY BECAUSE OF A CRASH: ABORTING\n");
                    System.exit(-1);
                    // buf.append("multiple starts in a row??");
                } else {
                    gc_timer_start_time = state.getCycles();
                    buf.append("GC start");
                }
            break;
            case AVRORA_TIMER_STOP_GC:
                if (main_timer_start_time == 0) { // We only care about garbage collection during benchmarks, so the main timer must be active
                    return;
                }
                if (gc_timer_start_time == 0) {
                    buf.append("GC timer multiple stops in a row??");
                } else {
                    long stop_time = state.getCycles();
                    long duration = stop_time - gc_timer_start_time;
                    gc_timer_total_time += duration;
                    buf.append("GC ran for " + String.valueOf(duration) + " cycles. Total GC time " + String.valueOf(gc_timer_total_time) + " cycles.");
                }
                gc_timer_start_time = 0;
            break;
            case AVRORA_TIMER_MARK:
                if (main_timer_start_time == 0) { // We only care about marks during benchmarks, so the main timer must be active
                    return;
                }
                int mark_number = a.getDataByte(data_addr+1);
                long mark_time = state.getCycles();
                buf.append("Mark " + mark_number + " at " + (mark_time - mark_timer_start_time - 5) + " cycles since last mark. (already deducted 5 cycles for timer overhead)");
                mark_timer_start_time = mark_time;
            break;
            default:
                buf.append("UNKNOWN COMMAND! " + value + "\n\r");
            break;
        }
        if (buf.length() > 0) {
            Terminal.printRed(buf.toString());
            Terminal.nextln();
        }
    }
}
