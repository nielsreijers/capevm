package avrora.sim.util;

import avrora.sim.*;
import avrora.arch.AbstractInstr;
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
	final static int AVRORA_RTC_SINGLEWORDINSTRUCTION = 1;
	final static int AVRORA_RTC_DOUBLEWORDINSTRUCTION = 2;
	final static int AVRORA_RTC_STARTMETHOD           = 3;
	final static int AVRORA_RTC_ENDMETHOD             = 4;
	final static int AVRORA_RTC_JAVAOPCODE            = 5;

	final static LegacyDisassembler disasm = new LegacyDisassembler();

	int functionStartAddress = 0;

	static private String opcode2string(int opcode) {
		switch (opcode) {
			case 0: return "JVM_NOP";
			case 1: return "JVM_SCONST_M1";
			case 2: return "JVM_SCONST_0";
			case 3: return "JVM_SCONST_1";
			case 4: return "JVM_SCONST_2";
			case 5: return "JVM_SCONST_3";
			case 6: return "JVM_SCONST_4";
			case 7: return "JVM_SCONST_5";
			case 8: return "JVM_ICONST_M1";
			case 9: return "JVM_ICONST_0";
			case 10: return "JVM_ICONST_1";
			case 11: return "JVM_ICONST_2";
			case 12: return "JVM_ICONST_3";
			case 13: return "JVM_ICONST_4";
			case 14: return "JVM_ICONST_5";
			case 15: return "JVM_ACONST_NULL";
			case 16: return "JVM_BSPUSH";
			case 17: return "JVM_BIPUSH";
			case 18: return "JVM_SSPUSH";
			case 19: return "JVM_SIPUSH";
			case 20: return "JVM_IIPUSH";
			case 21: return "JVM_LDS";
			case 22: return "JVM_SLOAD";
			case 23: return "JVM_SLOAD_0";
			case 24: return "JVM_SLOAD_1";
			case 25: return "JVM_SLOAD_2";
			case 26: return "JVM_SLOAD_3";
			case 27: return "JVM_ILOAD";
			case 28: return "JVM_ILOAD_0";
			case 29: return "JVM_ILOAD_1";
			case 30: return "JVM_ILOAD_2";
			case 31: return "JVM_ILOAD_3";
			case 32: return "JVM_ALOAD";
			case 33: return "JVM_ALOAD_0";
			case 34: return "JVM_ALOAD_1";
			case 35: return "JVM_ALOAD_2";
			case 36: return "JVM_ALOAD_3";
			case 37: return "JVM_SSTORE";
			case 38: return "JVM_SSTORE_0";
			case 39: return "JVM_SSTORE_1";
			case 40: return "JVM_SSTORE_2";
			case 41: return "JVM_SSTORE_3";
			case 42: return "JVM_ISTORE";
			case 43: return "JVM_ISTORE_0";
			case 44: return "JVM_ISTORE_1";
			case 45: return "JVM_ISTORE_2";
			case 46: return "JVM_ISTORE_3";
			case 47: return "JVM_ASTORE";
			case 48: return "JVM_ASTORE_0";
			case 49: return "JVM_ASTORE_1";
			case 50: return "JVM_ASTORE_2";
			case 51: return "JVM_ASTORE_3";
			case 52: return "JVM_BALOAD";
			case 53: return "JVM_CALOAD";
			case 54: return "JVM_SALOAD";
			case 55: return "JVM_IALOAD";
			case 56: return "JVM_AALOAD";
			case 57: return "JVM_BASTORE";
			case 58: return "JVM_CASTORE";
			case 59: return "JVM_SASTORE";
			case 60: return "JVM_IASTORE";
			case 61: return "JVM_AASTORE";
			case 62: return "JVM_IPOP";
			case 63: return "JVM_IPOP2";
			case 64: return "JVM_IDUP";
			case 65: return "JVM_IDUP2";
			case 66: return "JVM_IDUP_X1";
			case 67: return "JVM_ISWAP_X";
			case 68: return "JVM_APOP";
			case 69: return "JVM_APOP2";
			case 70: return "JVM_ADUP";
			case 71: return "JVM_ADUP2";
			case 72: return "JVM_ADUP_X1";
			case 73: return "JVM_ADUP_X2";
			case 74: return "JVM_ASWAP";
			case 75: return "JVM_GETFIELD_B";
			case 76: return "JVM_GETFIELD_C";
			case 77: return "JVM_GETFIELD_S";
			case 78: return "JVM_GETFIELD_I";
			case 79: return "JVM_GETFIELD_A";
			case 80: return "JVM_PUTFIELD_B";
			case 81: return "JVM_PUTFIELD_C";
			case 82: return "JVM_PUTFIELD_S";
			case 83: return "JVM_PUTFIELD_I";
			case 84: return "JVM_PUTFIELD_A";
			case 85: return "JVM_GETSTATIC_B";
			case 86: return "JVM_GETSTATIC_C";
			case 87: return "JVM_GETSTATIC_S";
			case 88: return "JVM_GETSTATIC_I";
			case 89: return "JVM_GETSTATIC_A";
			case 90: return "JVM_PUTSTATIC_B";
			case 91: return "JVM_PUTSTATIC_C";
			case 92: return "JVM_PUTSTATIC_S";
			case 93: return "JVM_PUTSTATIC_I";
			case 94: return "JVM_PUTSTATIC_A";
			case 95: return "JVM_SADD";
			case 96: return "JVM_SSUB";
			case 97: return "JVM_SMUL";
			case 98: return "JVM_SDIV";
			case 99: return "JVM_SREM";
			case 100: return "JVM_SNEG";
			case 101: return "JVM_SSHL";
			case 102: return "JVM_SSHR";
			case 103: return "JVM_SUSHR";
			case 104: return "JVM_SAND";
			case 105: return "JVM_SOR";
			case 106: return "JVM_SXOR";
			case 107: return "JVM_IADD";
			case 108: return "JVM_ISUB";
			case 109: return "JVM_IMUL";
			case 110: return "JVM_IDIV";
			case 111: return "JVM_IREM";
			case 112: return "JVM_INEG";
			case 113: return "JVM_ISHL";
			case 114: return "JVM_ISHR";
			case 115: return "JVM_IUSHR";
			case 116: return "JVM_IAND";
			case 117: return "JVM_IOR";
			case 118: return "JVM_IXOR";
			case 119: return "JVM_BINC";
			case 120: return "JVM_SINC";
			case 121: return "JVM_IINC";
			case 122: return "JVM_S2B";
			case 123: return "JVM_S2I";
			case 124: return "JVM_I2B";
			case 125: return "JVM_I2S";
			case 126: return "JVM_IIFEQ";
			case 127: return "JVM_IIFNE";
			case 128: return "JVM_IIFLT";
			case 129: return "JVM_IIFGE";
			case 130: return "JVM_IIFGT";
			case 131: return "JVM_IIFLE";
			case 132: return "JVM_IFNULL";
			case 133: return "JVM_IFNONNULL";
			case 134: return "JVM_IF_SCMPEQ";
			case 135: return "JVM_IF_SCMPNE";
			case 136: return "JVM_IF_SCMPLT";
			case 137: return "JVM_IF_SCMPGE";
			case 138: return "JVM_IF_SCMPGT";
			case 139: return "JVM_IF_SCMPLE";
			case 140: return "JVM_IF_ICMPEQ";
			case 141: return "JVM_IF_ICMPNE";
			case 142: return "JVM_IF_ICMPLT";
			case 143: return "JVM_IF_ICMPGE";
			case 144: return "JVM_IF_ICMPGT";
			case 145: return "JVM_IF_ICMPLE";
			case 146: return "JVM_IF_ACMPEQ";
			case 147: return "JVM_IF_ACMPNE";
			case 148: return "JVM_GOTO";
			case 149: return "JVM_GOTO_W";
			case 150: return "JVM_TABLESWITCH";
			case 151: return "JVM_LOOKUPSWITCH";
			case 152: return "JVM_SRETURN";
			case 153: return "JVM_IRETURN";
			case 154: return "JVM_ARETURN";
			case 155: return "JVM_RETURN";
			case 156: return "JVM_INVOKEVIRTUAL";
			case 157: return "JVM_INVOKESPECIAL";
			case 158: return "JVM_INVOKESTATIC";
			case 159: return "JVM_INVOKEINTERFACE";
			case 160: return "JVM_NEW";
			case 161: return "JVM_NEWARRAY";
			case 162: return "JVM_ANEWARRAY";
			case 163: return "JVM_ARRAYLENGTH";
			case 164: return "JVM_ATHROW";
			case 165: return "JVM_CHECKCAST";
			case 166: return "JVM_INSTANCEOF";
			case 167: return "JVM_MONITORENTER";
			case 168: return "JVM_MONITOREXIT";
			case 169: return "JVM_IDUP_X2";
			case 170: return "JVM_IINC_W";
			case 171: return "JVM_SINC_W";
			case 172: return "JVM_I2C";
			case 173: return "JVM_S2C";
			case 174: return "JVM_B2C";
			case 175: return "JVM_IDUP_X";
			case 176: return "JVM_SIFEQ";
			case 177: return "JVM_SIFNE";
			case 178: return "JVM_SIFLT";
			case 179: return "JVM_SIFGE";
			case 180: return "JVM_SIFGT";
			case 181: return "JVM_SIFLE";
			case 182: return "JVM_LCONST_0";
			case 183: return "JVM_LCONST_1";
			case 184: return "JVM_LLOAD";
			case 185: return "JVM_LLOAD_0";
			case 186: return "JVM_LLOAD_1";
			case 187: return "JVM_LLOAD_2";
			case 188: return "JVM_LLOAD_3";
			case 189: return "JVM_LLPUSH";
			case 190: return "JVM_LSTORE";
			case 191: return "JVM_LSTORE_0";
			case 192: return "JVM_LSTORE_1";
			case 193: return "JVM_LSTORE_2";
			case 194: return "JVM_LSTORE_3";
			case 195: return "JVM_LALOAD";
			case 196: return "JVM_LASTORE";
			case 197: return "JVM_GETFIELD_L";
			case 198: return "JVM_PUTFIELD_L";
			case 199: return "JVM_GETSTATIC_L";
			case 200: return "JVM_PUTSTATIC_L";
			case 201: return "JVM_LADD";
			case 202: return "JVM_LSUB";
			case 203: return "JVM_LMUL";
			case 204: return "JVM_LDIV";
			case 205: return "JVM_LREM";
			case 206: return "JVM_LNEG";
			case 207: return "JVM_LSHL";
			case 208: return "JVM_LSHR";
			case 209: return "JVM_LUSHR";
			case 210: return "JVM_LAND";
			case 211: return "JVM_LOR";
			case 212: return "JVM_LXOR";
			case 213: return "JVM_LRETURN";
			case 214: return "JVM_L2I";
			case 215: return "JVM_L2S";
			case 216: return "JVM_I2L";
			case 217: return "JVM_S2L";
			case 218: return "JVM_LCMP";
			case 219: return "JVM_BRTARGET";
			default: return "UNKNOWN OPCODE " + opcode;
		}
	}

    static private int getInt8(AtmelInterpreter a, int offset) {
        final int l = a.getDataByte(offset);
        return l;
    }
    static private int getInt16(AtmelInterpreter a, int offset) {
        final int l = a.getDataByte(offset);
        final int h = a.getDataByte(offset + 1);
        return ((h & 0xff) << 8) + (l & 0xff);
    }
    static private int getInt32(AtmelInterpreter a, int offset) {
        final int l = a.getDataByte(offset);
        final int h = a.getDataByte(offset + 1);
        final int h2 = a.getDataByte(offset + 2);
        final int h3 = a.getDataByte(offset + 3);
        return ((h3 & 0xff) << 24) + ((h2 & 0xff) << 16) + ((h & 0xff) << 8) + (l & 0xff);
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
        StringBuffer buf = new StringBuffer();

		switch (value) {
			case AVRORA_RTC_SINGLEWORDINSTRUCTION: { // 1 word instruction at data_addr+1:data_addr+2
				byte[] code = new byte[2];
				code[0] = (byte)getInt8(a, data_addr+1);
				code[1] = (byte)getInt8(a, data_addr+2);
				AbstractInstr instr = disasm.disassemble(0, 0, code);
				buf.append("RTCTrace: (AVR)                " + instr + "\n\r");
			}
			break;
			case AVRORA_RTC_DOUBLEWORDINSTRUCTION: { // 2 word instruction at data_addr+1:data_addr+2
				byte[] code = new byte[4];
				code[0] = (byte)getInt8(a, data_addr+1);
				code[1] = (byte)getInt8(a, data_addr+2);
				code[2] = (byte)getInt8(a, data_addr+3);
				code[3] = (byte)getInt8(a, data_addr+4);
				AbstractInstr instr = disasm.disassemble(0, 0, code);
				buf.append("RTCTrace: (AVR)                " + instr + "\n\r");
			}
			break;
			case AVRORA_RTC_STARTMETHOD: {
				int method_impl_id = (getInt8(a, data_addr+1) & 0xff);
				functionStartAddress = getInt32(a, data_addr+2);
				buf.append("\n\r\n\rRTCTrace: NEW METHOD WITH IMPL_ID " + method_impl_id + " STARTS AT 0x" + Integer.toHexString(functionStartAddress) + "\n\r");
			}
			break;
			case AVRORA_RTC_ENDMETHOD: {
				int functionEndAddress = getInt32(a, data_addr+1);
				buf.append("RTCTrace: METHOD ENDS AT 0x" + Integer.toHexString(functionEndAddress) + "\n\r");
				if (functionStartAddress == 0)
					buf.append("RTCTrace: No function start address?? Did you forget to send the AVRORA_RTC_STARTMETHOD command?");
				else
					addFunctionDisassembly(state, functionStartAddress, functionEndAddress, buf);
				functionStartAddress = 0;
				buf.append("RTCTrace: METHOD END ---------------\n\r");
			}
			break;
			case AVRORA_RTC_JAVAOPCODE: {
				int opcode = (getInt8(a, data_addr+1) & 0xff);
				buf.append("RTCTrace: (JAVA) " + opcode2string(opcode) + "\n\r");
			}
			break;
			default:
				buf.append("RTCTrace: unknown command " + value);
			break;
		}

		synchronized (Terminal.class) {
			Terminal.print(buf.toString());
		}
    }

    private void addFunctionDisassembly(State state, int functionStartAddress, int functionEndAddress, StringBuffer buf) {
    	int address = functionStartAddress;
		while (address < functionEndAddress) {
			AbstractInstr instr = state.getInstr(address);
			buf.append("RTCTrace: (AVR) 0x" + Integer.toHexString(address) + "               " + instr + "\n\r");
			address += instr.getSize();
    	}
    }
}
