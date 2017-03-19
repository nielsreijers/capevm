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
import avrora.arch.legacy.*;
import cck.text.StringUtil;
import cck.text.Terminal;
import avrora.core.SourceMapping;

import java.io.*;

/**
 * <code>MemPrint</code> is a simple utility that allows the simulated
 * program to send output to the screen or log it into a file
 *
 * @author John Regehr
 * @author Rodolfo de Paz
 */
public class MemPrint extends Simulator.Watch.Empty {

    int base;
    int max;
    String log;
    StringBuilder charbuffer;

    final byte AVRORA_PRINT_STRINGS                    = 0x2;
    final byte AVRORA_PRINT_2BYTE_HEXADECIMALS         = 0x1;
    final byte AVRORA_PRINT_2BYTE_UNSIGNED_INTEGERS    = 0x3;
    final byte AVRORA_PRINT_2BYTE_SIGNED_INTEGERS      = 0x8;
    final byte AVRORA_PRINT_4BYTE_HEXADECIMALS         = 0x4;
    final byte AVRORA_PRINT_4BYTE_UNSIGNED_INTEGERS    = 0x5;
    final byte AVRORA_PRINT_4BYTE_SIGNED_INTEGERS      = 0x9;
    final byte AVRORA_PRINT_STRING_POINTERS            = 0x6;
    final byte AVRORA_PRINT_BINARY_HEX_DUMPS           = 0x7;
    final byte AVRORA_WRITE_CHAR_BUFFER                = 0xA;
    final byte AVRORA_PRINT_CHAR_BUFFER                = 0xB;
    final byte AVRORA_PRINT_R1                         = 0xC;
    final byte AVRORA_PRINT_SP                         = 0xD;
    final byte AVRORA_PRINT_REGS                       = 0xE;
    final byte AVRORA_PRINT_FLASH_STRING_POINTER       = 0xF;
    final byte AVRORA_PRINT_PANIC                      = 0x10;
    final byte AVRORA_PRINT_FREEHEAPMEMORY             = 0x11;

