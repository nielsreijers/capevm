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
            StringBuffer buf = new StringBuffer();
            StringBuffer fil = new StringBuffer();
            SimUtil.getIDTimeString(buf, sim);
            boolean ret=false;//indicates that it is a return line
            switch (value) {
                case AVRORA_PRINT_STRINGS:
                default://for already formatted variables (i.e. TinyOS printf)
                    for (int i = 0; i <= max; i++) {
                        byte b = a.getDataByte(base + i + 1);
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
                    final int strAddr = getInt16(a, base + 1);
                    for (int i = 0; i <= max; i++) {
                        byte b = a.getDataByte(strAddr + i);
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
                        final byte b = a.getDataByte(bufAddr + i);
                        if (i > 0) {
                            fil.append(" ");
                            buf.append(" ");
                        }
                        fil.append(StringUtil.toHex(b, 2));
                        buf.append(StringUtil.toHex(b, 2));
                    }
                    break;
                case AVRORA_WRITE_CHAR_BUFFER:
                    byte b = a.getDataByte(base + 1);
                    charbuffer.append(String.valueOf((char) b));
                    break;
                case AVRORA_PRINT_CHAR_BUFFER:
                    fil.append(charbuffer.toString());
                    buf.append(charbuffer.toString());
                    charbuffer = new StringBuilder();
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
            System.err.println("Error: " + e.getMessage());
        }
    }
}
