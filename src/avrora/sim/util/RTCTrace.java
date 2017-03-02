package avrora.sim.util;

import java.util.ArrayList;
import java.util.Stack;
import java.net.URLEncoder;
import avrora.sim.*;
import avrora.arch.AbstractInstr;
import avrora.arch.legacy.LegacyInstr;
import avrora.arch.legacy.LegacyDisassembler;
import cck.text.Terminal;

/**
 * The <code>RTCDisassembler</code> 
 *
 *
 * @author Niels Reijers
 * @see Simulator.Watch
 * @see Counter
 */
public class RTCTrace extends Simulator.Watch.Empty {
    final static int AVRORA_RTC_SINGLEWORDINSTRUCTION     = 1;
    final static int AVRORA_RTC_DOUBLEWORDINSTRUCTION     = 2;
    final static int AVRORA_RTC_STARTMETHOD               = 3;
    final static int AVRORA_RTC_ENDMETHOD                 = 4;
    final static int AVRORA_RTC_JAVAOPCODE                = 5;
    final static int AVRORA_RTC_PATCHINGBRANCHES_ON       = 6;
    final static int AVRORA_RTC_PATCHINGBRANCHES_OFF      = 7;
    final static int AVRORA_RTC_STACKCACHESTATE           = 8;
    final static int AVRORA_RTC_STACKCACHEVALUETAGS       = 9;
    final static int AVRORA_RTC_STACKCACHEPINNEDREGISTERS = 10;
    final static int AVRORA_RTC_STACKCACHESKIPINSTRUCTION = 11;
    final static int AVRORA_RTC_INIT                      = 42;
    final static int AVRORA_RTC_SETCURRENTINFUSION        = 43;
    final static int AVRORA_RTC_RUNTIMEMETHODCALL         = 44;
    final static int AVRORA_RTC_RUNTIMEMETHODCALLRETURN   = 45;
    final static int AVRORA_RTC_PRINTALLRUNTIMEAOTCALLS   = 46;
    final static int AVRORA_RTC_PRINTCURRENTAOTCALLSTACK  = 47;
    final static int AVRORA_RTC_BEEP                      = 50;
    final static int AVRORA_RTC_TERMINATEONEXCEPTION      = 51;
    final static int AVRORA_RTC_EMITPROLOGUE              = 52;


    final static int JVM_NOP = 0;
    final static int JVM_SCONST_M1 = 1;
    final static int JVM_SCONST_0 = 2;
    final static int JVM_SCONST_1 = 3;
    final static int JVM_SCONST_2 = 4;
    final static int JVM_SCONST_3 = 5;
    final static int JVM_SCONST_4 = 6;
    final static int JVM_SCONST_5 = 7;
    final static int JVM_ICONST_M1 = 8;
    final static int JVM_ICONST_0 = 9;
    final static int JVM_ICONST_1 = 10;
    final static int JVM_ICONST_2 = 11;
    final static int JVM_ICONST_3 = 12;
    final static int JVM_ICONST_4 = 13;
    final static int JVM_ICONST_5 = 14;
    final static int JVM_ACONST_NULL = 15;
    final static int JVM_BSPUSH = 16;
    final static int JVM_BIPUSH = 17;
    final static int JVM_SSPUSH = 18;
    final static int JVM_SIPUSH = 19;
    final static int JVM_IIPUSH = 20;
    final static int JVM_LDS = 21;
    final static int JVM_SLOAD = 22;
    final static int JVM_SLOAD_0 = 23;
    final static int JVM_SLOAD_1 = 24;
    final static int JVM_SLOAD_2 = 25;
    final static int JVM_SLOAD_3 = 26;
    final static int JVM_ILOAD = 27;
    final static int JVM_ILOAD_0 = 28;
    final static int JVM_ILOAD_1 = 29;
    final static int JVM_ILOAD_2 = 30;
    final static int JVM_ILOAD_3 = 31;
    final static int JVM_ALOAD = 32;
    final static int JVM_ALOAD_0 = 33;
    final static int JVM_ALOAD_1 = 34;
    final static int JVM_ALOAD_2 = 35;
    final static int JVM_ALOAD_3 = 36;
    final static int JVM_SSTORE = 37;
    final static int JVM_SSTORE_0 = 38;
    final static int JVM_SSTORE_1 = 39;
    final static int JVM_SSTORE_2 = 40;
    final static int JVM_SSTORE_3 = 41;
    final static int JVM_ISTORE = 42;
    final static int JVM_ISTORE_0 = 43;
    final static int JVM_ISTORE_1 = 44;
    final static int JVM_ISTORE_2 = 45;
    final static int JVM_ISTORE_3 = 46;
    final static int JVM_ASTORE = 47;
    final static int JVM_ASTORE_0 = 48;
    final static int JVM_ASTORE_1 = 49;
    final static int JVM_ASTORE_2 = 50;
    final static int JVM_ASTORE_3 = 51;
    final static int JVM_BALOAD = 52;
    final static int JVM_CALOAD = 53;
    final static int JVM_SALOAD = 54;
    final static int JVM_IALOAD = 55;
    final static int JVM_AALOAD = 56;
    final static int JVM_BASTORE = 57;
    final static int JVM_CASTORE = 58;
    final static int JVM_SASTORE = 59;
    final static int JVM_IASTORE = 60;
    final static int JVM_AASTORE = 61;
    final static int JVM_IPOP = 62;
    final static int JVM_IPOP2 = 63;
    final static int JVM_IDUP = 64;
    final static int JVM_IDUP2 = 65;
    final static int JVM_IDUP_X1 = 66;
    final static int JVM_ISWAP_X = 67;
    final static int JVM_APOP = 68;
    final static int JVM_APOP2 = 69;
    final static int JVM_ADUP = 70;
    final static int JVM_ADUP2 = 71;
    final static int JVM_ADUP_X1 = 72;
    final static int JVM_ADUP_X2 = 73;
    final static int JVM_ASWAP = 74;
    final static int JVM_GETFIELD_B = 75;
    final static int JVM_GETFIELD_C = 76;
    final static int JVM_GETFIELD_S = 77;
    final static int JVM_GETFIELD_I = 78;
    final static int JVM_GETFIELD_A = 79;
    final static int JVM_PUTFIELD_B = 80;
    final static int JVM_PUTFIELD_C = 81;
    final static int JVM_PUTFIELD_S = 82;
    final static int JVM_PUTFIELD_I = 83;
    final static int JVM_PUTFIELD_A = 84;
    final static int JVM_GETSTATIC_B = 85;
    final static int JVM_GETSTATIC_C = 86;
    final static int JVM_GETSTATIC_S = 87;
    final static int JVM_GETSTATIC_I = 88;
    final static int JVM_GETSTATIC_A = 89;
    final static int JVM_PUTSTATIC_B = 90;
    final static int JVM_PUTSTATIC_C = 91;
    final static int JVM_PUTSTATIC_S = 92;
    final static int JVM_PUTSTATIC_I = 93;
    final static int JVM_PUTSTATIC_A = 94;
    final static int JVM_SADD = 95;
    final static int JVM_SSUB = 96;
    final static int JVM_SMUL = 97;
    final static int JVM_SDIV = 98;
    final static int JVM_SREM = 99;
    final static int JVM_SNEG = 100;
    final static int JVM_SSHL = 101;
    final static int JVM_SSHR = 102;
    final static int JVM_SUSHR = 103;
    final static int JVM_SAND = 104;
    final static int JVM_SOR = 105;
    final static int JVM_SXOR = 106;
    final static int JVM_IADD = 107;
    final static int JVM_ISUB = 108;
    final static int JVM_IMUL = 109;
    final static int JVM_IDIV = 110;
    final static int JVM_IREM = 111;
    final static int JVM_INEG = 112;
    final static int JVM_ISHL = 113;
    final static int JVM_ISHR = 114;
    final static int JVM_IUSHR = 115;
    final static int JVM_IAND = 116;
    final static int JVM_IOR = 117;
    final static int JVM_IXOR = 118;
    final static int JVM_BINC = 119;
    final static int JVM_SINC = 120;
    final static int JVM_IINC = 121;
    final static int JVM_S2B = 122;
    final static int JVM_S2I = 123;
    final static int JVM_I2B = 124;
    final static int JVM_I2S = 125;
    final static int JVM_IIFEQ = 126;
    final static int JVM_IIFNE = 127;
    final static int JVM_IIFLT = 128;
    final static int JVM_IIFGE = 129;
    final static int JVM_IIFGT = 130;
    final static int JVM_IIFLE = 131;
    final static int JVM_IFNULL = 132;
    final static int JVM_IFNONNULL = 133;
    final static int JVM_IF_SCMPEQ = 134;
    final static int JVM_IF_SCMPNE = 135;
    final static int JVM_IF_SCMPLT = 136;
    final static int JVM_IF_SCMPGE = 137;
    final static int JVM_IF_SCMPGT = 138;
    final static int JVM_IF_SCMPLE = 139;
    final static int JVM_IF_ICMPEQ = 140;
    final static int JVM_IF_ICMPNE = 141;
    final static int JVM_IF_ICMPLT = 142;
    final static int JVM_IF_ICMPGE = 143;
    final static int JVM_IF_ICMPGT = 144;
    final static int JVM_IF_ICMPLE = 145;
    final static int JVM_IF_ACMPEQ = 146;
    final static int JVM_IF_ACMPNE = 147;
    final static int JVM_GOTO = 148;
    final static int JVM_GOTO_W = 149;
    final static int JVM_TABLESWITCH = 150;
    final static int JVM_LOOKUPSWITCH = 151;
    final static int JVM_SRETURN = 152;
    final static int JVM_IRETURN = 153;
    final static int JVM_ARETURN = 154;
    final static int JVM_RETURN = 155;
    final static int JVM_INVOKEVIRTUAL = 156;
    final static int JVM_INVOKESPECIAL = 157;
    final static int JVM_INVOKESTATIC = 158;
    final static int JVM_INVOKEINTERFACE = 159;
    final static int JVM_NEW = 160;
    final static int JVM_NEWARRAY = 161;
    final static int JVM_ANEWARRAY = 162;
    final static int JVM_ARRAYLENGTH = 163;
    final static int JVM_ATHROW = 164;
    final static int JVM_CHECKCAST = 165;
    final static int JVM_INSTANCEOF = 166;
    final static int JVM_MONITORENTER = 167;
    final static int JVM_MONITOREXIT = 168;
    final static int JVM_IDUP_X2 = 169;
    final static int JVM_IINC_W = 170;
    final static int JVM_SINC_W = 171;
    final static int JVM_I2C = 172;
    final static int JVM_S2C = 173;
    final static int JVM_B2C = 174;
    final static int JVM_IDUP_X = 175;
    final static int JVM_SIFEQ = 176;
    final static int JVM_SIFNE = 177;
    final static int JVM_SIFLT = 178;
    final static int JVM_SIFGE = 179;
    final static int JVM_SIFGT = 180;
    final static int JVM_SIFLE = 181;
    final static int JVM_LCONST_0 = 182;
    final static int JVM_LCONST_1 = 183;
    final static int JVM_LLOAD = 184;
    final static int JVM_LLOAD_0 = 185;
    final static int JVM_LLOAD_1 = 186;
    final static int JVM_LLOAD_2 = 187;
    final static int JVM_LLOAD_3 = 188;
    final static int JVM_LLPUSH = 189;
    final static int JVM_LSTORE = 190;
    final static int JVM_LSTORE_0 = 191;
    final static int JVM_LSTORE_1 = 192;
    final static int JVM_LSTORE_2 = 193;
    final static int JVM_LSTORE_3 = 194;
    final static int JVM_LALOAD = 195;
    final static int JVM_LASTORE = 196;
    final static int JVM_GETFIELD_L = 197;
    final static int JVM_PUTFIELD_L = 198;
    final static int JVM_GETSTATIC_L = 199;
    final static int JVM_PUTSTATIC_L = 200;
    final static int JVM_LADD = 201;
    final static int JVM_LSUB = 202;
    final static int JVM_LMUL = 203;
    final static int JVM_LDIV = 204;
    final static int JVM_LREM = 205;
    final static int JVM_LNEG = 206;
    final static int JVM_LSHL = 207;
    final static int JVM_LSHR = 208;
    final static int JVM_LUSHR = 209;
    final static int JVM_LAND = 210;
    final static int JVM_LOR = 211;
    final static int JVM_LXOR = 212;
    final static int JVM_LRETURN = 213;
    final static int JVM_L2I = 214;
    final static int JVM_L2S = 215;
    final static int JVM_I2L = 216;
    final static int JVM_S2L = 217;
    final static int JVM_LCMP = 218;
    final static int JVM_BRTARGET = 219;
    final static int JVM_MARKLOOP_START = 220;
    final static int JVM_MARKLOOP_END = 221;

