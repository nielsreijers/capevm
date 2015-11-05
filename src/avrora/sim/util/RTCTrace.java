package avrora.sim.util;

import java.util.ArrayList; 
import avrora.sim.*;
import avrora.arch.AbstractInstr;
import avrora.arch.legacy.LegacyInstr;
import avrora.arch.legacy.LegacyDisassembler;
import cck.text.Terminal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

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

	private boolean logToFile;
	private Path logFilepath;
	public RTCTrace(String filename) {
		logToFile = (filename != "");
		if (logToFile) {
			logFilepath = Paths.get(filename);
		}
	}

	private static int bytesToInt(int hi, int lo) { return (short)( ((hi&0xFF)<<8) | (lo&0xFF) ); }
	private static int bytesToInt(int hi3, int hi2, int hi1, int lo) { return ( ((hi3&0xFF)<<24) | ((hi2&0xFF)<<16) | ((hi1&0xFF)<<8) | (lo&0xFF) ); }
	private String opcode2string(int[] opcode) {
		switch (opcode[0]) {
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
			case 16: return "JVM_BSPUSH			" + opcode[1];
			case 17: return "JVM_BIPUSH			" + opcode[1];
			case 18: return "JVM_SSPUSH			" + bytesToInt(opcode[1], opcode[2]);
			case 19: return "JVM_SIPUSH			" + bytesToInt(opcode[1], opcode[2]);
			case 20: return "JVM_IIPUSH			" + bytesToInt(opcode[1], opcode[2], opcode[3], opcode[4]);
			case 21: return "JVM_LDS			Infusion:" + opcode[1] + " String:" + opcode[2];
			case 22: return "JVM_SLOAD			" + opcode[1];
			case 23: return "JVM_SLOAD_0";
			case 24: return "JVM_SLOAD_1";
			case 25: return "JVM_SLOAD_2";
			case 26: return "JVM_SLOAD_3";
			case 27: return "JVM_ILOAD			" + opcode[1];
			case 28: return "JVM_ILOAD_0";
			case 29: return "JVM_ILOAD_1";
			case 30: return "JVM_ILOAD_2";
			case 31: return "JVM_ILOAD_3";
			case 32: return "JVM_ALOAD			" + opcode[1];
			case 33: return "JVM_ALOAD_0";
			case 34: return "JVM_ALOAD_1";
			case 35: return "JVM_ALOAD_2";
			case 36: return "JVM_ALOAD_3";
			case 37: return "JVM_SSTORE			" + opcode[1];
			case 38: return "JVM_SSTORE_0";
			case 39: return "JVM_SSTORE_1";
			case 40: return "JVM_SSTORE_2";
			case 41: return "JVM_SSTORE_3";
			case 42: return "JVM_ISTORE			" + opcode[1];
			case 43: return "JVM_ISTORE_0";
			case 44: return "JVM_ISTORE_1";
			case 45: return "JVM_ISTORE_2";
			case 46: return "JVM_ISTORE_3";
			case 47: return "JVM_ASTORE			" + opcode[1];
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
			case 75: return "JVM_GETFIELD_B		" + bytesToInt(opcode[1], opcode[2]);
			case 76: return "JVM_GETFIELD_C		" + bytesToInt(opcode[1], opcode[2]);
			case 77: return "JVM_GETFIELD_S		" + bytesToInt(opcode[1], opcode[2]);
			case 78: return "JVM_GETFIELD_I		" + bytesToInt(opcode[1], opcode[2]);
			case 79: return "JVM_GETFIELD_A		" + bytesToInt(opcode[1], opcode[2]);
			case 80: return "JVM_PUTFIELD_B		" + bytesToInt(opcode[1], opcode[2]);
			case 81: return "JVM_PUTFIELD_C		" + bytesToInt(opcode[1], opcode[2]);
			case 82: return "JVM_PUTFIELD_S		" + bytesToInt(opcode[1], opcode[2]);
			case 83: return "JVM_PUTFIELD_I		" + bytesToInt(opcode[1], opcode[2]);
			case 84: return "JVM_PUTFIELD_A		" + bytesToInt(opcode[1], opcode[2]);
			case 85: return "JVM_GETSTATIC_B	Infusion:" + opcode[1] + " Field:" + opcode[2];
			case 86: return "JVM_GETSTATIC_C	Infusion:" + opcode[1] + " Field:" + opcode[2];
			case 87: return "JVM_GETSTATIC_S	Infusion:" + opcode[1] + " Field:" + opcode[2];
			case 88: return "JVM_GETSTATIC_I	Infusion:" + opcode[1] + " Field:" + opcode[2];
			case 89: return "JVM_GETSTATIC_A	Infusion:" + opcode[1] + " Field:" + opcode[2];
			case 90: return "JVM_PUTSTATIC_B	Infusion:" + opcode[1] + " Field:" + opcode[2];
			case 91: return "JVM_PUTSTATIC_C	Infusion:" + opcode[1] + " Field:" + opcode[2];
			case 92: return "JVM_PUTSTATIC_S	Infusion:" + opcode[1] + " Field:" + opcode[2];
			case 93: return "JVM_PUTSTATIC_I	Infusion:" + opcode[1] + " Field:" + opcode[2];
			case 94: return "JVM_PUTSTATIC_A	Infusion:" + opcode[1] + " Field:" + opcode[2];
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
			case 121: return "JVM_IINC			Local:" + opcode[1] + " Increment:" + opcode[2];
			case 122: return "JVM_S2B";
			case 123: return "JVM_S2I";
			case 124: return "JVM_I2B";
			case 125: return "JVM_I2S";
			case 126: return "JVM_IIFEQ			Bytecode offset:" + bytesToInt(opcode[1], opcode[2]) + " Branch target: " + bytesToInt(opcode[3], opcode[4]);
			case 127: return "JVM_IIFNE			Bytecode offset:" + bytesToInt(opcode[1], opcode[2]) + " Branch target: " + bytesToInt(opcode[3], opcode[4]);
			case 128: return "JVM_IIFLT			Bytecode offset:" + bytesToInt(opcode[1], opcode[2]) + " Branch target: " + bytesToInt(opcode[3], opcode[4]);
			case 129: return "JVM_IIFGE			Bytecode offset:" + bytesToInt(opcode[1], opcode[2]) + " Branch target: " + bytesToInt(opcode[3], opcode[4]);
			case 130: return "JVM_IIFGT			Bytecode offset:" + bytesToInt(opcode[1], opcode[2]) + " Branch target: " + bytesToInt(opcode[3], opcode[4]);
			case 131: return "JVM_IIFLE			Bytecode offset:" + bytesToInt(opcode[1], opcode[2]) + " Branch target: " + bytesToInt(opcode[3], opcode[4]);
			case 132: return "JVM_IFNULL		Bytecode offset:" + bytesToInt(opcode[1], opcode[2]) + " Branch target: " + bytesToInt(opcode[3], opcode[4]);
			case 133: return "JVM_IFNONNULL		Bytecode offset:" + bytesToInt(opcode[1], opcode[2]) + " Branch target: " + bytesToInt(opcode[3], opcode[4]);
			case 134: return "JVM_IF_SCMPEQ		Bytecode offset:" + bytesToInt(opcode[1], opcode[2]) + " Branch target: " + bytesToInt(opcode[3], opcode[4]);
			case 135: return "JVM_IF_SCMPNE		Bytecode offset:" + bytesToInt(opcode[1], opcode[2]) + " Branch target: " + bytesToInt(opcode[3], opcode[4]);
			case 136: return "JVM_IF_SCMPLT		Bytecode offset:" + bytesToInt(opcode[1], opcode[2]) + " Branch target: " + bytesToInt(opcode[3], opcode[4]);
			case 137: return "JVM_IF_SCMPGE		Bytecode offset:" + bytesToInt(opcode[1], opcode[2]) + " Branch target: " + bytesToInt(opcode[3], opcode[4]);
			case 138: return "JVM_IF_SCMPGT		Bytecode offset:" + bytesToInt(opcode[1], opcode[2]) + " Branch target: " + bytesToInt(opcode[3], opcode[4]);
			case 139: return "JVM_IF_SCMPLE		Bytecode offset:" + bytesToInt(opcode[1], opcode[2]) + " Branch target: " + bytesToInt(opcode[3], opcode[4]);
			case 140: return "JVM_IF_ICMPEQ		Bytecode offset:" + bytesToInt(opcode[1], opcode[2]) + " Branch target: " + bytesToInt(opcode[3], opcode[4]);
			case 141: return "JVM_IF_ICMPNE		Bytecode offset:" + bytesToInt(opcode[1], opcode[2]) + " Branch target: " + bytesToInt(opcode[3], opcode[4]);
			case 142: return "JVM_IF_ICMPLT		Bytecode offset:" + bytesToInt(opcode[1], opcode[2]) + " Branch target: " + bytesToInt(opcode[3], opcode[4]);
			case 143: return "JVM_IF_ICMPGE		Bytecode offset:" + bytesToInt(opcode[1], opcode[2]) + " Branch target: " + bytesToInt(opcode[3], opcode[4]);
			case 144: return "JVM_IF_ICMPGT		Bytecode offset:" + bytesToInt(opcode[1], opcode[2]) + " Branch target: " + bytesToInt(opcode[3], opcode[4]);
			case 145: return "JVM_IF_ICMPLE		Bytecode offset:" + bytesToInt(opcode[1], opcode[2]) + " Branch target: " + bytesToInt(opcode[3], opcode[4]);
			case 146: return "JVM_IF_ACMPEQ		Bytecode offset:" + bytesToInt(opcode[1], opcode[2]) + " Branch target: " + bytesToInt(opcode[3], opcode[4]);
			case 147: return "JVM_IF_ACMPNE		Bytecode offset:" + bytesToInt(opcode[1], opcode[2]) + " Branch target: " + bytesToInt(opcode[3], opcode[4]);
			case 148: return "JVM_GOTO			Bytecode offset:" + bytesToInt(opcode[1], opcode[2]) + " Branch target: " + bytesToInt(opcode[3], opcode[4]);
			case 149: return "JVM_GOTO_W		Bytecode offset:" + bytesToInt(opcode[1], opcode[2]) + " Branch target: " + bytesToInt(opcode[3], opcode[4]);
			case 150: return "JVM_TABLESWITCH	OPERANDS NOT AVAILABLE IN CURRENT TRACER";
			case 151: return "JVM_LOOKUPSWITCH	OPERANDS NOT AVAILABLE IN CURRENT TRACER";
			case 152: return "JVM_SRETURN";
			case 153: return "JVM_IRETURN";
			case 154: return "JVM_ARETURN";
			case 155: return "JVM_RETURN";
			case 156: return "JVM_INVOKEVIRTUAL	OPERANDS NOT AVAILABLE IN CURRENT TRACER";
			case 157: return "JVM_INVOKESPECIAL	OPERANDS NOT AVAILABLE IN CURRENT TRACER";
			case 158: return "JVM_INVOKESTATIC	OPERANDS NOT AVAILABLE IN CURRENT TRACER";
			case 159: return "JVM_INVOKEINTERFACE	OPERANDS NOT AVAILABLE IN CURRENT TRACER";
			case 160: return "JVM_NEW			Infusion:" + opcode[1] + " Class:" + opcode[2];
			case 161: return "JVM_NEWARRAY		Element type:" + opcode[1];
			case 162: return "JVM_ANEWARRAY		Infusion:" + opcode[1] + " String:" + opcode[2];
			case 163: return "JVM_ARRAYLENGTH";
			case 164: return "JVM_ATHROW";
			case 165: return "JVM_CHECKCAST		Infusion:" + opcode[1] + " Class:" + opcode[2];
			case 166: return "JVM_INSTANCEOF	Infusion:" + opcode[1] + " Class:" + opcode[2];
			case 167: return "JVM_MONITORENTER";
			case 168: return "JVM_MONITOREXIT";
			case 169: return "JVM_IDUP_X2";
			case 170: return "JVM_IINC_W		Local:" + opcode[1] + " Increment:" + bytesToInt(opcode[2], opcode[3]);
			case 171: return "JVM_SINC_W";
			case 172: return "JVM_I2C";
			case 173: return "JVM_S2C";
			case 174: return "JVM_B2C";
			case 175: return "JVM_IDUP_X";
			case 176: return "JVM_SIFEQ			Bytecode offset:" + bytesToInt(opcode[1], opcode[2]) + " Branch target: " + bytesToInt(opcode[3], opcode[4]);
			case 177: return "JVM_SIFNE			Bytecode offset:" + bytesToInt(opcode[1], opcode[2]) + " Branch target: " + bytesToInt(opcode[3], opcode[4]);
			case 178: return "JVM_SIFLT			Bytecode offset:" + bytesToInt(opcode[1], opcode[2]) + " Branch target: " + bytesToInt(opcode[3], opcode[4]);
			case 179: return "JVM_SIFGE			Bytecode offset:" + bytesToInt(opcode[1], opcode[2]) + " Branch target: " + bytesToInt(opcode[3], opcode[4]);
			case 180: return "JVM_SIFGT			Bytecode offset:" + bytesToInt(opcode[1], opcode[2]) + " Branch target: " + bytesToInt(opcode[3], opcode[4]);
			case 181: return "JVM_SIFLE			Bytecode offset:" + bytesToInt(opcode[1], opcode[2]) + " Branch target: " + bytesToInt(opcode[3], opcode[4]);
			case 182: return "JVM_LCONST_0";
			case 183: return "JVM_LCONST_1";
			case 184: return "JVM_LLOAD			" + opcode[1];
			case 185: return "JVM_LLOAD_0";
			case 186: return "JVM_LLOAD_1";
			case 187: return "JVM_LLOAD_2";
			case 188: return "JVM_LLOAD_3";
			case 189: return "JVM_LLPUSH		OPERANDS NOT AVAILABLE IN CURRENT TRACER";
			case 190: return "JVM_LSTORE		" + opcode[1];
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
			case 219: return "JVM_BRTARGET		" + branchTargetCounter++;
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

		public JavaInstruction() {
			this.UnoptimisedAvr = new ArrayList<AvrInstruction>();
		}
	}
	private class MethodImpl {
		public int MethodImplId;
		public int StartAddress;
		public int EndAddress;
		public int JvmMethodSize;
		public ArrayList<JavaInstruction> JavaInstructions;
		public ArrayList<AvrInstruction> AvrInstructions;
		public ArrayList<Integer> BranchTargets;

		public MethodImpl() {
			this.JavaInstructions = new ArrayList<JavaInstruction>();
			this.AvrInstructions = new ArrayList<AvrInstruction>();
			this.BranchTargets = new ArrayList<Integer>();
			this.JavaInstructions.add(new JavaInstruction());
			this.JavaInstructions.get(0).Text = "Method preamble";
		}

		public JavaInstruction lastJavaInstruction() {
			return this.JavaInstructions.get(this.JavaInstructions.size()-1);
		}
	}

	private int branchTargetCounter;
	private MethodImpl currentMethod = null;

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

		switch (value) {
			case AVRORA_RTC_STARTMETHOD: {
				this.currentMethod = new MethodImpl();
				this.currentMethod.MethodImplId = (getDataInt8(a, data_addr+1) & 0xff);
				this.currentMethod.StartAddress = getDataInt32(a, data_addr+2);
				branchTargetCounter = 0;
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
				javaInstruction.Text = opcode2string(opcode);
				this.currentMethod.JavaInstructions.add(javaInstruction);
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
				this.currentMethod.lastJavaInstruction().UnoptimisedAvr.add(avrInstruction);
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
				this.currentMethod.lastJavaInstruction().UnoptimisedAvr.add(avrInstruction);
			}
			break;
			case AVRORA_RTC_ENDMETHOD: {
				currentMethod.EndAddress = getDataInt32(a, data_addr+1);
				currentMethod.JvmMethodSize = getDataInt16(a, data_addr+5);
				int numberOfBranchTargets = getDataInt8(a, data_addr+7);

				if (this.currentMethod.StartAddress == 0) {
					Terminal.print("[avrora.rtc] No function start address?? Did you forget to send the AVRORA_RTC_STARTMETHOD command?");
				} else {
					addFunctionDisassembly(state, currentMethod, numberOfBranchTargets);
				}

				if (logToFile) {
					logMethodToFile(this.currentMethod);
				} else {
					printMethodToTerminal(this.currentMethod);
				}
			}
			break;

			default:
				Terminal.print("[avrora.rtc] unknown command " + value + "\n\r");
			break;
		}

    }

    private void printMethodToTerminal(MethodImpl method) {
        StringBuffer buf = new StringBuffer();
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

		synchronized (Terminal.class) {
			Terminal.print(buf.toString());
		}
    }

    private void logMethodToFile(MethodImpl method) {
		String fileheader =
			"from collections import namedtuple\n" +
			"MethodImpl = namedtuple(\"MethodImpl\", \"methodImplId javaTrace avrTrace\")\n" +
			"JavaInstr = namedtuple(\"JavaInstr\", \"offset opcode text avr_preopt avr_postopt\")\n" +
			"AvrInstr = namedtuple(\"AvrInstr\", \"address opcode text\")\n" +
			"methods=[\n";
		// Files.write(logFilepath, fileheader.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);			
		// buf.append(
		// 	"    MethodImpl(methodImplId=5,\n"
	 //        "        javaTrace=[\n");

    }

    private void addFunctionDisassembly(State state, MethodImpl method, int numberOfBranchTargets) {
		Simulator sim = state.getSimulator();
		AtmelInterpreter a = (AtmelInterpreter) sim.getInterpreter();

    	// Scan branch target addresses
    	int address = method.StartAddress;
    	int i = 0;
		while (address < method.StartAddress+numberOfBranchTargets*2) {
			int wordOffsetFromFunctionStart = getProgramInt16(a, address);
			int branchTargetAddress = method.StartAddress + 2*wordOffsetFromFunctionStart;
			method.BranchTargets.add(branchTargetAddress);
			address += 2;
		}

		// Print compiled method, and add branch target annotations where necessary.
		while (address < method.EndAddress) {
			AbstractInstr instr = state.getInstr(address);

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
