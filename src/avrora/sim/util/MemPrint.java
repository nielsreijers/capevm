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
            buf.append("[avrora.c-print] ");
            boolean ret=false;//indicates that it is a return line
            switch (value) {
                case AVRORA_PRINT_STRINGS:
                default://for already formatted variables (i.e. TinyOS printf)
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
                case AVRORA_PRINT_REGS:
                    fil.append("R0:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R0) + "\n");   buf.append("R0:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R0) + "\n");
                    fil.append("R1:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R1) + "\n");   buf.append("R1:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R1) + "\n");
                    fil.append("R2:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R2) + "\n");   buf.append("R2:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R2) + "\n");
                    fil.append("R3:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R3) + "\n");   buf.append("R3:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R3) + "\n");
                    fil.append("R4:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R4) + "\n");   buf.append("R4:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R4) + "\n");
                    fil.append("R5:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R5) + "\n");   buf.append("R5:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R5) + "\n");
                    fil.append("R6:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R6) + "\n");   buf.append("R6:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R6) + "\n");
                    fil.append("R7:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R7) + "\n");   buf.append("R7:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R7) + "\n");
                    fil.append("R8:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R8) + "\n");   buf.append("R8:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R8) + "\n");
                    fil.append("R9:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R9) + "\n");   buf.append("R9:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R9) + "\n");
                    fil.append("R10:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R10) + "\n"); buf.append("R10:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R10) + "\n");
                    fil.append("R11:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R11) + "\n"); buf.append("R11:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R11) + "\n");
                    fil.append("R12:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R12) + "\n"); buf.append("R12:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R12) + "\n");
                    fil.append("R13:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R13) + "\n"); buf.append("R13:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R13) + "\n");
                    fil.append("R14:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R14) + "\n"); buf.append("R14:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R14) + "\n");
                    fil.append("R15:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R15) + "\n"); buf.append("R15:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R15) + "\n");
                    fil.append("R16:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R16) + "\n"); buf.append("R16:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R16) + "\n");
                    fil.append("R17:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R17) + "\n"); buf.append("R17:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R17) + "\n");
                    fil.append("R18:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R18) + "\n"); buf.append("R18:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R18) + "\n");
                    fil.append("R19:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R19) + "\n"); buf.append("R19:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R19) + "\n");
                    fil.append("R20:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R20) + "\n"); buf.append("R20:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R20) + "\n");
                    fil.append("R21:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R21) + "\n"); buf.append("R21:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R21) + "\n");
                    fil.append("R22:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R22) + "\n"); buf.append("R22:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R22) + "\n");
                    fil.append("R23:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R23) + "\n"); buf.append("R23:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R23) + "\n");
                    fil.append("R24:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R24) + "\n"); buf.append("R24:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R24) + "\n");
                    fil.append("R25:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R25) + "\n"); buf.append("R25:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R25) + "\n");
                    fil.append("R26:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R26) + "\n"); buf.append("R26:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R26) + "\n");
                    fil.append("R27:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R27) + "\n"); buf.append("R27:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R27) + "\n");
                    fil.append("R28:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R28) + "\n"); buf.append("R28:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R28) + "\n");
                    fil.append("R29:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R29) + "\n"); buf.append("R29:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R29) + "\n");
                    fil.append("R30:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R30) + "\n"); buf.append("R30:" + ((LegacyState)state).getRegisterByte(LegacyRegister.R30) + "\n");
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
            }

            synchronized ( Terminal.class) {
                    if (!ret & fil.length() != 0) Terminal.println(buf.toString());//print in screen if not return line and something to print
                    if (!log.equals("")) printToFile(fil.toString());//print in file
                }
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