    final static LegacyDisassembler disasm = new LegacyDisassembler();

    private static int bytesToInt(int hi, int lo) { return (short)( ((hi&0xFF)<<8) | (lo&0xFF) ); }
    private static int bytesToInt(int hi3, int hi2, int hi1, int lo) { return ( ((hi3&0xFF)<<24) | ((hi2&0xFF)<<16) | ((hi1&0xFF)<<8) | (lo&0xFF) ); }
    private String opcode2string(int[] opcode, String currentInfusion) {
        String referencedInfusionName;
        switch (opcode[0]) {
            case JVM_NOP: return "JVM_NOP";
            case JVM_SCONST_M1: return "JVM_SCONST_M1";
            case JVM_SCONST_0: return "JVM_SCONST_0";
            case JVM_SCONST_1: return "JVM_SCONST_1";
            case JVM_SCONST_2: return "JVM_SCONST_2";
            case JVM_SCONST_3: return "JVM_SCONST_3";
            case JVM_SCONST_4: return "JVM_SCONST_4";
            case JVM_SCONST_5: return "JVM_SCONST_5";
            case JVM_ICONST_M1: return "JVM_ICONST_M1";
            case JVM_ICONST_0: return "JVM_ICONST_0";
            case JVM_ICONST_1: return "JVM_ICONST_1";
            case JVM_ICONST_2: return "JVM_ICONST_2";
            case JVM_ICONST_3: return "JVM_ICONST_3";
            case JVM_ICONST_4: return "JVM_ICONST_4";
            case JVM_ICONST_5: return "JVM_ICONST_5";
            case JVM_ACONST_NULL: return "JVM_ACONST_NULL";
            case JVM_BSPUSH: return "JVM_BSPUSH         " + opcode[1];
            case JVM_BIPUSH: return "JVM_BIPUSH         " + opcode[1];
            case JVM_SSPUSH: return "JVM_SSPUSH         " + bytesToInt(opcode[1], opcode[2]);
            case JVM_SIPUSH: return "JVM_SIPUSH         " + bytesToInt(opcode[1], opcode[2]);
            case JVM_IIPUSH: return "JVM_IIPUSH         " + bytesToInt(opcode[1], opcode[2], opcode[3], opcode[4]);
            case JVM_LDS: return "JVM_LDS           Infusion:" + opcode[1] + " String:" + opcode[2];
            case JVM_SLOAD: return "JVM_SLOAD           " + opcode[1];
            case JVM_SLOAD_0: return "JVM_SLOAD_0";
            case JVM_SLOAD_1: return "JVM_SLOAD_1";
            case JVM_SLOAD_2: return "JVM_SLOAD_2";
            case JVM_SLOAD_3: return "JVM_SLOAD_3";
            case JVM_ILOAD: return "JVM_ILOAD           " + opcode[1];
            case JVM_ILOAD_0: return "JVM_ILOAD_0";
            case JVM_ILOAD_1: return "JVM_ILOAD_1";
            case JVM_ILOAD_2: return "JVM_ILOAD_2";
            case JVM_ILOAD_3: return "JVM_ILOAD_3";
            case JVM_ALOAD: return "JVM_ALOAD           " + opcode[1];
            case JVM_ALOAD_0: return "JVM_ALOAD_0";
            case JVM_ALOAD_1: return "JVM_ALOAD_1";
            case JVM_ALOAD_2: return "JVM_ALOAD_2";
            case JVM_ALOAD_3: return "JVM_ALOAD_3";
            case JVM_SSTORE: return "JVM_SSTORE         " + opcode[1];
            case JVM_SSTORE_0: return "JVM_SSTORE_0";
            case JVM_SSTORE_1: return "JVM_SSTORE_1";
            case JVM_SSTORE_2: return "JVM_SSTORE_2";
            case JVM_SSTORE_3: return "JVM_SSTORE_3";
            case JVM_ISTORE: return "JVM_ISTORE         " + opcode[1];
            case JVM_ISTORE_0: return "JVM_ISTORE_0";
            case JVM_ISTORE_1: return "JVM_ISTORE_1";
            case JVM_ISTORE_2: return "JVM_ISTORE_2";
            case JVM_ISTORE_3: return "JVM_ISTORE_3";
            case JVM_ASTORE: return "JVM_ASTORE         " + opcode[1];
            case JVM_ASTORE_0: return "JVM_ASTORE_0";
            case JVM_ASTORE_1: return "JVM_ASTORE_1";
            case JVM_ASTORE_2: return "JVM_ASTORE_2";
            case JVM_ASTORE_3: return "JVM_ASTORE_3";
            case JVM_BALOAD: return "JVM_BALOAD";
            case JVM_CALOAD: return "JVM_CALOAD";
            case JVM_SALOAD: return "JVM_SALOAD";
            case JVM_IALOAD: return "JVM_IALOAD";
            case JVM_AALOAD: return "JVM_AALOAD";
            case JVM_BASTORE: return "JVM_BASTORE";
            case JVM_CASTORE: return "JVM_CASTORE";
            case JVM_SASTORE: return "JVM_SASTORE";
            case JVM_IASTORE: return "JVM_IASTORE";
            case JVM_AASTORE: return "JVM_AASTORE";
            case JVM_IPOP: return "JVM_IPOP";
            case JVM_IPOP2: return "JVM_IPOP2";
            case JVM_IDUP: return "JVM_IDUP";
            case JVM_IDUP2: return "JVM_IDUP2";
            case JVM_IDUP_X1: return "JVM_IDUP_X1";
            case JVM_ISWAP_X: return "JVM_ISWAP_X";
            case JVM_APOP: return "JVM_APOP";
            case JVM_APOP2: return "JVM_APOP2";
            case JVM_ADUP: return "JVM_ADUP";
            case JVM_ADUP2: return "JVM_ADUP2";
            case JVM_ADUP_X1: return "JVM_ADUP_X1";
            case JVM_ADUP_X2: return "JVM_ADUP_X2";
            case JVM_ASWAP: return "JVM_ASWAP";
            case JVM_GETFIELD_B: return "JVM_GETFIELD_B   " + bytesToInt(opcode[1], opcode[2]);
            case JVM_GETFIELD_C: return "JVM_GETFIELD_C   " + bytesToInt(opcode[1], opcode[2]);
            case JVM_GETFIELD_S: return "JVM_GETFIELD_S   " + bytesToInt(opcode[1], opcode[2]);
            case JVM_GETFIELD_I: return "JVM_GETFIELD_I   " + bytesToInt(opcode[1], opcode[2]);
            case JVM_GETFIELD_A: return "JVM_GETFIELD_A   " + bytesToInt(opcode[1], opcode[2]);
            case JVM_PUTFIELD_B: return "JVM_PUTFIELD_B   " + bytesToInt(opcode[1], opcode[2]);
            case JVM_PUTFIELD_C: return "JVM_PUTFIELD_C   " + bytesToInt(opcode[1], opcode[2]);
            case JVM_PUTFIELD_S: return "JVM_PUTFIELD_S   " + bytesToInt(opcode[1], opcode[2]);
            case JVM_PUTFIELD_I: return "JVM_PUTFIELD_I   " + bytesToInt(opcode[1], opcode[2]);
            case JVM_PUTFIELD_A: return "JVM_PUTFIELD_A   " + bytesToInt(opcode[1], opcode[2]);
            case JVM_GETSTATIC_B: return "JVM_GETSTATIC_B " + InfusionHeaderParser.getParser(currentInfusion).getReferencedInfusionName(opcode[1]) + "." + opcode[2];
            case JVM_GETSTATIC_C: return "JVM_GETSTATIC_C " + InfusionHeaderParser.getParser(currentInfusion).getReferencedInfusionName(opcode[1]) + "." + opcode[2];
            case JVM_GETSTATIC_S: return "JVM_GETSTATIC_S " + InfusionHeaderParser.getParser(currentInfusion).getReferencedInfusionName(opcode[1]) + "." + opcode[2];
            case JVM_GETSTATIC_I: return "JVM_GETSTATIC_I " + InfusionHeaderParser.getParser(currentInfusion).getReferencedInfusionName(opcode[1]) + "." + opcode[2];
            case JVM_GETSTATIC_A: return "JVM_GETSTATIC_A " + InfusionHeaderParser.getParser(currentInfusion).getReferencedInfusionName(opcode[1]) + "." + opcode[2];
            case JVM_PUTSTATIC_B: return "JVM_PUTSTATIC_B " + InfusionHeaderParser.getParser(currentInfusion).getReferencedInfusionName(opcode[1]) + "." + opcode[2];
            case JVM_PUTSTATIC_C: return "JVM_PUTSTATIC_C " + InfusionHeaderParser.getParser(currentInfusion).getReferencedInfusionName(opcode[1]) + "." + opcode[2];
            case JVM_PUTSTATIC_S: return "JVM_PUTSTATIC_S " + InfusionHeaderParser.getParser(currentInfusion).getReferencedInfusionName(opcode[1]) + "." + opcode[2];
            case JVM_PUTSTATIC_I: return "JVM_PUTSTATIC_I " + InfusionHeaderParser.getParser(currentInfusion).getReferencedInfusionName(opcode[1]) + "." + opcode[2];
            case JVM_PUTSTATIC_A: return "JVM_PUTSTATIC_A " + InfusionHeaderParser.getParser(currentInfusion).getReferencedInfusionName(opcode[1]) + "." + opcode[2];
            case JVM_SADD: return "JVM_SADD";
            case JVM_SSUB: return "JVM_SSUB";
            case JVM_SMUL: return "JVM_SMUL";
            case JVM_SDIV: return "JVM_SDIV";
            case JVM_SREM: return "JVM_SREM";
            case JVM_SNEG: return "JVM_SNEG";
            case JVM_SSHL: return "JVM_SSHL";
            case JVM_SSHR: return "JVM_SSHR";
            case JVM_SUSHR: return "JVM_SUSHR";
            case JVM_SAND: return "JVM_SAND";
            case JVM_SOR: return "JVM_SOR";
            case JVM_SXOR: return "JVM_SXOR";
            case JVM_IADD: return "JVM_IADD";
            case JVM_ISUB: return "JVM_ISUB";
            case JVM_IMUL: return "JVM_IMUL";
            case JVM_IDIV: return "JVM_IDIV";
            case JVM_IREM: return "JVM_IREM";
            case JVM_INEG: return "JVM_INEG";
            case JVM_ISHL: return "JVM_ISHL";
            case JVM_ISHR: return "JVM_ISHR";
            case JVM_IUSHR: return "JVM_IUSHR";
            case JVM_IAND: return "JVM_IAND";
            case JVM_IOR: return "JVM_IOR";
            case JVM_IXOR: return "JVM_IXOR";
            case JVM_BINC: return "JVM_BINC";
            case JVM_SINC: return "JVM_SINC";
            case JVM_IINC: return "JVM_IINC         Local:" + opcode[1] + " Increment:" + opcode[2];
            case JVM_S2B: return "JVM_S2B";
            case JVM_S2I: return "JVM_S2I";
            case JVM_I2B: return "JVM_I2B";
            case JVM_I2S: return "JVM_I2S";
            case JVM_IIFEQ:        return "JVM_IIFEQ        Offset:" + bytesToInt(opcode[1], opcode[2]) + " Br.target: " + bytesToInt(opcode[3], opcode[4]);
            case JVM_IIFNE:        return "JVM_IIFNE        Offset:" + bytesToInt(opcode[1], opcode[2]) + " Br.target: " + bytesToInt(opcode[3], opcode[4]);
            case JVM_IIFLT:        return "JVM_IIFLT        Offset:" + bytesToInt(opcode[1], opcode[2]) + " Br.target: " + bytesToInt(opcode[3], opcode[4]);
            case JVM_IIFGE:        return "JVM_IIFGE        Offset:" + bytesToInt(opcode[1], opcode[2]) + " Br.target: " + bytesToInt(opcode[3], opcode[4]);
            case JVM_IIFGT:        return "JVM_IIFGT        Offset:" + bytesToInt(opcode[1], opcode[2]) + " Br.target: " + bytesToInt(opcode[3], opcode[4]);
            case JVM_IIFLE:        return "JVM_IIFLE        Offset:" + bytesToInt(opcode[1], opcode[2]) + " Br.target: " + bytesToInt(opcode[3], opcode[4]);
            case JVM_IFNULL:       return "JVM_IFNULL       Offset:" + bytesToInt(opcode[1], opcode[2]) + " Br.target: " + bytesToInt(opcode[3], opcode[4]);
            case JVM_IFNONNULL:    return "JVM_IFNONNULL    Offset:" + bytesToInt(opcode[1], opcode[2]) + " Br.target: " + bytesToInt(opcode[3], opcode[4]);
            case JVM_IF_SCMPEQ:    return "JVM_IF_SCMPEQ    Offset:" + bytesToInt(opcode[1], opcode[2]) + " Br.target: " + bytesToInt(opcode[3], opcode[4]);
            case JVM_IF_SCMPNE:    return "JVM_IF_SCMPNE    Offset:" + bytesToInt(opcode[1], opcode[2]) + " Br.target: " + bytesToInt(opcode[3], opcode[4]);
            case JVM_IF_SCMPLT:    return "JVM_IF_SCMPLT    Offset:" + bytesToInt(opcode[1], opcode[2]) + " Br.target: " + bytesToInt(opcode[3], opcode[4]);
            case JVM_IF_SCMPGE:    return "JVM_IF_SCMPGE    Offset:" + bytesToInt(opcode[1], opcode[2]) + " Br.target: " + bytesToInt(opcode[3], opcode[4]);
            case JVM_IF_SCMPGT:    return "JVM_IF_SCMPGT    Offset:" + bytesToInt(opcode[1], opcode[2]) + " Br.target: " + bytesToInt(opcode[3], opcode[4]);
            case JVM_IF_SCMPLE:    return "JVM_IF_SCMPLE    Offset:" + bytesToInt(opcode[1], opcode[2]) + " Br.target: " + bytesToInt(opcode[3], opcode[4]);
            case JVM_IF_ICMPEQ:    return "JVM_IF_ICMPEQ    Offset:" + bytesToInt(opcode[1], opcode[2]) + " Br.target: " + bytesToInt(opcode[3], opcode[4]);
            case JVM_IF_ICMPNE:    return "JVM_IF_ICMPNE    Offset:" + bytesToInt(opcode[1], opcode[2]) + " Br.target: " + bytesToInt(opcode[3], opcode[4]);
            case JVM_IF_ICMPLT:    return "JVM_IF_ICMPLT    Offset:" + bytesToInt(opcode[1], opcode[2]) + " Br.target: " + bytesToInt(opcode[3], opcode[4]);
            case JVM_IF_ICMPGE:    return "JVM_IF_ICMPGE    Offset:" + bytesToInt(opcode[1], opcode[2]) + " Br.target: " + bytesToInt(opcode[3], opcode[4]);
            case JVM_IF_ICMPGT:    return "JVM_IF_ICMPGT    Offset:" + bytesToInt(opcode[1], opcode[2]) + " Br.target: " + bytesToInt(opcode[3], opcode[4]);
            case JVM_IF_ICMPLE:    return "JVM_IF_ICMPLE    Offset:" + bytesToInt(opcode[1], opcode[2]) + " Br.target: " + bytesToInt(opcode[3], opcode[4]);
            case JVM_IF_ACMPEQ:    return "JVM_IF_ACMPEQ    Offset:" + bytesToInt(opcode[1], opcode[2]) + " Br.target: " + bytesToInt(opcode[3], opcode[4]);
            case JVM_IF_ACMPNE:    return "JVM_IF_ACMPNE    Offset:" + bytesToInt(opcode[1], opcode[2]) + " Br.target: " + bytesToInt(opcode[3], opcode[4]);
            case JVM_GOTO:         return "JVM_GOTO         Offset:" + bytesToInt(opcode[1], opcode[2]) + " Br.target: " + bytesToInt(opcode[3], opcode[4]);
            case JVM_GOTO_W:       return "JVM_GOTO_W       Offset:" + bytesToInt(opcode[1], opcode[2]) + " Br.target: " + bytesToInt(opcode[3], opcode[4]);
            case JVM_TABLESWITCH:  return "JVM_TABLESWITCH  OPERANDS NOT AVAILABLE IN CURRENT TRACER";
            case JVM_LOOKUPSWITCH: return "JVM_LOOKUPSWITCH OPERANDS NOT AVAILABLE IN CURRENT TRACER";
            case JVM_SRETURN: return "JVM_SRETURN";
            case JVM_IRETURN: return "JVM_IRETURN";
            case JVM_ARETURN: return "JVM_ARETURN";
            case JVM_RETURN: return "JVM_RETURN";
            case JVM_INVOKEVIRTUAL:   referencedInfusionName=InfusionHeaderParser.getParser(currentInfusion).getReferencedInfusionName(opcode[1]); return   "JVM_INVOKEVIRTUAL " + opcode[1] + "." + opcode[2] + "  " + referencedInfusionName + "." + urlencode(InfusionHeaderParser.getParser(referencedInfusionName).getMethodDef_name_and_signature(opcode[2]));
            case JVM_INVOKESPECIAL:   referencedInfusionName=InfusionHeaderParser.getParser(currentInfusion).getReferencedInfusionName(opcode[1]); return   "JVM_INVOKESPECIAL " + opcode[1] + "." + opcode[2] + "  " + referencedInfusionName + "." + urlencode(InfusionHeaderParser.getParser(referencedInfusionName).getMethodImpl_name_and_signature(opcode[2]));
            case JVM_INVOKESTATIC:    referencedInfusionName=InfusionHeaderParser.getParser(currentInfusion).getReferencedInfusionName(opcode[1]); return    "JVM_INVOKESTATIC " + opcode[1] + "." + opcode[2] + "  " + referencedInfusionName + "." + urlencode(InfusionHeaderParser.getParser(referencedInfusionName).getMethodImpl_name_and_signature(opcode[2]));
            case JVM_INVOKEINTERFACE: referencedInfusionName=InfusionHeaderParser.getParser(currentInfusion).getReferencedInfusionName(opcode[1]); return "JVM_INVOKEINTERFACE " + opcode[1] + "." + opcode[2] + "  " + referencedInfusionName + "." + urlencode(InfusionHeaderParser.getParser(referencedInfusionName).getMethodDef_name_and_signature(opcode[2]));
            case JVM_NEW: return "JVM_NEW           Infusion:" + opcode[1] + " Class:" + opcode[2];
            case JVM_NEWARRAY: return "JVM_NEWARRAY     Element type:" + opcode[1];
            case JVM_ANEWARRAY: return "JVM_ANEWARRAY       Infusion:" + opcode[1] + " String:" + opcode[2];
            case JVM_ARRAYLENGTH: return "JVM_ARRAYLENGTH";
            case JVM_ATHROW: return "JVM_ATHROW";
            case JVM_CHECKCAST: return "JVM_CHECKCAST       Infusion:" + opcode[1] + " Class:" + opcode[2];
            case JVM_INSTANCEOF: return "JVM_INSTANCEOF Infusion:" + opcode[1] + " Class:" + opcode[2];
            case JVM_MONITORENTER: return "JVM_MONITORENTER";
            case JVM_MONITOREXIT: return "JVM_MONITOREXIT";
            case JVM_IDUP_X2: return "JVM_IDUP_X2";
            case JVM_IINC_W: return "JVM_IINC_W     Local:" + opcode[1] + " Increment:" + bytesToInt(opcode[2], opcode[3]);
            case JVM_SINC_W: return "JVM_SINC_W";
            case JVM_I2C: return "JVM_I2C";
            case JVM_S2C: return "JVM_S2C";
            case JVM_B2C: return "JVM_B2C";
            case JVM_IDUP_X: return "JVM_IDUP_X";
            case JVM_SIFEQ:        return "JVM_SIFEQ        Offset:" + bytesToInt(opcode[1], opcode[2]) + " Br.target: " + bytesToInt(opcode[3], opcode[4]);
            case JVM_SIFNE:        return "JVM_SIFNE        Offset:" + bytesToInt(opcode[1], opcode[2]) + " Br.target: " + bytesToInt(opcode[3], opcode[4]);
            case JVM_SIFLT:        return "JVM_SIFLT        Offset:" + bytesToInt(opcode[1], opcode[2]) + " Br.target: " + bytesToInt(opcode[3], opcode[4]);
            case JVM_SIFGE:        return "JVM_SIFGE        Offset:" + bytesToInt(opcode[1], opcode[2]) + " Br.target: " + bytesToInt(opcode[3], opcode[4]);
            case JVM_SIFGT:        return "JVM_SIFGT        Offset:" + bytesToInt(opcode[1], opcode[2]) + " Br.target: " + bytesToInt(opcode[3], opcode[4]);
            case JVM_SIFLE:        return "JVM_SIFLE        Offset:" + bytesToInt(opcode[1], opcode[2]) + " Br.target: " + bytesToInt(opcode[3], opcode[4]);
            case JVM_LCONST_0: return "JVM_LCONST_0";
            case JVM_LCONST_1: return "JVM_LCONST_1";
            case JVM_LLOAD: return "JVM_LLOAD           " + opcode[1];
            case JVM_LLOAD_0: return "JVM_LLOAD_0";
            case JVM_LLOAD_1: return "JVM_LLOAD_1";
            case JVM_LLOAD_2: return "JVM_LLOAD_2";
            case JVM_LLOAD_3: return "JVM_LLOAD_3";
            case JVM_LLPUSH: return "JVM_LLPUSH     OPERANDS NOT AVAILABLE IN CURRENT TRACER";
            case JVM_LSTORE: return "JVM_LSTORE     " + opcode[1];
            case JVM_LSTORE_0: return "JVM_LSTORE_0";
            case JVM_LSTORE_1: return "JVM_LSTORE_1";
            case JVM_LSTORE_2: return "JVM_LSTORE_2";
            case JVM_LSTORE_3: return "JVM_LSTORE_3";
            case JVM_LALOAD: return "JVM_LALOAD";
            case JVM_LASTORE: return "JVM_LASTORE";
            case JVM_GETFIELD_L: return "JVM_GETFIELD_L";
            case JVM_PUTFIELD_L: return "JVM_PUTFIELD_L";
            case JVM_GETSTATIC_L: return "JVM_GETSTATIC_L";
            case JVM_PUTSTATIC_L: return "JVM_PUTSTATIC_L";
            case JVM_LADD: return "JVM_LADD";
            case JVM_LSUB: return "JVM_LSUB";
            case JVM_LMUL: return "JVM_LMUL";
            case JVM_LDIV: return "JVM_LDIV";
            case JVM_LREM: return "JVM_LREM";
            case JVM_LNEG: return "JVM_LNEG";
            case JVM_LSHL: return "JVM_LSHL";
            case JVM_LSHR: return "JVM_LSHR";
            case JVM_LUSHR: return "JVM_LUSHR";
            case JVM_LAND: return "JVM_LAND";
            case JVM_LOR: return "JVM_LOR";
            case JVM_LXOR: return "JVM_LXOR";
            case JVM_LRETURN: return "JVM_LRETURN";
            case JVM_L2I: return "JVM_L2I";
            case JVM_L2S: return "JVM_L2S";
            case JVM_I2L: return "JVM_I2L";
            case JVM_S2L: return "JVM_S2L";
            case JVM_LCMP: return "JVM_LCMP";
            case JVM_BRTARGET: return "JVM_BRTARGET     " + branchTargetCounter++;
            case JVM_MARKLOOP_START: return "JVM_MARKLOOP_START";
            case JVM_MARKLOOP_END: return "JVM_MARKLOOP_END";
            default: return "UNKNOWN OPCODE " + opcode;
        }
    }

