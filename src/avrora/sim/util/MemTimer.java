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
    private static final byte AVRORA_TIMER_START_CURRENT      = 1;
    private static final byte AVRORA_TIMER_STOP_CURRENT       = 2;
    private static final byte AVRORA_TIMER_SET_CURRENT_NUMBER = 3;
    private static final byte AVRORA_TIMER_START_SPECIFIC     = 4;
    private static final byte AVRORA_TIMER_STOP_SPECIFIC      = 5;
    private static final byte AVRORA_TIMER_MARK               = 6;

    private static final byte AVRORA_DEFAULT_TIMER            =   1;
    private static final byte AVRORA_BENCH_NATIVE_TIMER       = 101;
    private static final byte AVRORA_BENCH_AOT_TIMER          = 102;
    private static final byte AVRORA_BENCH_JAVA_TIMER         = 103;
    private static final byte AVRORA_GC_TIMER                 = 104;
    private static final byte AVRORA_REPROG_TIMER             = 105;
    private static final byte AVRORA_AOT_COMPILE_TIMER        = 106;


    int base;
    int current_timer_number = AVRORA_DEFAULT_TIMER;
    long timers_start_time[] = new long[256];
    long timers_total_time[] = new long[256];
    long timers_total_count[] = new long[256];
    long mark_timer_start_time = 0;

    public MemTimer(int b) {
        base = b;
    }

    private StringBuffer getStringBuffer(State state) {
        StringBuffer buf = new StringBuffer();
        SimUtil.getIDTimeString(buf, state.getSimulator());
        buf.append("[avrora.c-timer] ");
        return buf;
    }

    private boolean timerIsActive(int timer) {
        if (timer == AVRORA_GC_TIMER) {
            // Only count GC cycles spent when running AOT code
            return timers_start_time[AVRORA_BENCH_AOT_TIMER] != 0;
        }
        return true;
    }

    private String timerName(int timer) {
        switch(timer) {
            case AVRORA_DEFAULT_TIMER: return "DEFAULT";
            case AVRORA_BENCH_NATIVE_TIMER: return "NATIVE";
            case AVRORA_BENCH_AOT_TIMER: return "AOT";
            case AVRORA_BENCH_JAVA_TIMER: return "JAVA";
            case AVRORA_GC_TIMER: return "GC";
            case AVRORA_REPROG_TIMER: return "REPROG";
            case AVRORA_AOT_COMPILE_TIMER: return "AOT COMPILATION";
            default: return "UNKNOWN TIMER " + timer;
        }
    }

    private void startTimer(int timer, State state) {
        if (!timerIsActive(timer)) {
            return;
        }

        if (timers_start_time[timer] != 0) {
            Terminal.printRed("multiple starts in a row??\nPROBABLY BECAUSE OF A CRASH: ABORTING\n");
            System.exit(-1);
        }

        timers_start_time[timer] = state.getCycles();

        if (timer != AVRORA_REPROG_TIMER) {
            StringBuffer buf = getStringBuffer(state);
            buf.append("start timer " + timerName(timer));
            Terminal.printRed(buf.toString());
            Terminal.nextln();
        }
    }

    private void stopTimer(int timer, State state) {
        if (!timerIsActive(timer)) {
            return;
        }

        if (timers_start_time[timer] == 0) {
            Terminal.printRed("multiple stops in a row??\nPROBABLY BECAUSE OF A CRASH: ABORTING\n");
            System.exit(-1);
        }

        long stop_time = state.getCycles();
        long duration = stop_time - timers_start_time[timer];
        timers_total_time[timer] += duration;
        timers_total_count[timer] += 1;
        timers_start_time[timer] = 0;        

        if (timer != AVRORA_REPROG_TIMER) {
            StringBuffer buf = getStringBuffer(state);
            buf.append("stop timer " + timerName(timer) + ": " + String.valueOf(duration) + " cycles. Total time: " + String.valueOf(timers_total_time[timer]) + " cycles.");
            Terminal.printRed(buf.toString());
            Terminal.nextln();
        }
    }

    public void fireBeforeWrite(State state, int data_addr, byte value) {
        if (data_addr != base) {
            Terminal.printRed("Unexpected interception by printer!");
            System.exit(-1);
        }

        Simulator sim = state.getSimulator();
        AtmelInterpreter a = (AtmelInterpreter) sim.getInterpreter();
        StringBuffer buf = getStringBuffer(state);

        switch(value) {
            case AVRORA_TIMER_START_CURRENT:
                startTimer(this.current_timer_number, state);
            break;
            case AVRORA_TIMER_STOP_CURRENT:
                stopTimer(this.current_timer_number, state);
            break;
            case AVRORA_TIMER_SET_CURRENT_NUMBER:
                this.current_timer_number = a.getDataByte(data_addr+1);
            break;
            case AVRORA_TIMER_START_SPECIFIC:
                startTimer(a.getDataByte(data_addr+1), state);
            break;
            case AVRORA_TIMER_STOP_SPECIFIC:
                stopTimer(a.getDataByte(data_addr+1), state);
            break;
            case AVRORA_TIMER_MARK:
                if (timers_start_time[AVRORA_BENCH_AOT_TIMER] != 0) { // We only care about marks during benchmarks, so the main timer must be active
                    return;
                }
                int mark_number = a.getDataByte(data_addr+1);
                long mark_time = state.getCycles();
                long duration = mark_time - mark_timer_start_time;
                mark_timer_start_time = mark_time;

                buf.append("Mark " + mark_number + " at " + (duration - 5) + " cycles since last mark. (already deducted 5 cycles for timer overhead)");
                Terminal.printRed(buf.toString());
                Terminal.nextln();
            break;
            default:
                buf.append("UNKNOWN COMMAND! " + value + "\n\r");
                Terminal.printRed(buf.toString());
            break;
        }
    }

    public void report() {
        for(int i=0; i<256; i++) {
            if (timers_total_count[i] > 0) {
                Terminal.printRed("[avrora.c-timer] Timer " + timerName(i) + " ran " + timers_total_count[i] + " times for a total of " + timers_total_time[i] + " cycles.");
                Terminal.nextln();                
            }
        }
    }
}