    public MemPrint(int b, int m, String l) {
        base = b;
        max = m;
        log = l;
        charbuffer = new StringBuilder();
        //Open file first time without append mode
        if (!log.equals("")){
            try{
                new FileWriter(log);
            }catch (Exception e){//Catch exception if any
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    static private int getInt16(AtmelInterpreter a, int offset) {
        final int l = a.getDataByte(offset);
        final int h = a.getDataByte(offset + 1);
        return ((h & 0xff) << 8) + (l & 0xff);
    }
    static private long getInt32(AtmelInterpreter a, int offset) {
        final long l = a.getDataByte(offset);
        final long h1 = a.getDataByte(offset + 1);
        final long h2 = a.getDataByte(offset + 2);
        final long h3 = a.getDataByte(offset + 3);
        return ((h3 & 0xff) << 24) + ((h2 & 0xff) << 16) + ((h1 & 0xff) << 8) + (l & 0xff);
    }

    static private void printRegs(StringBuffer fil, StringBuffer buf, LegacyState state) {
        fil.append("R0:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R0),2) + " ");   buf.append("R0:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R0),2) + " ");
        fil.append("R1:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R1),2) + " ");   buf.append("R1:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R1),2) + " ");
        fil.append("R2:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R2),2) + " ");   buf.append("R2:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R2),2) + " ");
        fil.append("R3:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R3),2) + " ");   buf.append("R3:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R3),2) + " ");
        fil.append("R4:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R4),2) + " ");   buf.append("R4:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R4),2) + " ");
        fil.append("R5:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R5),2) + " ");   buf.append("R5:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R5),2) + " ");
        fil.append("R6:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R6),2) + " ");   buf.append("R6:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R6),2) + " ");
        fil.append("R7:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R7),2) + " ");   buf.append("R7:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R7),2) + " ");
        fil.append("R8:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R8),2) + " ");   buf.append("R8:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R8),2) + " ");
        fil.append("R9:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R9),2) + " ");   buf.append("R9:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R9),2) + " ");
        fil.append("R10:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R10),2) + " "); buf.append("R10:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R10),2) + " ");
        fil.append("R11:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R11),2) + " "); buf.append("R11:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R11),2) + " ");
        fil.append("R12:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R12),2) + " "); buf.append("R12:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R12),2) + " ");
        fil.append("R13:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R13),2) + " "); buf.append("R13:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R13),2) + " ");
        fil.append("R14:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R14),2) + " "); buf.append("R14:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R14),2) + " ");
        fil.append("R15:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R15),2) + " "); buf.append("R15:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R15),2) + " ");
        fil.append("R16:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R16),2) + " "); buf.append("R16:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R16),2) + " ");
        fil.append("R17:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R17),2) + " "); buf.append("R17:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R17),2) + " ");
        fil.append("R18:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R18),2) + " "); buf.append("R18:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R18),2) + " ");
        fil.append("R19:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R19),2) + " "); buf.append("R19:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R19),2) + " ");
        fil.append("R20:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R20),2) + " "); buf.append("R20:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R20),2) + " ");
        fil.append("R21:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R21),2) + " "); buf.append("R21:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R21),2) + " ");
        fil.append("R22:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R22),2) + " "); buf.append("R22:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R22),2) + " ");
        fil.append("R23:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R23),2) + " "); buf.append("R23:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R23),2) + " ");
        fil.append("R24:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R24),2) + " "); buf.append("R24:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R24),2) + " ");
        fil.append("R25:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R25),2) + " "); buf.append("R25:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R25),2) + " ");
        fil.append("R26:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R26),2) + " "); buf.append("R26:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R26),2) + " ");
        fil.append("R27:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R27),2) + " "); buf.append("R27:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R27),2) + " ");
        fil.append("R28:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R28),2) + " "); buf.append("R28:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R28),2) + " ");
        fil.append("R29:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R29),2) + " "); buf.append("R29:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R29),2) + " ");
        fil.append("R30:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R30),2) + " "); buf.append("R30:" + StringUtil.toHex(state.getRegisterByte(LegacyRegister.R30),2) + " ");
    }

    public void fireBeforeWrite(State state, int data_addr, byte value) {
            if (data_addr != base) {
                Terminal.printRed("Unexpected interception by printer!");
            }

            Simulator sim = state.getSimulator();
            AtmelInterpreter a = (AtmelInterpreter) sim.getInterpreter();
            int strAddr;
            byte b;
            StringBuffer buf = new StringBuffer();
            StringBuffer fil = new StringBuffer();
            SimUtil.getIDTimeString(buf, sim);
            buf.append("[avrora.c-print] " + value + " ");
            boolean ret=false;//indicates that it is a return line
            switch (value) {
                case AVRORA_PRINT_STRINGS:
                // default://for already formatted variables (i.e. TinyOS printf)
                    for (int i = 0; i <= max; i++) {
                        b = a.getDataByte(base + i + 1);
                        if (b == 0) break;//break if end of string
                        fil.append(String.valueOf((char) b));
                        if (b != 10) buf.append(String.valueOf((char) b));//not return char
                        else if (i == 0) ret = true;//return line
                    }
                    break;
                case AVRORA_PRINT_2BYTE_HEXADECIMALS:
                case AVRORA_PRINT_2BYTE_UNSIGNED_INTEGERS:
                case AVRORA_PRINT_2BYTE_SIGNED_INTEGERS:
                    final int v = getInt16(a, base + 1);
                    if (value == AVRORA_PRINT_2BYTE_HEXADECIMALS) {
                        fil.append(StringUtil.toHex(v, 4));
                        buf.append(StringUtil.toHex(v, 4));
                        break;
                    } else if (value == AVRORA_PRINT_2BYTE_UNSIGNED_INTEGERS) {
                        fil.append(v);
                        buf.append(v);
                        break;
                    } else if (value == AVRORA_PRINT_2BYTE_SIGNED_INTEGERS) {
                        fil.append((short)v);
                        buf.append((short)v);
                        break;
                    }
                    break;
                case AVRORA_PRINT_4BYTE_HEXADECIMALS:
                case AVRORA_PRINT_4BYTE_UNSIGNED_INTEGERS:
                case AVRORA_PRINT_4BYTE_SIGNED_INTEGERS:
                    final long r = getInt32(a, base + 1);
                    if (value == AVRORA_PRINT_4BYTE_HEXADECIMALS) {
                        fil.append(StringUtil.toHex(r, 8));
                        buf.append(StringUtil.toHex(r, 8));
                        break;
                    } else if (value == AVRORA_PRINT_4BYTE_UNSIGNED_INTEGERS) {
                        fil.append(r);
                        buf.append(r);
                        break;
                    } else if (value == AVRORA_PRINT_4BYTE_SIGNED_INTEGERS) {
                        fil.append((int)r);
                        buf.append((int)r);
                        break;
                    }
                    break;
                case AVRORA_PRINT_STRING_POINTERS:
                    strAddr = getInt16(a, base + 1);
                    for (int i = 0; i <= max; i++) {
                        b = a.getDataByte(strAddr + i);
                        if (b == 0) break;//break if end of string
                        fil.append(String.valueOf((char) b));
                        if (b != 10) buf.append(String.valueOf((char) b));//not return char
                        else if (i == 0) ret = true;//return line
                    }
                    break;
                case AVRORA_PRINT_BINARY_HEX_DUMPS:
                    final int bufAddr = getInt16(a, base + 1);
                    final int bufLen = getInt16(a, base + 3);
                    for (int i = 0; i < bufLen; i++) {
                        b = a.getDataByte(bufAddr + i);
                        if (i > 0) {
                            fil.append(" ");
                            buf.append(" ");
                        }
                        fil.append(StringUtil.toHex(b, 2));
                        buf.append(StringUtil.toHex(b, 2));
                    }
                    break;
                case AVRORA_WRITE_CHAR_BUFFER:
                    b = a.getDataByte(base + 1);
                    charbuffer.append(String.valueOf((char) b));
                    break;
                case AVRORA_PRINT_CHAR_BUFFER:
                    fil.append(charbuffer.toString());
                    buf.append(charbuffer.toString());
                    charbuffer = new StringBuilder();
                    break;
                case AVRORA_PRINT_R1:
                    fil.append("R1:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R1)); buf.append("R1:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R1));
                    break;
                case AVRORA_PRINT_SP:
                    fil.append("SP:" + state.getSP()); buf.append("SP:" + state.getSP());
                    break;
                case 0:
                case AVRORA_PRINT_REGS:
                    printRegs(fil, buf, (LegacyState)state);
                    break;
                case AVRORA_PRINT_FLASH_STRING_POINTER:
                    strAddr = (int)getInt32(a, base + 1);
                    for (int i = 0; i <= max; i++) {
                        b = a.getProgramByte(strAddr + i);
                        if (b == 0) break;//break if end of string
                        fil.append(String.valueOf((char) b));
                        if (b != 10) buf.append(String.valueOf((char) b));//not return char
                        else if (i == 0) ret = true;//return line
                    }
                    break;
                case AVRORA_PRINT_PANIC:
                    fil.append("PANIC!!!! \n"); buf.append("PANIC!!!! \n");
                    int paniccode = a.getDataByte(base + 1);
                    String panictext = "";

                    printDarjeelingHeap(state);

                    // RUNLEVEL_PANIC = 3;
                    switch(paniccode) {
                        case 3+ 0: panictext = "DJ_PANIC_OUT_OF_MEMORY                            "; break;
                        case 3+ 2: panictext = "DJ_PANIC_UNIMPLEMENTED_FEATURE                    "; break;
                        case 3+ 3: panictext = "DJ_PANIC_UNCAUGHT_EXCEPTION                       "; break;
                        case 3+ 4: panictext = "DJ_PANIC_UNSATISFIED_LINK                         "; break;
                        case 3+ 5: panictext = "DJ_PANIC_MALFORMED_INFUSION                       "; break;
                        case 3+ 6: panictext = "DJ_PANIC_ASSERTION_FAILURE                        "; break;
                        case 3+ 7: panictext = "DJ_PANIC_SAFE_POINTER_OVERFLOW                    "; break;
                        case 3+ 8: panictext = "DJ_PANIC_INFUSION_VERSION_MISMATCH                "; break;
                        case 3+ 9: panictext = "DJ_PANIC_UNSUPPORTED_OPCODE                       "; break;
                        case 3+10: panictext = "DJ_PANIC_REPROGRAM_OUTSIDE_REGION                 "; break;
                        case 3+11: panictext = "DJ_PANIC_OFFSET_TOO_LARGE                         "; break;
                        case 3+12: panictext = "DJ_PANIC_AOT_STACKCACHE_IN_USE                    "; break;
                        case 3+13: panictext = "DJ_PANIC_AOT_STACKCACHE_NOTHING_TO_SPILL          "; break;
                        case 3+14: panictext = "DJ_PANIC_AOT_STACKCACHE_PUSHED_REG_NOT_IN_USE     "; break;
                        case 3+15: panictext = "DJ_PANIC_AOT_STACKCACHE_INVALID_POP_TARGET        "; break;
                        case 3+16: panictext = "DJ_PANIC_AOT_STACKCACHE_NO_SPACE_FOR_POP          "; break;
                        case 3+17: panictext = "DJ_PANIC_AOT_MARKLOOP_LOW_WORD_NOT_FOUND          "; break;
                        case 3+18: panictext = "DJ_PANIC_CHECKCAST_FAILED                         "; break;
                        case 3+19: panictext = "DJ_PANIC_ILLEGAL_INTERNAL_STATE_ARRAY_COMPONENT   "; break;
                        case 3+20: panictext = "DJ_PANIC_ILLEGAL_INTERNAL_STATE_THREAD_FRAME_NULL "; break;
                        case 3+21: panictext = "DJ_PANIC_ILLEGAL_INTERNAL_STATE_NO_RUNTIME_CLASS  "; break;
                        case 3+50: panictext = "DJ_PANIC_AOT_ASM_ERROR                            "; break;
                        case 3+51: panictext = "DJ_PANIC_AOT_ASM_ERROR_OFFSET_OUT_OF_RANGE        "; break;
                        default: panictext = "UNKNOWN PANIC CODE: " + paniccode;                                ; break;
                    }
                    fil.append(panictext+"\n"); buf.append(panictext+"\n");

                    fil.append("\n"); buf.append("\n");
                    printRegs(fil, buf, (LegacyState)state);
                    fil.append("\n"); buf.append("\n");
                    fil.append("SP:" + StringUtil.toHex(state.getSP(), 6)); buf.append("SP:" + StringUtil.toHex(state.getSP(), 6));
                    fil.append("\n"); buf.append("\n");
                    fil.append("PC:" + StringUtil.toHex(state.getPC(), 6)); buf.append("PC:" + StringUtil.toHex(state.getPC(), 6));
                    fil.append("\n"); buf.append("\n");
                break;
                case AVRORA_PRINT_FREEHEAPMEMORY:
                    final SourceMapping map = sim.getProgram().getSourceMapping();
                    final SourceMapping.Location left_pointer_location = map.getLocation("left_pointer");
                    int left_pointer = 0;
                    if (left_pointer_location != null) {
                        // Strip any memory-region markers from the address.
                        left_pointer = getInt16(a, left_pointer_location.vma_addr & 0xffff);
                    }
                    final SourceMapping.Location right_pointer_location = map.getLocation("right_pointer");
                    int right_pointer = 0;
                    if (right_pointer_location != null) {
                        // Strip any memory-region markers from the address.
                        right_pointer = getInt16(a, right_pointer_location.vma_addr & 0xffff);
                    }
                    int free = right_pointer - left_pointer;
                    String line = "HEAP: left_pointer 0x" + StringUtil.toHex(left_pointer, 4) + " right_pointer 0x" + StringUtil.toHex(right_pointer, 4) + " free: " + free + "\n";
                    fil.append(line);
                    buf.append(line);
                break;
                default:
                    fil.append("beep:" + value); buf.append("beep:" + value);

                break;
            }

            synchronized ( Terminal.class) {
                    if (!ret & fil.length() != 0) Terminal.println(buf.toString());//print in screen if not return line and something to print
                    if (!log.equals("")) printToFile(fil.toString());//print in file
                }
    }

    public void printDarjeelingHeap(State state) {
        Simulator sim = state.getSimulator();
        AtmelInterpreter a = (AtmelInterpreter) sim.getInterpreter();

        final SourceMapping map = sim.getProgram().getSourceMapping();
        final SourceMapping.Location heapBaseLocation = map.getLocation("heap_base");
        final SourceMapping.Location leftPointerLocation = map.getLocation("left_pointer");
        final int heapBaseAddress = heapBaseLocation.vma_addr & 0xffff;
        final int heapBase = a.getDataUShort(heapBaseAddress);
        final int leftPointerAddress = leftPointerLocation.vma_addr & 0xffff;
        final int leftPointer = a.getDataUShort(leftPointerAddress);

        int finger = heapBase;
        System.out.println("========= HEAP DUMP ========");
        System.out.println("heap_base: " + StringUtil.toHex(heapBaseAddress,4));
        System.out.println("left_pointer: " + StringUtil.toHex(leftPointer,4)); 
        while (finger < leftPointer) {
                // struct _heap_chunk
                // {
                //     uint16_t color:2;
                //     uint16_t size:14;
                //     uint16_t shift;
                //     uint8_t id;
                // }

            int colorAndSize = a.getDataUShort(finger);
            int color = colorAndSize & 0x03;
            int size = colorAndSize >>> 2;
            int shift = a.getDataUShort(finger+2);
            int id = a.getDataByte(finger + 4);

            System.out.println(StringUtil.toHex(finger, 4) + " color: " + color + " size: " + size + " id: " + id + " shift: " + shift);
            if (size == 0) {
                break;
            }
            finger += size;
        }
        System.out.println("===== END OF HEAP DUMP =====");
    }

     /**
     * The <code>PrintToFile</code> method prints to a file the character sent to it
     * @param str string to print
     */
    public void printToFile(String str){
        try{
            BufferedWriter out = new BufferedWriter( new FileWriter(log,true));
            //Str = ControlChars(Str);
            out.write(str);
            out.close();
        }catch (Exception e){//Catch exception if any
            System.err.println("[avrora.c-print] Error: " + e.getMessage());
        }
    }
}