    static private int getDataInt8(AtmelInterpreter a, int offset) {
        final int l = a.getDataByte(offset);
        return l;
    }
    static private int getDataInt16(AtmelInterpreter a, int offset) {
        final int l = a.getDataByte(offset);
        final int h = a.getDataByte(offset + 1);
        return ((h & 0xff) << 8) + (l & 0xff);
    }
    static private int getDataInt32(AtmelInterpreter a, int offset) {
        final int l = a.getDataByte(offset);
        final int h = a.getDataByte(offset + 1);
        final int h2 = a.getDataByte(offset + 2);
        final int h3 = a.getDataByte(offset + 3);
        return ((h3 & 0xff) << 24) + ((h2 & 0xff) << 16) + ((h & 0xff) << 8) + (l & 0xff);
    }
    static private int getProgramInt8(AtmelInterpreter a, int offset) {
        return a.getProgramByte(offset);
    }
    static private int getProgramInt16(AtmelInterpreter a, int offset) {
        final int l = a.getProgramByte(offset);
        final int h = a.getProgramByte(offset + 1);
        return ((h & 0xff) << 8) + (l & 0xff);
    }
    static private int getProgramInt32(AtmelInterpreter a, int offset) {
        final int l = a.getProgramByte(offset);
        final int h = a.getProgramByte(offset + 1);
        final int h2 = a.getProgramByte(offset + 2);
        final int h3 = a.getProgramByte(offset + 3);
        return ((h3 & 0xff) << 24) + ((h2 & 0xff) << 16) + ((h & 0xff) << 8) + (l & 0xff);
    }

    private class AvrInstruction {
        public int Address;
        public int Opcode;
        public String Text;
        public boolean IsBranchThroughBranchTable;
        public int BranchTarget;

        public AvrInstruction() {
            this.IsBranchThroughBranchTable = false;
            this.BranchTarget = -1;
        }
    }
    private class JavaInstruction {
        public String Text;
        public ArrayList<AvrInstruction> UnoptimisedAvr;
        public ArrayList<Short> StackCacheState;
        public ArrayList<Short> StackCacheValueTags;
        public Integer StackCachePinnedRegisters;
        public Integer StackCacheSkipInstructionReason;

        public JavaInstruction() {
            this.UnoptimisedAvr = new ArrayList<AvrInstruction>();
        }
    }
    private class MethodImpl {
        public String Infusion;
        public int MethodImplId;
        public int StartAddress;
        public int EndAddress;
        public int JvmMethodSize;
        public int BranchCount;
        public int MarkloopCount;
        public int MarkloopTotalSize;
        public ArrayList<JavaInstruction> JavaInstructions;
        public ArrayList<AvrInstruction> AvrInstructions;
        public ArrayList<Integer> BranchTargets;

        public MethodImpl() {
            this.JavaInstructions = new ArrayList<JavaInstruction>();
            this.AvrInstructions = new ArrayList<AvrInstruction>();
            this.BranchTargets = new ArrayList<Integer>();
            this.JavaInstructions.add(new JavaInstruction());
            this.JavaInstructions.get(0).Text = "Method preamble";
            this.BranchCount = 0;
            this.MarkloopCount = 0;
            this.MarkloopTotalSize = 0;
        }

        public JavaInstruction firstJavaInstruction() {
            return this.JavaInstructions.get(0);
        }
        public JavaInstruction lastJavaInstruction() {
            return this.JavaInstructions.get(this.JavaInstructions.size()-1);
        }
    }

    private void updateCounters(MethodImpl method, int[] opcode) {
        if (opcode[0] == JVM_MARKLOOP_START) {
            method.MarkloopCount++;
            method.MarkloopTotalSize += 2 + opcode[1]*2;
        }
        if (opcode[0] == JVM_MARKLOOP_END) {
            method.MarkloopCount++;
            method.MarkloopTotalSize++;
        }
        if (opcode[0] == JVM_IIFEQ || opcode[0] == JVM_IIFNE || opcode[0] == JVM_IIFLT || opcode[0] == JVM_IIFGE || opcode[0] == JVM_IIFGT || opcode[0] == JVM_IIFLE || opcode[0] == JVM_IFNULL || opcode[0] == JVM_IFNONNULL || opcode[0] == JVM_IF_SCMPEQ || opcode[0] == JVM_IF_SCMPNE || opcode[0] == JVM_IF_SCMPLT || opcode[0] == JVM_IF_SCMPGE || opcode[0] == JVM_IF_SCMPGT || opcode[0] == JVM_IF_SCMPLE || opcode[0] == JVM_IF_ICMPEQ || opcode[0] == JVM_IF_ICMPNE || opcode[0] == JVM_IF_ICMPLT || opcode[0] == JVM_IF_ICMPGE || opcode[0] == JVM_IF_ICMPGT || opcode[0] == JVM_IF_ICMPLE || opcode[0] == JVM_IF_ACMPEQ || opcode[0] == JVM_IF_ACMPNE || opcode[0] == JVM_GOTO || opcode[0] == JVM_GOTO_W || opcode[0] == JVM_SIFEQ || opcode[0] == JVM_SIFNE || opcode[0] == JVM_SIFLT || opcode[0] == JVM_SIFGE || opcode[0] == JVM_SIFGT || opcode[0] == JVM_SIFLE) {
            method.BranchCount++;
        }
        if (opcode[0] == JVM_TABLESWITCH || opcode[0] == JVM_LOOKUPSWITCH) {
            System.err.println("BRANCH COUNTER FOR SWITCH NOT IMPLEMENTED!!!!");
        }
    }

    private boolean initialised = false;
    private boolean printAllRuntimeAotCalls = false;
    private int branchTargetCounter;
    private boolean emittingPrologue = false; // The prologue is the last instruction generated, but the first instruction of the method. The compiler will signal RTCTrace when it starts to emit the prologue.
    private ArrayList<MethodImpl> methodImpls = new ArrayList<MethodImpl>();
    private boolean patchingBranches = false;
    private String currentInfusion = "not yet set";
    private Stack<String> callStack = new Stack<String>();

    public RTCTrace() {
        callStack.push("null");
    }

    /**
     * The <code>fireBeforeWrite()</code> method is called before the data address is written by the program.
     *
     * @param state     the state of the simulation
     * @param data_addr the address of the data being referenced
     * @param value     the value being written to the memory location
     */
    public void fireBeforeWrite(State state, int data_addr, byte value) {
        Simulator sim = state.getSimulator();
        AtmelInterpreter a = (AtmelInterpreter) sim.getInterpreter();
        MethodImpl currentMethod;

        int infusionNameAddress;
        int c;
        String caller, callee;

        // Check if we're turning the patchingBranches switch on or off
        switch (value) {
            case AVRORA_RTC_PATCHINGBRANCHES_ON: {
                this.patchingBranches = true;
                return;
            }
            case AVRORA_RTC_PATCHINGBRANCHES_OFF: {
                this.patchingBranches = false;
                return;
            }
        }

        if (!patchingBranches) {
            switch (value) {
                case AVRORA_RTC_STARTMETHOD: {
                    currentMethod = new MethodImpl();
                    currentMethod.Infusion = this.currentInfusion;
                    currentMethod.MethodImplId = (getDataInt8(a, data_addr+1) & 0xff);
                    currentMethod.StartAddress = getDataInt32(a, data_addr+2);
                    methodImpls.add(currentMethod);
                    branchTargetCounter = 0;
                    emittingPrologue = false;

                    String methodDefId = InfusionHeaderParser.getParser(currentMethod.Infusion).getMethodImpl_MethodDefId(currentMethod.MethodImplId);
                    String methodDefInfusion = InfusionHeaderParser.getParser(currentMethod.Infusion).getMethodImpl_MethodDefInfusion(currentMethod.MethodImplId);
                    String methodName = InfusionHeaderParser.getParser(methodDefInfusion).getMethodDef_name(methodDefId);
                    String methodSignature = InfusionHeaderParser.getParser(methodDefInfusion).getMethodDef_signature(methodDefId);
                    Terminal.print("[avrora.rtc] Start method " + currentMethod.MethodImplId + " " + this.currentInfusion + "." + methodName + " " + methodSignature + " at 0x" + Integer.toHexString(currentMethod.StartAddress) + ": ");
                }
                break;
                case AVRORA_RTC_JAVAOPCODE: {
                    int[] opcode = new int[5];
                    opcode[0] = (getDataInt8(a, data_addr+1) & 0xff);
                    opcode[1] = (getDataInt8(a, data_addr+2) & 0xff);
                    opcode[2] = (getDataInt8(a, data_addr+3) & 0xff);
                    opcode[3] = (getDataInt8(a, data_addr+4) & 0xff);
                    opcode[4] = (getDataInt8(a, data_addr+5) & 0xff);

                    JavaInstruction javaInstruction = new JavaInstruction();
                    javaInstruction.Text = opcode2string(opcode, this.currentInfusion);
                    currentMethod = methodImpls.get(methodImpls.size()-1);
                    updateCounters(currentMethod, opcode);
                    currentMethod.JavaInstructions.add(javaInstruction);
                }
                break;
                case AVRORA_RTC_SINGLEWORDINSTRUCTION: { // 1 word instruction at data_addr+1:data_addr+2
                    byte[] code = new byte[2];
                    code[0] = (byte)getDataInt8(a, data_addr+1);
                    code[1] = (byte)getDataInt8(a, data_addr+2);
                    AbstractInstr instr = disasm.disassemble(0, 0, code);

                    AvrInstruction avrInstruction = new AvrInstruction();
                    avrInstruction.Opcode = getDataInt16(a, data_addr+1);
                    avrInstruction.Text = instr.toString();
                    currentMethod = methodImpls.get(methodImpls.size()-1);
                    if (!emittingPrologue) {
                        currentMethod.lastJavaInstruction().UnoptimisedAvr.add(avrInstruction);
                    } else {
                        currentMethod.firstJavaInstruction().UnoptimisedAvr.add(avrInstruction);                        
                    }
                }
                break;
                case AVRORA_RTC_DOUBLEWORDINSTRUCTION: { // 2 word instruction at data_addr+1:data_addr+2
                    byte[] code = new byte[4];
                    code[0] = (byte)getDataInt8(a, data_addr+1);
                    code[1] = (byte)getDataInt8(a, data_addr+2);
                    code[2] = (byte)getDataInt8(a, data_addr+3);
                    code[3] = (byte)getDataInt8(a, data_addr+4);
                    AbstractInstr instr = disasm.disassemble(0, 0, code);

                    AvrInstruction avrInstruction = new AvrInstruction();
                    avrInstruction.Opcode = getDataInt32(a, data_addr+1);
                    avrInstruction.Text = instr.toString();
                    currentMethod = methodImpls.get(methodImpls.size()-1);
                    if (!emittingPrologue) {
                        currentMethod.lastJavaInstruction().UnoptimisedAvr.add(avrInstruction);
                    } else {
                        currentMethod.firstJavaInstruction().UnoptimisedAvr.add(avrInstruction);                        
                    }
                }
                break;
                case AVRORA_RTC_ENDMETHOD: {
                    currentMethod = methodImpls.get(methodImpls.size()-1);
                    currentMethod.EndAddress = getDataInt32(a, data_addr+1);
                    currentMethod.JvmMethodSize = getDataInt16(a, data_addr+5);
                    int numberOfBranchTargets = getDataInt8(a, data_addr+7);

                    if (currentMethod.StartAddress == 0) {
                        Terminal.print("[avrora.rtc] No function start address?? Did you forget to send the AVRORA_RTC_STARTMETHOD command?");
                    } else {
                        addFunctionDisassembly(state, currentMethod, numberOfBranchTargets);
                    }

                    Terminal.print(" ends at 0x" + Integer.toHexString(currentMethod.EndAddress) + ", AOT size: " + (currentMethod.EndAddress - currentMethod.StartAddress) + ", JVM size: " + currentMethod.JvmMethodSize + "\n\r");
                }
                break;
                case AVRORA_RTC_STACKCACHESTATE:
                    int stackcachestate_addr = ((int)getDataInt8(a, data_addr+1) & 0xFF) + (((int)getDataInt8(a, data_addr+2) & 0xFF) << 8);
                    ArrayList<Short> stackcachestate = new ArrayList<Short>();                  
                    for (int i=0; i<16; i++) {
                        stackcachestate.add((short)getDataInt8(a, stackcachestate_addr+i));
                    }
                    currentMethod = methodImpls.get(methodImpls.size()-1);
                    currentMethod.lastJavaInstruction().StackCacheState = stackcachestate;
                break;
                case AVRORA_RTC_STACKCACHEVALUETAGS:
                    int stackcachevaluetags_addr = ((int)getDataInt8(a, data_addr+1) & 0xFF) + (((int)getDataInt8(a, data_addr+2) & 0xFF) << 8);
                    ArrayList<Short> stackcachevaluetags = new ArrayList<Short>();                  
                    for (int i=0; i<16; i++) {
                        stackcachevaluetags.add((short)getDataInt16(a, stackcachevaluetags_addr+(2*i)));
                    }
                    currentMethod = methodImpls.get(methodImpls.size()-1);
                    currentMethod.lastJavaInstruction().StackCacheValueTags = stackcachevaluetags;
                break;
                case AVRORA_RTC_STACKCACHEPINNEDREGISTERS:
                    int pinnedregisters = ((int)getDataInt8(a, data_addr+1) & 0xFF) + (((int)getDataInt8(a, data_addr+2) & 0xFF) << 8);
                    currentMethod = methodImpls.get(methodImpls.size()-1);
                    currentMethod.lastJavaInstruction().StackCachePinnedRegisters = pinnedregisters;
                break;
                case AVRORA_RTC_STACKCACHESKIPINSTRUCTION:
                    int reason = (int)getDataInt8(a, data_addr+1);
                    currentMethod = methodImpls.get(methodImpls.size()-1);
                    currentMethod.lastJavaInstruction().StackCacheSkipInstructionReason = reason;
                break;
                case AVRORA_RTC_INIT:
                    if (this.initialised == true) {
                        // Init called twice: probably a crash
                        Terminal.printRed("[avrora.rtc] init called more than once??\nPROBABLY BECAUSE OF A CRASH: ABORTING\n");
                        System.exit(-1);
                    }
                    this.initialised = true;
                break;
                case AVRORA_RTC_SETCURRENTINFUSION:
                    this.currentInfusion = "";
                    infusionNameAddress = ((int)getDataInt8(a, data_addr+1) & 0xFF)
                                            + (((int)getDataInt8(a, data_addr+2) & 0xFF) << 8)
                                            + (((int)getDataInt8(a, data_addr+3) & 0xFF) << 16)
                                            + (((int)getDataInt8(a, data_addr+4) & 0xFF) << 24);
                    do {
                        c=getProgramInt8(a, infusionNameAddress++);
                        if (c != 0) {
                            this.currentInfusion += Character.toString((char)c);
                        }
                    } while (c != 0);
                    Terminal.print("\n\r####################################### RTC INFUSION " + this.currentInfusion + "\n\r\n\r");
                break;
                case AVRORA_RTC_RUNTIMEMETHODCALL:
                    String infusionName = "";
                    infusionNameAddress = ((int)getDataInt8(a, data_addr+1) & 0xFF)
                                            + (((int)getDataInt8(a, data_addr+2) & 0xFF) << 8)
                                            + (((int)getDataInt8(a, data_addr+3) & 0xFF) << 16)
                                            + (((int)getDataInt8(a, data_addr+4) & 0xFF) << 24);
                    do {
                        c=getProgramInt8(a, infusionNameAddress++);
                        if (c != 0) {
                            infusionName += Character.toString((char)c);
                        }
                    } while (c != 0);
                    int methodImplId = ((int)getDataInt8(a, data_addr+5) & 0xFF);
                    String methodDefId = InfusionHeaderParser.getParser(infusionName).getMethodImpl_MethodDefId(methodImplId);
                    String methodDefInfusion = InfusionHeaderParser.getParser(infusionName).getMethodImpl_MethodDefInfusion(methodImplId);
                    String methodName = InfusionHeaderParser.getParser(methodDefInfusion).getMethodDef_name(methodDefId);
                    String methodSignature = InfusionHeaderParser.getParser(methodDefInfusion).getMethodDef_signature(methodDefId);
                    caller = callStack.peek();
                    callee = infusionName + "." + methodName + " " + methodSignature;
                    if (this.printAllRuntimeAotCalls) {
                        Terminal.print("____" + Integer.toHexString(state.getSP()) + " " + callStack.size() + " RUNTIME CALL   " + caller + " -> " + callee + "   entity_id " + methodImplId + "\n\r\n\r");
                    }
                    callStack.push(callee);
                break;
                case AVRORA_RTC_RUNTIMEMETHODCALLRETURN:
                    callee = callStack.pop();
                    caller = callStack.peek();
                    if (this.printAllRuntimeAotCalls) {
                        Terminal.print("____" + Integer.toHexString(state.getSP()) + " " + callStack.size() + " RUNTIME RETURN " + caller + " <- " + callee + "\n\r\n\r");
                    }
                break;
                case AVRORA_RTC_PRINTALLRUNTIMEAOTCALLS:
                    this.printAllRuntimeAotCalls = true;
                break;
                case AVRORA_RTC_PRINTCURRENTAOTCALLSTACK:
                    printAOTCallStack(state);
                break;
                case AVRORA_RTC_BEEP:
                    int number = getDataInt8(a, data_addr+1);
                    Terminal.print("____" + Integer.toHexString(state.getSP()) + " " + callStack.size() + " BEEP BEEP " + number + "\n\r\n\r");
                break;
                case AVRORA_RTC_TERMINATEONEXCEPTION:
                    int exceptionType = getDataInt16(a, data_addr+1);
                    String exceptionName = "?";
                    switch (exceptionType) {
                        case  1 : exceptionName = "ARITHMETIC_EXCEPTION"; break;
                        case  2 : exceptionName = "ARRAYINDEXOUTOFBOUNDS_EXCEPTION"; break;
                        case  3 : exceptionName = "ARRAYSTORE_EXCEPTION"; break;
                        case  4 : exceptionName = "CLASSCAST_EXCEPTION"; break;
                        case  5 : exceptionName = "CLASSUNLOADED_EXCEPTION"; break;
                        case  6 : exceptionName = "ILLEGALARGUMENT_EXCEPTION"; break;
                        case  7 : exceptionName = "ILLEGALTHREADSTATE_EXCEPTION"; break;
                        case  8 : exceptionName = "INDEXOUTOFBOUNDS_EXCEPTION"; break;
                        case  9 : exceptionName = "INFUSIONUNLOADDEPENDENCY_EXCEPTION"; break;
                        case 10 : exceptionName = "NATIVEMETHODNOTIMPLEMENTED_ERROR"; break;
                        case 11 : exceptionName = "NULLPOINTER_EXCEPTION"; break;
                        case 12 : exceptionName = "OUTOFMEMORY_ERROR"; break;
                        case 13 : exceptionName = "RUNTIME_EXCEPTION"; break;
                        case 14 : exceptionName = "STACKOVERFLOW_ERROR"; break;
                        case 15 : exceptionName = "STRINGINDEXOUTOFBOUNDS_EXCEPTION"; break;
                        case 16 : exceptionName = "VIRTUALMACHINE_ERROR"; break;
                    }
                    Terminal.print("\n\rKAPOT KAPOT KAPOT KAPOT " + exceptionType + " " + exceptionName + "\n\r\n\r");
                    printAOTCallStack(state);
                    System.exit(exceptionType);
                break;
                case AVRORA_RTC_EMITPROLOGUE:
                    emittingPrologue = true;
                break;
                default:
                    Terminal.print("[avrora.rtc] unknown command " + value + "\n\r");
                break;
            }
        }
    }

    private void printAOTCallStack(State state) {
        Terminal.print("____" + Integer.toHexString(state.getSP()) + " CURRENT AOT CALL STACK:\n");
        Terminal.print(" 1 " + callStack.get(0) + "\n");
        for (int i=1; i<callStack.size(); i++) {
            Terminal.print(" " + (i+2) + " -> " + callStack.get(i) + "\n");
        }
        Terminal.print("____ END OF AOT CALL STACK.\n");
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        for (MethodImpl method : methodImpls) {
            buf.append("[avrora.rtc] --------------- NEW METHOD WITH IMPL_ID " + method.MethodImplId + " STARTS AT 0x" + Integer.toHexString(method.StartAddress) + "\n\r");
            for (JavaInstruction i : method.JavaInstructions) {
                buf.append("[avrora.rtc] JAVA " + i.Text + "\n\r");
                for (AvrInstruction j : i.UnoptimisedAvr) {
                    buf.append("[avrora.rtc] AVR                " + j.Text + "\n\r");
                }
            }
            buf.append("[avrora.rtc] METHOD ENDS AT 0x" + Integer.toHexString(method.EndAddress) + "\n\r");
            buf.append("[avrora.rtc]                NUMBER OF BRTARGETS: " + method.BranchTargets.size() + "\n\r");
            for (int i=0; i<method.BranchTargets.size(); i++) {
                int address = method.StartAddress + 2*i;
                int branchTargetAddress = method.BranchTargets.get(i);
                buf.append("[avrora.rtc] AVR 0x" + Integer.toHexString(address) + "   BRTARGET " + i + " at 0x" + Integer.toHexString(branchTargetAddress) + "\n\r");
            }

            for (AvrInstruction avrInstruction : method.AvrInstructions) {
                String branchTargetString;
                int branchTarget = method.BranchTargets.indexOf(avrInstruction.Address);
                if (branchTarget == -1) { // This is the branch target itself
                    branchTargetString = "       ";
                } else {
                    // this is a branch target
                    branchTargetString = " (BT:" + branchTarget + ")";
                }
                buf.append("[avrora.rtc] AVR 0x" + Integer.toHexString(avrInstruction.Address) + branchTargetString + "       " + avrInstruction.Text);


                if (avrInstruction.IsBranchThroughBranchTable) {
                    branchTarget = avrInstruction.BranchTarget; // This is a branch TO a branch target
                    buf.append("   (BT:" + branchTarget + " @ " + Integer.toHexString(method.BranchTargets.get(branchTarget)) + ")");
                }

                buf.append("\n\r");         
            }
            buf.append("[avrora.rtc] --------------- METHOD END byte code size: " + method.JvmMethodSize + ", compiled size: " + (method.EndAddress-method.StartAddress) + "\n\r");
        }

        return buf.toString();
    }

    private String AvrInstruction2PyString(AvrInstruction avrInstruction) {
        return String.format("AvrInstruction(address = %8s, opcode = %10s, isBranchThroughBranchTable = %5s, branchTarget = %3d, jvm = None, text = '%s')",
            "0x" + Integer.toHexString(avrInstruction.Address),
            "0x" + Integer.toHexString(avrInstruction.Opcode),
            avrInstruction.IsBranchThroughBranchTable ? "True" : "False",
            avrInstruction.BranchTarget,
            avrInstruction.Text);
    }
    public String toPythonString() {
        StringBuffer buf = new StringBuffer();
        buf.append("from collections import namedtuple\n\r");
        buf.append("MethodImpl = namedtuple(\"MethodImpl\", \"methodImplId startAddress endAddress jvmMethodSize avrMethodSize javaInstructions branchTargets avrInstructions\")\n\r");
        buf.append("JavaInstruction = namedtuple(\"JavaInstruction\", \"index text unoptimisedAvr optimisedAvr\")\n\r");
        buf.append("AvrInstruction = namedtuple(\"AvrInstruction\", \"address opcode isBranchThroughBranchTable branchTarget jvm text\")\n\r");
        buf.append("\n\r");
        buf.append("methods=[\n\r");

        for (MethodImpl method : methodImpls) {
            buf.append("    MethodImpl(\n\r");
            buf.append("        methodImplId = " + method.MethodImplId + ",\n\r");
            buf.append("        startAddress = 0x" + Integer.toHexString(method.StartAddress) + ",\n\r");
            buf.append("        endAddress = 0x" + Integer.toHexString(method.EndAddress) + ",\n\r");
            buf.append("        jvmMethodSize = " + method.JvmMethodSize + ",\n\r");
            buf.append("        avrMethodSize = " + (method.EndAddress-method.StartAddress) + ",\n\r");         
            buf.append("        javaInstructions = [\n\r");
            int instructionIndex = 0;
            for (JavaInstruction javaInstruction : method.JavaInstructions) {
                buf.append("            JavaInstruction(\n\r");
                buf.append("                index = '" + instructionIndex++ + "',\n\r");
                buf.append("                text = '" + javaInstruction.Text + "',\n\r");
                buf.append("                unoptimisedAvr = [\n\r");
                for (AvrInstruction avrInstruction : javaInstruction.UnoptimisedAvr) {
                    buf.append("                    " + AvrInstruction2PyString(avrInstruction) + ",\n\r");
                }
                buf.append("                ],\n\r");
                buf.append("                optimisedAvr = []\n\r");
                buf.append("            ),\n\r");
            }
            buf.append("        ],\n\r");
            buf.append("        branchTargets = [\n\r");
            for (int i=0; i<method.BranchTargets.size(); i++) {
                buf.append("            0x" + Integer.toHexString(method.BranchTargets.get(i)) + ",\n\r");
            }
            buf.append("        ],\n\r");
            buf.append("        avrInstructions = [\n\r");
            for (AvrInstruction avrInstruction : method.AvrInstructions) {
                buf.append("                " + AvrInstruction2PyString(avrInstruction) + ",\n\r");
            }
            buf.append("        ]\n\r");
            buf.append("    ),\n\r");
        }
        buf.append("]\n\r");
        return buf.toString();
    }


    private String StackCacheState2String(short stackCacheState) {
        switch(stackCacheState & 0xFF) {
            case 0xFF:
                return "";
            case 0xFE:
                return "USED";
            case 0xFD:
                return "---------";
            default:
                if ((stackCacheState & 0x10) == 0x10) {
                    return "REF " + (stackCacheState & 0x0F);
                } else {
                    return "INT " + (stackCacheState & 0x0F);                   
                }
        }
    }
    private String StackCacheValueTag2String(short stackCacheValueTag) {
        // 9 char
        String typeString     ="  ";
        String datatypeString ="  ";

// #define RTC_VALUETAG_TYPE_LOCAL     0x0000
// #define RTC_VALUETAG_TYPE_STATIC    0x4000
// #define RTC_VALUETAG_TYPE_CONSTANT  0x8000
// #define RTC_VALUETAG_UNUSED         0xFFFF
// #define RTC_VALUETAG_DATATYPE_REF   0x0000
// #define RTC_VALUETAG_DATATYPE_SHORT 0x1000
// #define RTC_VALUETAG_DATATYPE_INT   0x2000
// #define RTC_VALUETAG_DATATYPE_INT_H 0x3000

        if ((stackCacheValueTag&0xFFFF) == 0xFFFF)
            return "";

        int type = (stackCacheValueTag >> 14) & 0x3;
        switch(type) {
            case 0:
                typeString = "L"; break;
            case 1:
                typeString = "S"; break;
            case 2:
                typeString = "C"; break;
            default:
                typeString = String.format("%1d", type);
        }
        int datatype = (stackCacheValueTag >> 12) & 0x3;
        switch(datatype) {
            case 0:
                datatypeString = "R"; break;
            case 1:
                datatypeString = "S"; break;
            case 2:
                datatypeString = "IH"; break;
            case 3:
                datatypeString = "IL"; break;
            default:
                datatypeString = String.format("%2d", datatype);
        }

        return String.format("%1s %2s%5d", typeString, datatypeString, stackCacheValueTag & 0x0FFF);
    }
    private String AvrInstruction2XmlString(AvrInstruction avrInstruction) {
        return String.format("<avrInstruction address=\"%8s\" opcode=\"%10s\" isBranchThroughBranchTable=\"%5s\" branchTarget=\"%3d\" text=\"%s\" />",
            "0x" + Integer.toHexString(avrInstruction.Address),
            "0x" + Integer.toHexString(avrInstruction.Opcode),
            avrInstruction.IsBranchThroughBranchTable ? "True" : "False",
            avrInstruction.BranchTarget,
            avrInstruction.Text);
    }
    private String urlencode(String s) {
        try {
            return URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8.toString());
        } catch (Exception ex) {
            System.err.println("Couldn't urlencode string \"" + s + "\".");
            System.err.println(ex.toString());
            System.exit(1);
            return ""; // Really javac?
        }
    }
    public String toXmlString() {
        StringBuffer buf = new StringBuffer();
        buf.append("<methods>");

        for (MethodImpl method : methodImpls) {
            String methodDefId = InfusionHeaderParser.getParser(method.Infusion).getMethodImpl_MethodDefId(method.MethodImplId);
            String methodDefInfusion = InfusionHeaderParser.getParser(method.Infusion).getMethodImpl_MethodDefInfusion(method.MethodImplId);
            String methodName = InfusionHeaderParser.getParser(methodDefInfusion).getMethodDef_name(methodDefId);
            String methodSignature = InfusionHeaderParser.getParser(methodDefInfusion).getMethodDef_signature(methodDefId);
            buf.append("    <methodImpl\n\r");
            buf.append("            jvmMethodSize=\"" + method.JvmMethodSize + "\"");
            buf.append("            avrMethodSize=\"" + (method.EndAddress-method.StartAddress) + "\"");
            buf.append("            method=\"" + urlencode(method.Infusion) + "." + urlencode(methodName) + " " + methodSignature + "\"\n\r");
            buf.append("            methodImplId=\"" + method.MethodImplId + "\"\n\r");
            buf.append("            methodDefId=\"" + methodDefId + "\"\n\r");
            buf.append("            methodDefInfusion=\"" + methodDefInfusion + "\"\n\r");
            buf.append("            methodSignature=\"" + methodSignature + "\"\n\r");
            buf.append("            startAddress=\"0x" + Integer.toHexString(method.StartAddress) + "\"\n\r");
            buf.append("            endAddress=\"0x" + Integer.toHexString(method.EndAddress) + "\"\n\r");
            buf.append("            branchCount=\"" + (method.BranchCount) + "\"\n\r");
            buf.append("            markloopCount=\"" + (method.MarkloopCount) + "\"\n\r");
            buf.append("            markloopTotalSize=\"" + (method.MarkloopTotalSize) + "\" >\n\r");

            buf.append("        <javaInstructions>\n\r");
            int instructionIndex = 0;
            for (JavaInstruction javaInstruction : method.JavaInstructions) {
                buf.append("            <javaInstruction index=\"" + instructionIndex++ + "\" text=\"" + javaInstruction.Text + "\">\n\r");
                buf.append("                <unoptimisedAvr>\n\r");
                for (AvrInstruction avrInstruction : javaInstruction.UnoptimisedAvr) {
                    buf.append("                    " + AvrInstruction2XmlString(avrInstruction) + "\n\r");
                }
                buf.append("                </unoptimisedAvr>\n\r");
                buf.append("                <stackCacheState>\n\r");
                if (javaInstruction.StackCachePinnedRegisters != null) {
                    for (int i = 0; i<16; i++) {
                        if ((javaInstruction.StackCachePinnedRegisters & (1 << i)) != 0) {
                            buf.append("| PINNED    ");
                        } else {
                            buf.append("|           ");
                        }
                    }                   
                    buf.append("|\n\r");
                }
                for (int i = 0; i<16; i++) {
                    buf.append(String.format("| R%2d       ", i*2));
                }
                buf.append("|\n\r");
                if (javaInstruction.StackCacheState != null) {
                    int i = 0;
                    for (Short stackCacheState : javaInstruction.StackCacheState) {
                        buf.append(String.format("| %-10s", StackCacheState2String(stackCacheState)));
                        i += 2;
                    }                   
                    buf.append("|\n\r");
                }
                if (javaInstruction.StackCacheValueTags != null) {
                    int i = 0;
                    for (Short stackCacheValueTag : javaInstruction.StackCacheValueTags) {
                        buf.append(String.format("| %9s ", StackCacheValueTag2String(stackCacheValueTag)));
                        i += 2;
                    }                   
                    buf.append("|\n\r");
                }
                if (javaInstruction.StackCacheSkipInstructionReason != null) {
                    buf.append("                SKIP REASON: " + javaInstruction.StackCacheSkipInstructionReason + "\n\r");                 
                }
                buf.append("                </stackCacheState>\n\r");
                buf.append("            </javaInstruction>\n\r");
            }
            buf.append("        </javaInstructions>\n\r");
            buf.append("        <branchTargets>\n\r");
            for (int i=0; i<method.BranchTargets.size(); i++) {
                buf.append("            <branchTarget target=\"0x" + Integer.toHexString(method.BranchTargets.get(i)) + "\" />\n\r");
            }
            buf.append("        </branchTargets>\n\r");
            buf.append("        <avrInstructions>\n\r");
            for (AvrInstruction avrInstruction : method.AvrInstructions) {
                buf.append("                " + AvrInstruction2XmlString(avrInstruction) + ",\n\r");
            }
            buf.append("        </avrInstructions>\n\r");
            buf.append("    </methodImpl>\n\r");
        }
        buf.append("</methods>");
        return buf.toString();
    }


    private void addFunctionDisassembly(State state, MethodImpl method, int numberOfBranchTargets) {
        Simulator sim = state.getSimulator();
        AtmelInterpreter a = (AtmelInterpreter) sim.getInterpreter();

        // Scan branch target addresses
        int address;
        if (true) { // NOP optimisation? Then there will be two branchtables, one before, and one after removing the NOPs. The second will contain the right addresses.
            address = method.StartAddress + numberOfBranchTargets*2;
            int i = 0;
            while (address < method.StartAddress+numberOfBranchTargets*4) {
                int wordOffsetFromFunctionStart = getProgramInt16(a, address);
                int branchTargetAddress = method.StartAddress + 2*wordOffsetFromFunctionStart;
                method.BranchTargets.add(branchTargetAddress);
                address += 2;
            }
        } else {
            address = method.StartAddress;
            int i = 0;
            while (address < method.StartAddress+numberOfBranchTargets*2) {
                int wordOffsetFromFunctionStart = getProgramInt16(a, address);
                int branchTargetAddress = method.StartAddress + 2*wordOffsetFromFunctionStart;
                method.BranchTargets.add(branchTargetAddress);
                address += 2;
            }
        }

        // Print compiled method, and add branch target annotations where necessary.
        while (address < method.EndAddress) {
            AbstractInstr instr = state.getInstr(address);

            if (instr==null) {
                Terminal.print("Can't find instruction at address 0x" + Integer.toHexString(address) + ". (contains " + getProgramInt16(a, address) + ").\n");
                System.exit(1);
            }

            AvrInstruction avrInstruction = new AvrInstruction();
            avrInstruction.Address = address;
            if (instr.getSize()==2) {
                avrInstruction.Opcode = getProgramInt16(a, address);
            } else if (instr.getSize()==4) {
                avrInstruction.Opcode = getProgramInt32(a, address);
            } else {
                Terminal.print("INSTRUCTION LENGTH SHOULD BE 2 or 4!!!\n");
            }
            avrInstruction.Text = instr.toString();

            if (instr instanceof LegacyInstr.RJMP) {
                // Resolve branch target
                int operand = ((LegacyInstr.RJMP)instr).imm1;
                Integer targetAddress = address + 2*(1+operand);
                int branchTarget = method.BranchTargets.indexOf(targetAddress);
                if (branchTarget >= 0 && branchTarget < method.BranchTargets.size()) { // local branches, such as in sshr don't jump to the branch table
                    avrInstruction.IsBranchThroughBranchTable = true;
                    avrInstruction.BranchTarget = branchTarget;
                }
            }

            method.AvrInstructions.add(avrInstruction);
            address += instr.getSize();
        }
    }
}
